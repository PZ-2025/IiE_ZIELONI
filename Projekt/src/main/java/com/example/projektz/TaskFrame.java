package com.example.projektz;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class TaskFrame extends VBox {

    public TaskFrame(String title, String description, String priority, String progress) {
        super(10);
        this.setPadding(new Insets(15));
        this.setPrefSize(300, 220);

        // otrzymywanie koloru tła ze względu na priorytet zadania
        String backgroundColor = getPriorityColor(priority);
        this.setStyle("-fx-background-color: " + backgroundColor + "; -fx-background-radius: 5;");

        // Ustawienia Tytułu
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        // Opis
        Label descLabel = new Label("Description:");
        descLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        Label descText = new Label(description);
        descText.setWrapText(true);

        // Priorytet
        HBox priorityBox = new HBox(5);
        Label priorityLabel = new Label("Priority:");
        priorityLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        Label priorityValue = new Label(priority);
        priorityBox.getChildren().addAll(priorityLabel, priorityValue);

        // Progres
        HBox progressBox = new HBox(5);
        Label progressLabel = new Label("Progress:");
        progressLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        Label progressValue = new Label(progress);
        progressBox.getChildren().addAll(progressLabel, progressValue);

        // Przycisk do edycji
        Button editButton = new Button("Edit Button");
        editButton.getStyleClass().add("edit-button"); // Apply CSS class
        editButton.setPrefWidth(150);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        this.getChildren().addAll(
                titleLabel,
                descLabel,
                descText,
                priorityBox,
                progressBox,
                spacer,
                editButton
        );

        // Wycentrowanie przycisku edycji
        this.setAlignment(Pos.CENTER);
    }

    // Metoda zwracająca kolor na bazie priorytetu
    private String getPriorityColor(String priority) {
        switch(priority.toLowerCase()) {
            case "low":
                return "#c1ff72"; // Jasno zielony dla niskiego priorytetu
            case "medium":
                return "#ffe066"; // Zolty dla średniego
            case "high":
                return "#ff6b6b"; // Czerwony dla wysokiego
            default:
                return "#f0f0f0"; // Default Jasno szary
        }
    }
}