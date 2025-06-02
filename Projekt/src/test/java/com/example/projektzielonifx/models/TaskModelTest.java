package com.example.projektzielonifx.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Prosty test klasy TaskModel.
 */
public class TaskModelTest {

    @Test
    public void simpleTaskModelTest() {
        TaskModel task = new TaskModel();

        task.setId("1");
        task.setTitle("Testowe zadanie");
        task.setDescription("Opis testowego zadania");
        task.setPriority("wysoki");
        task.setStatus("doZrobienia");
        task.setProgress("50");
        task.setCreated_at("2025-06-01");
        task.setDeadline("2025-06-30");
        task.setAssignedTo("Jan Kowalski");

        assertEquals("1", task.getId());
        assertEquals("Testowe zadanie", task.getTitle());
        assertEquals("Opis testowego zadania", task.getDescription());
        assertEquals("wysoki", task.getPriority());
        assertEquals("doZrobienia", task.getStatus());
        assertEquals("50", task.getProgress());
        assertEquals("2025-06-01", task.getCreated_at());
        assertEquals("2025-06-30", task.getDeadline());
        assertEquals("Jan Kowalski", task.getAssignedTo());

        System.out.println("Test 'simpleTaskModelTest' wykonano poprawnie.");
    }
}
