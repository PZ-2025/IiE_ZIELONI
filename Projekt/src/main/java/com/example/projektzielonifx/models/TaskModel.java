package com.example.projektzielonifx.models;

public class TaskModel {
    private String id;
    private String milestone_id;
    private String title;
    private String description;
    private String priority;
    private String status;
    private String progress;
    private String created_at;
    private String deadline;
    private String canceled_by;

    public TaskModel(String id, String milestone_id, String title, String description, String priority, String status, String progress, String created_at, String deadline, String canceled_by) {
        this.id = id;
        this.milestone_id = milestone_id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.progress = progress;
        this.created_at = created_at;
        this.deadline = deadline;
        this.canceled_by = canceled_by;
    }

    public String getId() {
        return id;
    }

    public String getMilestone_id() {
        return milestone_id;
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

    public String getCanceled_by() {
        return canceled_by;
    }
}