package com.example.projektzielonifx.auth;

import javafx.embed.swing.JFXPanel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test klasy LoginWindow.
 *
 * Ten test sprawdza, czy instancja klasy LoginWindow może zostać poprawnie utworzona.
 * W teście wymuszona jest inicjalizacja środowiska JavaFX,
 * aby uniknąć błędu "Toolkit not initialized".
 */
public class LoginWindowTest {

    /**
     * Inicjalizuje toolkit JavaFX przed uruchomieniem testów.
     * Tworzenie obiektu JFXPanel inicjuje JavaFX toolkit.
     */
    @BeforeAll
    public static void initToolkit() {
        new JFXPanel(); // inicjalizacja toolkit JavaFX
    }

    /**
     * Testuje, czy instancja LoginWindow może zostać utworzona bez błędów.
     * Test ten nie uruchamia aplikacji, tylko sprawdza konstruktor.
     */
    @Test
    public void testLoginWindowInstanceCreation() {
        LoginWindow window = new LoginWindow();
        assertNotNull(window, "Instancja LoginWindow została utworzona poprawnie.");
        System.out.println("Test klasy LoginWindow wykonany poprawnie.");
    }
}
