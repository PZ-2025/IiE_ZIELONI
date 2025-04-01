package com.example.projektz;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginWindow extends Application {

    private static final String CREDENTIALS_FILE = "users.txt";

    @Override
    public void start(Stage primaryStage) {

        // Utworzenie okienka aplikacji
        primaryStage.setTitle("Login Application");

        // Stworzenie panelu gridowego
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER); // centrowanie
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        // Tekst powitalny
        Text sceneTitle = new Text("Welcome");
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(sceneTitle, 0, 0, 2, 1);

        // Pole uzytkownika
        Label userLabel = new Label("Username:");
        grid.add(userLabel, 0, 1);

        TextField userTextField = new TextField();
        userTextField.setPromptText("Enter your username");
        grid.add(userTextField, 1, 1);

        // Pole hasla
        Label pwLabel = new Label("Password:");
        grid.add(pwLabel, 0, 2);

        PasswordField pwField = new PasswordField();
        pwField.setPromptText("Enter your password");
        grid.add(pwField, 1, 2);

        // Przycisk logowania
        Button loginBtn = new Button("Login");
        loginBtn.getStyleClass().add("login-button");

        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(loginBtn);
        grid.add(hbBtn, 1, 4);

        loginBtn.setOnAction(e -> {
            String username = userTextField.getText();
            String password = pwField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Błędne dane", "Podaj login i hasło.");
            } else {
                try {
                    // Direct validation without User object
                    String fullName = validateLogin(username, password);
                    if (fullName != null) {
                        Stage currentStage = (Stage) loginBtn.getScene().getWindow();
                        currentStage.close();

                        // Pass just the full name to HomePage
                        HomePage homePage = new HomePage(fullName);
                        Stage homeStage = new Stage();
                        homePage.start(homeStage);
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Błędne dane", "Błędna nazwa użytkownika lub hasło.");
                    }
                } catch (SQLException ex) {
                    showAlert(Alert.AlertType.ERROR, "Błąd połączenia", "Nie można połączyć się z bazą danych.");
                    ex.printStackTrace();
                }
            }
        });

        // Stworzenie okienka

        Scene scene = new Scene(grid, 400, 275);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        primaryStage.setScene(scene);

        primaryStage.show();
    }

    private String validateLogin(String username, String password) throws SQLException {
        String query =  "SELECT first_name, last_name FROM users WHERE login = ? AND password_hash = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Set parameters (prevents SQL injection)
            stmt.setString(1, username);
            stmt.setString(2, password); // Always hash passwords!

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("first_name") + " " + rs.getString("last_name");
                }
            }
        }
        return null;
    }


    public static void main(String[] args) {
        launch(args);
    }
    

    private static void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}