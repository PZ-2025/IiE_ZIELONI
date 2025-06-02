package com.example.projektzielonifx.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testuje enum Priority - podstawowe metody enumów.
 */
public class PriorityTest {

    /**
     * Sprawdza czy enum zawiera właściwe wartości.
     */
    @Test
    public void testEnumValues() {
        Priority[] values = Priority.values();
        assertEquals(3, values.length);
        assertEquals(Priority.niski, values[0]);
        assertEquals(Priority.sredni, values[1]);
        assertEquals(Priority.wysoki, values[2]);

        System.out.println("Test 'testEnumValues' wykonano poprawnie.");
    }

    /**
     * Sprawdza działanie valueOf().
     */
    @Test
    public void testEnumValueOf() {
        assertEquals(Priority.niski, Priority.valueOf("niski"));
        assertEquals(Priority.sredni, Priority.valueOf("sredni"));
        assertEquals(Priority.wysoki, Priority.valueOf("wysoki"));

        System.out.println("Test 'testEnumValueOf' wykonano poprawnie.");
    }
}
