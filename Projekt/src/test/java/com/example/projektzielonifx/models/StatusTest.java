package com.example.projektzielonifx.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test klasy enum Status.
 * Sprawdza poprawność wartości i działania metod enum.
 */
public class StatusTest {

    /**
     * Sprawdza czy enum zawiera wszystkie zadeklarowane wartości w odpowiedniej kolejności.
     */
    @Test
    public void testEnumValues() {
        Status[] values = Status.values();
        assertEquals(4, values.length);
        assertEquals(Status.doZrobienia, values[0]);
        assertEquals(Status.wTrakcie, values[1]);
        assertEquals(Status.zrobione, values[2]);
        assertEquals(Status.anulowane, values[3]);

        System.out.println("Test 'testEnumValues' wykonano poprawnie.");
    }

    /**
     * Sprawdza poprawność działania metody valueOf().
     */
    @Test
    public void testEnumValueOf() {
        assertEquals(Status.doZrobienia, Status.valueOf("doZrobienia"));
        assertEquals(Status.wTrakcie, Status.valueOf("wTrakcie"));
        assertEquals(Status.zrobione, Status.valueOf("zrobione"));
        assertEquals(Status.anulowane, Status.valueOf("anulowane"));

        System.out.println("Test 'testEnumValueOf' wykonano poprawnie.");
    }
}
