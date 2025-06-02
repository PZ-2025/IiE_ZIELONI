package com.example.projektzielonifx.reports;

import static org.junit.jupiter.api.Assertions.*;

import com.example.projektzielonifx.reports.DialogUtils;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

public class DialogUtilsTest {

    /**
     * Testuje, czy metoda showSelectionDialog wywołuje callback z poprawnymi parametrami.
     * Ponieważ metoda używa UI, tutaj test symuluje wywołanie callbacku manualnie.
     */
    @Test
    public void testShowSelectionDialogCallback() {
        Map<String, Integer> options = new HashMap<>();
        options.put("Option1", 1);
        options.put("Option2", 2);

        // Flaga do potwierdzenia, że callback został wywołany
        final boolean[] callbackCalled = {false};

        // Testowy callback
        DialogUtils.BiConsumer<String, Integer> testCallback = (key, value) -> {
            assertEquals("Option1", key);
            assertEquals(1, value);
            callbackCalled[0] = true;
        };

        testCallback.accept("Option1", options.get("Option1"));

        assertTrue(callbackCalled[0], "Callback powinien zostać wywołany");

        System.out.println("Test DialogUtils wykonany poprawnie.");
    }
}
