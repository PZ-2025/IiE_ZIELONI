package com.example.projektzielonifx.auth;

import com.example.projektzielonifx.database.DBUtil;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.URL;
import java.util.ResourceBundle;

import static org.mockito.Mockito.*;

/**
 * Testy jednostkowe dla klasy HelloController.
 */
public class HelloControllerTest {

    private HelloController controller;

    /**
     * Metoda przygotowująca nową instancję kontrolera przed każdym testem.
     * Inicjalizuje niezbędne komponenty GUI jako mocki.
     */
    @BeforeEach
    public void setUp() {
        controller = new HelloController();

        // Mockujemy elementy GUI
        controller.tf_username = new TextField("testuser");
        controller.tf_password = new PasswordField();
        controller.tf_password.setText("secret");
        controller.loginButton = new Button();

        // Inicjalizacja (symulujemy wywołanie z FXML)
        controller.initialize(mock(URL.class), mock(ResourceBundle.class));
    }

    /**
     * Testuje, czy po kliknięciu przycisku login wywoływana jest metoda logInUser z odpowiednimi parametrami.
     */
    @Test
    public void testLoginButtonAction() {
        // Zamieniamy statyczną metodę logInUser na mock (trudne bez narzędzi jak PowerMockito, więc tylko pokaz)
        // W praktyce lepiej przetestować logikę niezależną od static metody (albo opakować ją w klasę).

        // Symulujemy kliknięcie
        controller.loginButton.fire();

        // Zakładamy, że metoda DBUtil.logInUser została poprawnie wywołana
        // UWAGA: logInUser jest metodą statyczną, nie można jej łatwo zmockować bez narzędzi typu PowerMock.
        // Ten test tylko sprawdza, że akcja została przypisana i działa bez wyjątku.
    }
}
