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
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(loginBtn);
        grid.add(hbBtn, 1, 4);

        //  Akcja logowania
        loginBtn.setOnAction(e -> {
            String username = userTextField.getText(); // pobranie tekstu z pol
            String password = pwField.getText();

// WALIDACJA DANYCH
            if (username.isEmpty() || password.isEmpty()) { // Jeśli któraś z text fieldów jest puste to alert
                showAlert(Alert.AlertType.ERROR, "Błędne dane", "Podaj login i hasło.");
            } else {
                User user = validateLogin(username, password); // wywołaj metodę validate login
                if (user != null) { // jeśli znaleziono użytkownika to zamknij okno i przejdź do Home Page
                    Stage currentStage = (Stage) loginBtn.getScene().getWindow();
                    currentStage.close();

                    // Otworz Strone glowna
                    HomePage homePage = new HomePage(user.fullName);
                    Stage homeStage = new Stage();
                    homePage.start(homeStage);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password."); // Błędny login lub hasło
                }
            }
        });

        // Stworzenie okienka
        Scene scene = new Scene(grid, 400, 275);
        primaryStage.setScene(scene);

        primaryStage.show();
    }



    public static void main(String[] args) {
        launch(args);
    }

    private static User validateLogin(String username, String password) {
        try (InputStream inputStream = LoginWindow.class.getClassLoader().getResourceAsStream(CREDENTIALS_FILE);
             BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Podzielenie danych ze stringu na części (dane szywne w txt)
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    String storedUsername = parts[0].trim();
                    String storedPassword = parts[1].trim();
                    String fullName = parts[2].trim();
                    String position = parts[3].trim();

                    // Check if credentials match
                    if (storedUsername.equals(username) && storedPassword.equals(password)) {
                        return new User(storedUsername, storedPassword, fullName, position);
                    }
                }
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Bład pliku", "Bład przy wczytywaniu pliku: " + e.getMessage()); // blad przy wczytywaniu danych
        }
        return null;
    }

    private static void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    static class User {
        String username;
        String password;
        String fullName;
        String position;

        public User(String username, String password, String fullName, String position) {
            this.username = username;
            this.password = password;
            this.fullName = fullName;
            this.position = position;
        }
    }
}