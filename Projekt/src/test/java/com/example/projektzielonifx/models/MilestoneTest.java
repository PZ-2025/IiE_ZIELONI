package com.example.projektzielonifx.models;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test klasy Milestone - proste testy getterów, setterów i toString().
 */
public class MilestoneTest {

    /**
     * Testuje podstawowe działanie getterów i setterów w klasie Milestone.
     */
    @Test
    public void testMilestoneGettersAndSetters() {
        // Tworzenie obiektu z konstruktora
        Milestone milestone = new Milestone(1, 100, "Test Milestone");

        // Testy setterów
        milestone.setDescription("Opis testowy");
        milestone.setDeadline(LocalDate.of(2025, 6, 2));

        // Testy getterów
        assertEquals(1, milestone.getId());
        assertEquals(100, milestone.getProjectId());
        assertEquals("Test Milestone", milestone.getName());
        assertEquals("Opis testowy", milestone.getDescription());
        assertEquals(LocalDate.of(2025, 6, 2), milestone.getDeadline());

        // Test metody toString
        assertEquals("Test Milestone (ID: 1)", milestone.toString());

        System.out.println("Test 'testMilestoneGettersAndSetters' wykonano poprawnie.");
    }
}
