package com.example.projektzielonifx.models;

public class TaskModel {
    protected String id;
    protected String title;
    protected String description;
    protected String priority;
    protected String status;
    protected String progress;
    protected String created_at;
    protected String deadline;
    protected String assignedTo;

    public  TaskModel(String id, String title, String description, String priority, String status, String progress, String created_at, String deadline, String assignedTo) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.progress = progress;
        this.created_at = created_at;
        this.deadline = deadline;
        this.assignedTo = assignedTo;

    }
    // New no-argument constructor
    public TaskModel() {
        // Initialize with empty strings or default values
        this("", "", "", "", "", "", "", "","");
    }

    public String getId() {
        return id;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
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

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }
}