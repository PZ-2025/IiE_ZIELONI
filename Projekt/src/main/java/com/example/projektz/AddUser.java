package com.example.projektz;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class AddUser extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Główny kontener
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(10);
        grid.setHgap(10);

        // Pola formularza
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        GridPane.setConstraints(usernameLabel, 0, 0);
        GridPane.setConstraints(usernameField, 1, 0);

        Label passwordLabel = new Label("Password:");
        TextField passwordField = new TextField();
        GridPane.setConstraints(passwordLabel, 0, 1);
        GridPane.setConstraints(passwordField, 1, 1);

        Label fullNameLabel = new Label("Full Name:");
        TextField fullNameField = new TextField();
        GridPane.setConstraints(fullNameLabel, 0, 2);
        GridPane.setConstraints(fullNameField, 1, 2);

        Label positionLabel = new Label("Position:");
        ComboBox<String> positionComboBox = new ComboBox<>();
        positionComboBox.getItems().addAll("Pracownik", "Team Lider", "Project Manager", "Prezes");
        positionComboBox.setValue("Pracownik"); // Domyślna wartość
        GridPane.setConstraints(positionLabel, 0, 3);
        GridPane.setConstraints(positionComboBox, 1, 3);

        // Przycisk "Dodaj użytkownika"
        Button addButton = new Button("Dodaj użytkownika");
        GridPane.setConstraints(addButton, 1, 4);

        // Obsługa przycisku
        addButton.setOnAction(event -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            String fullName = fullNameField.getText();
            String position = positionComboBox.getValue();

            if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                showAlert("Błąd", "Wszystkie pola muszą być wypełnione!");
                return;
            }

            // Zapisz dane

            System.out.println("Username " + username + " password " + password + " fullName " + fullName + " position " + position);

            // Zamknij okno
            primaryStage.close();
        });

        // Dodanie elementów do kontenera
        grid.getChildren().addAll(usernameLabel, usernameField, passwordLabel, passwordField,
                fullNameLabel, fullNameField, positionLabel, positionComboBox, addButton);

        // Scena i okno
        Scene scene = new Scene(grid, 300, 200);
        primaryStage.setTitle("Dodaj użytkownika");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Metoda do wyświetlania alertów
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Uruchomienie aplikacji
    public static void main(String[] args) {
        launch(args);
    }
}