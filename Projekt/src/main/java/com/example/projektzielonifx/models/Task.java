package com.example.projektzielonifx.models;

import java.time.LocalDate;

public class Task {
    private int id;
    private int milestoneId;
    private String title;
    private String description;
    private Priority priority;
    private Status status;
    private int progress;
    private LocalDate deadline;
    private String milestoneName;

    private Integer assignedUserId; // 0 = none
    private String assignedUserName;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMilestoneId() { return milestoneId; }
    public void setMilestoneId(int milestoneId) { this.milestoneId = milestoneId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public String getMilestoneName() {
        return milestoneName;
    }

    public void setMilestoneName(String milestoneName) {
        this.milestoneName = milestoneName;
    }

    public Integer getAssignedUserId() { return assignedUserId; }
    public void setAssignedUserId(Integer assignedUserId) { this.assignedUserId = assignedUserId; }

    public String getAssignedUserName() { return assignedUserName; }
    public void setAssignedUserName(String assignedUserName) { this.assignedUserName = assignedUserName; }

}
