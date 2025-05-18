package com.example.projektzielonifx.models;

public class TaskModel {
    private String id;
    private String title;
    private String description;
    private String priority;
    private String status;
    private String progress;
    private String created_at;
    private String deadline;

    public TaskModel(String id, String title, String description, String priority, String status, String progress, String created_at, String deadline) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.progress = progress;
        this.created_at = created_at;
        this.deadline = deadline;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPriority() {
        return priority;
    }

    public String getStatus() {
        return status;
    }

    public String getProgress() {
        return progress;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getDeadline() {
        return deadline;
    }

}