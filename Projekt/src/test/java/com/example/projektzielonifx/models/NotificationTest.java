package com.example.projektzielonifx.models;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test klasy Notification - sprawdzenie działania konstruktorów, getterów, setterów i toString().
 */
public class NotificationTest {

    /**
     * Testuje działanie pól klasy Notification.
     */
    @Test
    public void testNotificationGettersAndSetters() {
        // Tworzymy przykładowy czas
        LocalDateTime time = LocalDateTime.of(2025, 6, 2, 15, 30);

        // Tworzymy obiekt Notification
        Notification notification = new Notification(1, "Nowa wiadomość", "INFO", time);

        // Sprawdzamy gettery
        assertEquals(1, notification.getId());
        assertEquals("Nowa wiadomość", notification.getMessage());
        assertEquals("INFO", notification.getType());
        assertEquals(time, notification.getCreatedAt());
        assertFalse(notification.isRead());

        // Ustawiamy nowe wartości
        notification.setId(2);
        notification.setMessage("Zmieniona wiadomość");
        notification.setType("WARNING");
        notification.setCreatedAt(LocalDateTime.of(2025, 6, 3, 12, 0));
        notification.setRead(true);

        // Sprawdzamy settery
        assertEquals(2, notification.getId());
        assertEquals("Zmieniona wiadomość", notification.getMessage());
        assertEquals("WARNING", notification.getType());
        assertEquals(LocalDateTime.of(2025, 6, 3, 12, 0), notification.getCreatedAt());
        assertTrue(notification.isRead());

        // Test metody toString
        assertEquals("[WARNING] Zmieniona wiadomość", notification.toString());

        System.out.println("Test 'testNotificationGettersAndSetters' wykonano poprawnie.");
    }
}
