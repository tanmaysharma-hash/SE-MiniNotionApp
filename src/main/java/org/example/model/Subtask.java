package org.example.model;

public class Subtask {
    private String text;
    private boolean completed;

    public Subtask(String text) {
        this.text = text;
        this.completed = false;
    }

    // Getters
    public String getText() {
        return text;
    }

    public boolean isCompleted() {
        return completed;
    }

    // Setters
    public void setText(String text) {
        this.text = text;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}