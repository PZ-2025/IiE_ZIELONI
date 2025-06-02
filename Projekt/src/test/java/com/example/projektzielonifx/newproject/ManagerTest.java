package com.example.projektzielonifx.newproject;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Prosty test klasy Manager.
 */
public class ManagerTest {

    @Test
    public void testManagerConstructorAndGetters() {
        Manager manager = new Manager(1, "Jan Kowalski");

        assertEquals(1, manager.getId());
        assertEquals("Jan Kowalski", manager.getFullName());
        assertEquals("Jan Kowalski", manager.toString());

        System.out.println("Test 'testManagerConstructorAndGetters' wykonano poprawnie.");
    }
}
