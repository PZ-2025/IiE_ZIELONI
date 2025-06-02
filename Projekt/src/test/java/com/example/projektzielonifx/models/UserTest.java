package com.example.projektzielonifx.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy jednostkowe dla klasy {@link User}.
 * Testują poprawność działania getterów, setterów i właściwości JavaFX.
 */
class UserTest {

    /**
     * Testuje tworzenie obiektu User oraz działanie getterów i setterów na właściwościach.
     */
    @Test
    void testUserProperties() {
        User user = new User(
                10,
                "Adam",
                "Nowak",
                "Tester",
                "Zieloni",
                "2025-01-01",
                "adam.nowak",
                "hash123",
                "2025-05-01"
        );

        // Sprawdzamy gettery
        assertEquals(10, user.getId());
        assertEquals("Adam", user.getFirstName());
        assertEquals("Nowak", user.getLastName());
        assertEquals("Tester", user.getRole());
        assertEquals("Zieloni", user.getTeam());
        assertEquals("2025-01-01", user.getHireDate());
        assertEquals("adam.nowak", user.getLogin());
        assertEquals("hash123", user.getPassword());
        assertEquals("2025-05-01", user.getCreatedAt());

        // Testujemy settery
        user.setId(20);
        user.setFirstName("Ewa");
        user.setLastName("Kowalska");
        user.setRole("Developer");
        user.setTeam("Czerwoni");
        user.setHireDate("2025-02-02");
        user.setLogin("ewa.kowalska");
        user.setPasswordHash("newHash456");
        user.setCreatedAt("2025-06-01");

        assertEquals(20, user.getId());
        assertEquals("Ewa", user.getFirstName());
        assertEquals("Kowalska", user.getLastName());
        assertEquals("Developer", user.getRole());
        assertEquals("Czerwoni", user.getTeam());
        assertEquals("2025-02-02", user.getHireDate());
        assertEquals("ewa.kowalska", user.getLogin());
        assertEquals("newHash456", user.getPassword());
        assertEquals("2025-06-01", user.getCreatedAt());

        System.out.println("Test klasy User wykonany poprawnie.");
    }
}
