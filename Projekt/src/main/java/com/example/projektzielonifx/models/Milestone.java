package com.example.projektzielonifx.models;

import java.time.LocalDate;

public class Milestone {
    protected int id;
    protected int projectId;  // Add this field
    protected String name;
    protected String description;
    protected LocalDate deadline;

    public Milestone(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // Constructor with projectId
    public Milestone(int id, int projectId, String name, LocalDate deadline) {
        this.id = id;
        this.projectId = projectId;
        this.name = name;
        this.deadline = deadline;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProjectId() { return projectId; }  // Add getter
    public void setProjectId(int projectId) { this.projectId = projectId; }  // Add setter

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    @Override
    public String toString() {
        return name + " (ID: " + id + ")";
    }
}