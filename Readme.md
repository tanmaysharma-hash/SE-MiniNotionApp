# MiniNotion

**MiniNotion** is a modern, desktop-based task manager inspired by Notion, designed with **JavaFX**. It helps you stay productive by letting you create tasks, break them down into subtasks, assign priorities, and track your progress — all stored locally on your machine.

---

## ✨ Key Features

* **Complete Task Management** – Create, view, update, and delete tasks
* **Subtask Checklists** – Divide big tasks into smaller actionable items
* **Progress Tracking** – Automatic progress bars based on completed subtasks
* **Task Metadata** – Add priority (High/Medium/Low), status (To-Do/In Progress/Done), and due dates
* **Smart Search & Filters** – Instantly find tasks and filter by status
* **Sorting Options** – Organize tasks by due date or priority level
* **Two-Panel Interface** – Task list on the left, details view on the right
* **Overdue Highlighting** – Late tasks are automatically highlighted in red
* **Dark Theme UI** – A polished, minimal dark mode interface
* **Local JSON Storage** – Data saved persistently in `data/task.json`

---

## 🛠 Tech Stack

* **Java 17** – Core programming language
* **JavaFX 17** – User interface framework
* **Gradle** – Build and dependency management
* **Google Gson** – JSON data storage and parsing

---

## 🚀 Getting Started

### Prerequisites

* Install **JDK 17** or later
* Install **Git**

### Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/your-username/MiniNotion.git
   ```
2. Enter the project folder:

   ```bash
   cd MiniNotion
   ```
3. Run the project using Gradle wrapper:

    * On **Windows**:

      ```bash
      gradlew.bat run
      ```
    * On **macOS/Linux**:

      ```bash
      ./gradlew run
      ```

---

## 📂 Project Structure

```
MiniNotion/
├── data/
│   └── task.json          # Local storage file
├── gradle/                # Gradle wrapper
├── src/main/
│   ├── java/org/example/
│   │   ├── model/        # Task & Subtask classes
│   │   ├── storage/      # TaskStorage (JSON persistence)
│   │   └── MiniNotion.java # Main application class
│   └── resources/
│       ├── icons/        # Icons
│       └── styles/       # CSS styling
├── build.gradle.kts
└── README.md
```

---

## 📄 License

This project is licensed under the **MIT License**. See the `LICENSE` file for details.
