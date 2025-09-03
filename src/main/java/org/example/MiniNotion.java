package org.example;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.example.model.Subtask;
import org.example.model.Task;
import org.example.storage.TaskStorage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;

public class MiniNotion extends Application {

    private final TaskStorage taskStorage = new TaskStorage();
    private ObservableList<Task> tasks;
    private ListView<Task> taskListView;
    private FilteredList<Task> filteredData;
    private VBox detailsPanel; // Panel to show details of selected task

    // UI elements for the details panel that need to be updated
    private Label titleLabel, statusLabel, priorityLabel, dueDateLabel;
    private Label descriptionText;
    private VBox subtaskContainer;
    private ProgressBar subtaskProgressBar;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            tasks = FXCollections.observableArrayList(taskStorage.loadTasks());
        } catch (IOException e) {
            e.printStackTrace();
            tasks = FXCollections.observableArrayList();
        }

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        taskListView = createListView();


        ToolBar toolBar = createToolBar();


        detailsPanel = createDetailsPanel();

        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(taskListView, detailsPanel);
        splitPane.setDividerPositions(0.4);

        root.setTop(toolBar);
        root.setCenter(splitPane);

        // Listener to update details panel when selection changes
        taskListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                updateDetailsPanel(newSelection);
            }
        });

        Scene scene = new Scene(root, 1200, 700);
        scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());

        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/TaskManager.png")));
        } catch(Exception e) {
            System.err.println("App icon not found.");
        }

        primaryStage.setTitle("MiniNotion - Complete");
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
            try {
                taskStorage.saveTasks(tasks);
            } catch (IOException e) {
                e.printStackTrace();
                showError("Save Error", "Could not save tasks to file.");
            }
        });
    }

    private ToolBar createToolBar() {
        // --- Buttons ---
        Button addButton = new Button("Add Task", createIcon("/icons/New.png"));
        addButton.setContentDisplay(ContentDisplay.LEFT);
        addButton.setOnAction(e -> showTaskDialog(null));

        Button editButton = new Button("Edit Task", createIcon("/icons/Edit.png"));
        editButton.setContentDisplay(ContentDisplay.LEFT);
        editButton.setOnAction(e -> {
            Task selected = taskListView.getSelectionModel().getSelectedItem();
            if (selected != null) showTaskDialog(selected);
            else showAlert("No Task Selected", "Please select a task to edit.");
        });

        Button deleteButton = new Button("Delete Task", createIcon("/icons/Delete.png"));
        deleteButton.setContentDisplay(ContentDisplay.LEFT);
        deleteButton.setId("deleteButton");
        deleteButton.setOnAction(e -> deleteTask());

        // --- Filtering and Sorting ---
        TextField searchField = new TextField();
        searchField.setPromptText("\uD83D\uDD0E Search tasks..."); // Using Unicode escape for 🔍

        ComboBox<Task.Status> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll(Task.Status.values());
        statusFilter.setPromptText("Filter by Status...");

        ChoiceBox<String> sortBy = new ChoiceBox<>(FXCollections.observableArrayList("Due Date", "Priority"));
        sortBy.setValue("Due Date");

        setupFilteringAndSorting(searchField, statusFilter, sortBy);

        HBox filterSortGroup = new HBox(10, new Label("Filter:"), statusFilter, new Label("Sort By:"), sortBy);
        filterSortGroup.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        return new ToolBar(addButton, editButton, deleteButton, spacer, searchField, filterSortGroup);
    }

    private void setupFilteringAndSorting(TextField searchField, ComboBox<Task.Status> statusFilter, ChoiceBox<String> sortBy) {
        filteredData = new FilteredList<>(tasks, p -> true);

        // Combine predicates for search and status filter
        Predicate<Task> searchPredicate = task -> {
            String searchText = searchField.getText();
            if (searchText == null || searchText.isEmpty()) return true;
            String lowerCaseFilter = searchText.toLowerCase();
            return task.getTitle().toLowerCase().contains(lowerCaseFilter);
        };

        Predicate<Task> statusPredicate = task -> {
            Task.Status selectedStatus = statusFilter.getValue();
            return selectedStatus == null || task.getStatus() == selectedStatus;
        };

        // Use JavaFX Bindings to combine the predicates
        filteredData.predicateProperty().bind(Bindings.createObjectBinding(
                () -> searchPredicate.and(statusPredicate),
                searchField.textProperty(),
                statusFilter.valueProperty()
        ));

        SortedList<Task> sortedData = new SortedList<>(filteredData);
        sortBy.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if ("Priority".equals(newVal)) {
                sortedData.setComparator(Comparator.comparing(Task::getPriority));
            } else {
                sortedData.setComparator(Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())));
            }
        });

        sortedData.setComparator(Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())));
        taskListView.setItems(sortedData);
    }

    private VBox createDetailsPanel() {
        detailsPanel = new VBox(20);
        detailsPanel.setPadding(new Insets(20));
        detailsPanel.getStyleClass().add("details-panel");

        // Initialize UI components
        titleLabel = new Label();
        titleLabel.getStyleClass().add("details-title");
        statusLabel = new Label();
        priorityLabel = new Label();
        dueDateLabel = new Label();
        descriptionText = new Label();
        descriptionText.setWrapText(true);
        subtaskContainer = new VBox(8);
        subtaskProgressBar = new ProgressBar(0);
        subtaskProgressBar.setMaxWidth(Double.MAX_VALUE);

        // Placeholder content
        Label placeholder = new Label("Select a task to see its details");
        placeholder.getStyleClass().add("placeholder-text");
        detailsPanel.getChildren().add(placeholder);

        return detailsPanel;
    }

    private void updateDetailsPanel(Task task) {
        detailsPanel.getChildren().clear();

        // Populate with selected task's data
        titleLabel.setText(task.getTitle());
        statusLabel.setText("Status: " + task.getStatus());
        priorityLabel.setText("Priority: " + task.getPriority());
        dueDateLabel.setText("Due: " + (task.getDueDate() != null ? task.getDueDate().toString() : "N/A"));
        descriptionText.setText(task.getDescription());

        // Add Subtask controls
        Label subtaskHeader = new Label("Subtasks");
        subtaskHeader.getStyleClass().add("details-header");

        updateSubtaskList(task); // Populate the subtask container
        subtaskProgressBar.setProgress(task.getCompletionPercentage());

        TextField addSubtaskField = new TextField();
        addSubtaskField.setPromptText("Add a new subtask...");
        addSubtaskField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && !addSubtaskField.getText().trim().isEmpty()) {
                Subtask newSubtask = new Subtask(addSubtaskField.getText().trim());
                task.addSubtask(newSubtask);
                addSubtaskField.clear();
                updateSubtaskList(task);
                taskListView.refresh();
            }
        });

        ScrollPane descriptionScrollPane = new ScrollPane(descriptionText);
        descriptionScrollPane.setFitToWidth(true);
        descriptionScrollPane.getStyleClass().add("description-scroll-pane");

        detailsPanel.getChildren().addAll(
                titleLabel, statusLabel, priorityLabel, dueDateLabel,
                new Separator(),
                descriptionScrollPane,
                new Separator(),
                subtaskHeader, subtaskProgressBar, subtaskContainer, addSubtaskField
        );
    }

    private void updateSubtaskList(Task task) {
        subtaskContainer.getChildren().clear();
        for (Subtask subtask : task.getSubtasks()) {
            CheckBox checkBox = new CheckBox(subtask.getText());
            checkBox.setSelected(subtask.isCompleted());
            checkBox.setOnAction(e -> {
                subtask.setCompleted(checkBox.isSelected());
                subtaskProgressBar.setProgress(task.getCompletionPercentage());
                taskListView.refresh();
            });
            subtaskContainer.getChildren().add(checkBox);
        }
    }


    // --- (Utility and Dialog methods below) ---
    private ImageView createIcon(String path) {
        try {
            Image image = new Image(getClass().getResourceAsStream(path), 16, 16, true, true);
            return new ImageView(image);
        } catch (Exception e) {
            System.err.println("Cannot load icon: " + path);
            return null;
        }
    }

    private ListView<Task> createListView() {
        ListView<Task> listView = new ListView<>();
        listView.setCellFactory(param -> new TaskListCell());
        return listView;
    }

    private void deleteTask() {
        Task selected = taskListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Task Selected", "Please select a task to delete.");
            return;
        }
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to permanently delete this task?", ButtonType.YES, ButtonType.CANCEL);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Task: \"" + selected.getTitle() + "\"?");
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                tasks.remove(selected);
                detailsPanel.getChildren().clear(); // Clear details panel
                detailsPanel.getChildren().add(new Label("Select a task to see its details"));
            }
        });
    }

    private void showTaskDialog(Task task) {
        // (This method remains largely the same as before)
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle(task == null ? "Add New Task" : "Edit Task");
        dialog.setHeaderText(task == null ? "Enter the details for your new task." : "Update the details of your task.");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField();
        titleField.setPromptText("e.g., Finish project report");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Add more details here...");
        ComboBox<Task.Priority> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().setAll(Task.Priority.values());
        DatePicker dueDatePicker = new DatePicker();
        ComboBox<Task.Status> statusCombo = new ComboBox<>();
        statusCombo.getItems().setAll(Task.Status.values());

        if (task != null) {
            titleField.setText(task.getTitle());
            descriptionArea.setText(task.getDescription());
            priorityCombo.setValue(task.getPriority());
            dueDatePicker.setValue(task.getDueDate());
            statusCombo.setValue(task.getStatus());
        } else {
            priorityCombo.setValue(Task.Priority.MEDIUM);
            statusCombo.setValue(Task.Status.TO_DO);
            dueDatePicker.setValue(LocalDate.now());
        }

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionArea, 1, 1);
        grid.add(new Label("Priority:"), 0, 2);
        grid.add(priorityCombo, 1, 2);
        grid.add(new Label("Due Date:"), 0, 3);
        grid.add(dueDatePicker, 1, 3);
        grid.add(new Label("Status:"), 0, 4);
        grid.add(statusCombo, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (titleField.getText().trim().isEmpty()) {
                    showError("Validation Error", "Title cannot be empty.");
                    return null;
                }
                if (task == null) {
                    return new Task(titleField.getText(), descriptionArea.getText(), priorityCombo.getValue(), dueDatePicker.getValue());
                } else {
                    task.setTitle(titleField.getText());
                    task.setDescription(descriptionArea.getText());
                    task.setPriority(priorityCombo.getValue());
                    task.setDueDate(dueDatePicker.getValue());
                    task.setStatus(statusCombo.getValue());
                    return task;
                }
            }
            return null;
        });

        Optional<Task> result = dialog.showAndWait();
        result.ifPresent(updatedTask -> {
            if (!tasks.contains(updatedTask)) {
                tasks.add(updatedTask);
            }
            taskListView.refresh();
            updateDetailsPanel(updatedTask);
        });
    }

    private static class TaskListCell extends ListCell<Task> {
        private final HBox content;
        private final Circle priorityCircle;
        private final Text title;
        private final Text details;
        private final ProgressBar progressBar;

        public TaskListCell() {
            super();
            priorityCircle = new Circle(5);
            title = new Text();
            title.setFont(Font.font("System", FontWeight.BOLD, 14));
            details = new Text();
            details.setFill(Color.GRAY);
            progressBar = new ProgressBar();
            progressBar.setMaxWidth(Double.MAX_VALUE);
            VBox textVBox = new VBox(5, title, details, progressBar);
            content = new HBox(10, priorityCircle, textVBox);
            HBox.setHgrow(textVBox, Priority.ALWAYS);
            content.setAlignment(Pos.CENTER_LEFT);
            content.setPadding(new Insets(5, 10, 5, 10));
        }

        @Override
        protected void updateItem(Task task, boolean empty) {
            super.updateItem(task, empty);
            getStyleClass().remove("overdue-task");
            if (task == null || empty) {
                setGraphic(null);
            } else {
                title.setText(task.getTitle());
                String dueDateStr = task.getDueDate() != null ? task.getDueDate().toString() : "No due date";
                details.setText("Status: " + task.getStatus() + " | Due: " + dueDateStr);
                progressBar.setProgress(task.getCompletionPercentage());


                if (task.getDueDate() != null && task.getDueDate().isBefore(LocalDate.now()) && task.getStatus() != Task.Status.DONE) {
                    getStyleClass().add("overdue-task");
                }

                switch (task.getPriority()) {
                    case HIGH: priorityCircle.setFill(Color.RED); break;
                    case MEDIUM: priorityCircle.setFill(Color.ORANGE); break;
                    case LOW: priorityCircle.setFill(Color.GREEN); break;
                }
                setGraphic(content);
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}