package com.example.projektzielonifx.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TaskModelTest {

    @Test
    public void testTaskModelConstructorAndGetters() {
        // Arrange
        String id = "1";
        String title = "Implement login";
        String description = "Create login form and logic";
        String priority = "High";
        String status = "Not Started";
        String progress = "0%";
        String createdAt = "2024-01-01";
        String deadline = "2024-02-01";

        // Act
        TaskModel task = new TaskModel(id, title, description, priority, status, progress, createdAt, deadline);

        // Assert
        assertEquals(id, task.getId());
        assertEquals(title, task.getTitle());
        assertEquals(description, task.getDescription());
        assertEquals(priority, task.getPriority());
        assertEquals(status, task.getStatus());
        assertEquals(progress, task.getProgress());
        assertEquals(createdAt, task.getCreated_at());
        assertEquals(deadline, task.getDeadline());
    }
}
