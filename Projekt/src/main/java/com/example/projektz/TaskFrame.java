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
        this.setStyle("-fx-background-color: rgba(" + backgroundColor + ", 0.3); " + "-fx-background-radius: 15;" +
                "-fx-border-color: rgba(" + backgroundColor + "); " +
                        "-fx-border-radius: 15; " +
                        "-fx-border-width: 5;"
        );

        // Ustawienia Tytułu
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

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
                return "193, 255, 114"; // Jasno zielony dla niskiego priorytetu
            case "medium":
                return "255, 224, 102"; // Zolty dla średniego
            case "high":
                return "255, 107, 107"; // Czerwony dla wysokiego
            default:
                return "240, 240, 240"; // Default Jasno szary
        }
    }
}