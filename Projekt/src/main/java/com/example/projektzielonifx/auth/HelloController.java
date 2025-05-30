package com.example.projektzielonifx.auth;

import com.example.projektzielonifx.database.DBUtil;
import com.example.projektzielonifx.database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Kontroler odpowiedzialny za obsługę okna logowania w aplikacji.
 * Implementuje interfejs Initializable do inicjalizacji elementów interfejsu.
 */
public class HelloController implements Initializable {

    /**
     * Przycisk służący do zatwierdzenia logowania.
     */
    @FXML
    private Button loginButton;

    /**
     * Pole tekstowe do wprowadzania nazwy użytkownika.
     */
    @FXML
    private TextField tf_username;

    /**
     * Pole do bezpiecznego wprowadzania hasła użytkownika.
     */
    @FXML
    private PasswordField tf_password;

    /**
     * Inicjalizuje kontroler po całkowitym przetworzeniu elementu root.
     * Konfiguruje obsługę zdarzenia kliknięcia przycisku logowania.
     *
     * @param url Lokalizacja używana do rozwiązywania ścieżek względnych, lub null jeśli lokalizacja jest nieznana.
     * @param resourceBundle Zasoby używane do lokalizacji, lub null jeśli element root nie został zlokalizowany.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loginButton.setOnAction(event -> {
            DBUtil.logInUser(event, tf_username.getText(), tf_password.getText());
        });
    }
}

