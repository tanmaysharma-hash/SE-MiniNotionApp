package org.example.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Task {


    public enum Priority { HIGH, MEDIUM, LOW }
    public enum Status {
        TO_DO("To-Do"), IN_PROGRESS("In Progress"), DONE("Done");
        private final String displayName;
        Status(String displayName) { this.displayName = displayName; }
        @Override public String toString() { return displayName; }
    }

    private final String id;
    private String title;
    private String description;
    private Priority priority;
    private Status status;
    private LocalDate dueDate;
    private final List<Subtask> subtasks; // New field for subtasks

    public Task(String title, String description, Priority priority, LocalDate dueDate) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
        this.status = Status.TO_DO;
        this.subtasks = new ArrayList<>(); // Initialize the list
    }


    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Priority getPriority() { return priority; }
    public Status getStatus() { return status; }
    public LocalDate getDueDate() { return dueDate; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public void setStatus(Status status) { this.status = status; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }


    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    public void addSubtask(Subtask subtask) {
        this.subtasks.add(subtask);
    }

    public double getCompletionPercentage() {
        if (subtasks.isEmpty()) {
            return status == Status.DONE ? 1.0 : 0.0;
        }
        long completedCount = subtasks.stream().filter(Subtask::isCompleted).count();
        return (double) completedCount / subtasks.size();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getTitle();
    }
}