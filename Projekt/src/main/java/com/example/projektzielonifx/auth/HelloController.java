package com.example.projektzielonifx.auth;

import com.example.projektzielonifx.database.DBUtil;
import com.example.projektzielonifx.database.DatabaseConnection;
import com.example.projektzielonifx.auth.SecurePasswordManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

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
    protected Button loginButton;

    /**
     * Pole tekstowe do wprowadzania nazwy użytkownika.
     */
    @FXML
    protected TextField tf_username;

    /**
     * Pole do bezpiecznego wprowadzania hasła użytkownika.
     */
    @FXML
    protected PasswordField tf_password;

    /**
     * Główny kontener formularza logowania.
     */
    @FXML
    protected VBox loginForm;

    /**
     * Kontener z nakładką ładowania.
     */
    @FXML
    protected VBox loadingOverlay;

    /**
     * Wskaźnik postępu ładowania.
     */
    @FXML
    protected ProgressIndicator loadingIndicator;

    /**
     * Etykieta z tekstem ładowania.
     */
    @FXML
    protected Label loadingLabel;

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
            handleLoginButtonClick(event);
        });

        // Optional: Allow Enter key to trigger login
        tf_password.setOnAction(event -> {
            handleLoginButtonClick(event);
        });
    }

    /**
     * Obsługuje kliknięcie przycisku logowania z migracją haseł i wskaźnikiem ładowania.
     */
    private void handleLoginButtonClick(javafx.event.ActionEvent event) {
        String username = tf_username.getText().trim();
        String password = tf_password.getText();

        // Basic validation
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter both username and password", Alert.AlertType.WARNING);
            return;
        }

        // Show initial loading state
        showLoadingState(true);

        // Start with migration check and then login
        performMigrationAndLogin(event, username, password);
    }

    /**
     * Wykonuje migrację haseł (jeśli potrzebna) a następnie logowanie
     */
    private void performMigrationAndLogin(javafx.event.ActionEvent event, String username, String password) {
        // Create progress dialog for migration
        Stage progressStage = new Stage();
        progressStage.setTitle("Migration Progress");
        progressStage.initModality(Modality.APPLICATION_MODAL);

        VBox progressBox = new VBox(10);
        progressBox.setPadding(new Insets(20));
        progressBox.setAlignment(Pos.CENTER);

        Label progressLabel = new Label("Checking password security...");
        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(300);

        progressBox.getChildren().addAll(progressLabel, progressBar);

        Scene progressScene = new Scene(progressBox, 350, 120);
        progressStage.setScene(progressScene);
        progressStage.setResizable(false);

        // Show progress dialog
        progressStage.show();

        // Create the migration service
        PasswordMigrationService migrationService = new PasswordMigrationService();

        // Bind progress bar to task
        progressBar.progressProperty().bind(migrationService.progressProperty());
        progressLabel.textProperty().bind(migrationService.messageProperty());

        migrationService.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                progressStage.close();

                boolean isMigrationSuccessful = migrationService.getValue();
                if (!isMigrationSuccessful) {
                    showLoadingState(false);
                    showAlert("Error", "Password migration failed", Alert.AlertType.ERROR);
                    return;
                }

                // Update loading label for login phase
                loadingLabel.setText("Logging in...");

                // Now proceed with actual login
                performLogin(event, username, password);
            });
        });

        migrationService.setOnFailed(e -> {
            Platform.runLater(() -> {
                progressStage.close();
                showLoadingState(false);
                showAlert("Error", "Password migration failed: " + migrationService.getException().getMessage(), Alert.AlertType.ERROR);
            });
        });

        // Start migration service
        migrationService.start();
    }

    /**
     * Wykonuje rzeczywiste logowanie po zakończeniu migracji
     */
    private void performLogin(javafx.event.ActionEvent event, String username, String password) {
        LoginService loginService = new LoginService(username, password, event);

        loginService.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                showLoadingState(false);
            });
        });

        loginService.setOnFailed(e -> {
            Platform.runLater(() -> {
                showLoadingState(false);
                Throwable exception = loginService.getException();
                showAlert("Error", "Login failed: " + exception.getMessage(), Alert.AlertType.ERROR);
            });
        });

        // Start login service
        loginService.start();
    }

    /**
     * Pokazuje lub ukrywa stan ładowania.
     *
     * @param isLoading true aby pokazać stan ładowania, false aby ukryć
     */
    private void showLoadingState(boolean isLoading) {
        loadingOverlay.setVisible(isLoading);
        loginForm.setDisable(isLoading);

        if (isLoading) {
            loadingLabel.setText("Preparing login...");
        }
    }

    /**
     * Wyświetla alert z podanym tytułem, wiadomością i typem.
     *
     * @param title Tytuł alertu
     * @param message Wiadomość alertu
     * @param alertType Typ alertu
     */
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}