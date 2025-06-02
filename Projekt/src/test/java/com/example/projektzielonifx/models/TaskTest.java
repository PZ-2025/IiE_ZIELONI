package com.example.projektzielonifx.models;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prosty test klasy Task.
 */
public class TaskTest {

    @Test
    public void simpleTaskTest() {
        Task task = new Task();

        task.setId(123);
        task.setTitle("Zadanie testowe");
        task.setDeadline(LocalDate.of(2025, 12, 31));

        assertEquals(123, task.getId());
        assertEquals("Zadanie testowe", task.getTitle());
        assertEquals(LocalDate.of(2025, 12, 31), task.getDeadline());

        System.out.println("Test 'simpleTaskTest' wykonano poprawnie.");
    }
}
