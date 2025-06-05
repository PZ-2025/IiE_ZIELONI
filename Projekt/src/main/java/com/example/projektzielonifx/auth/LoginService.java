package com.example.projektzielonifx.auth;

import com.example.projektzielonifx.database.DBUtil;
import com.example.projektzielonifx.database.DatabaseConnection;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Usługa do obsługi logowania użytkownika
 */
public class LoginService extends Service<Void> {

    private final String username;
    private final String password;
    private final ActionEvent event;

    public LoginService(String username, String password, ActionEvent event) {
        this.username = username;
        this.password = password;
        this.event = event;
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                String query = "SELECT id, login, password_hash FROM Users WHERE login = ?";

                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement(query)) {

                    ps.setString(1, username);
                    ResultSet rs = ps.executeQuery();

                    if (!rs.isBeforeFirst()) {
                        Platform.runLater(() ->
                                showAlert("Error", "User not found", Alert.AlertType.ERROR)
                        );
                    } else {
                        while (rs.next()) {
                            int userId = rs.getInt("id");
                            String storedPasswordHash = rs.getString("password_hash");

                            // Use bcrypt to verify the password
                            if (SecurePasswordManager.verifyPassword(password, storedPasswordHash)) {
                                // Scene change must happen on JavaFX Application Thread
                                Platform.runLater(() -> {
                                    try {
                                        DBUtil.changeScene(event, "/com/example/projektzielonifx/home/HomePage.fxml",
                                                "Home Page", userId, 700, 1000);
                                    } catch (Exception e) {
                                        showAlert("Error", "Failed to navigate to home page", Alert.AlertType.ERROR);
                                    }
                                });
                                return null; // Success - exit method
                            } else {
                                Platform.runLater(() ->
                                        showAlert("Error", "Wrong Password!", Alert.AlertType.ERROR)
                                );
                            }
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    Platform.runLater(() ->
                            showAlert("Database Error", "Could not connect to database", Alert.AlertType.ERROR)
                    );
                }
                return null;
            }
        };
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