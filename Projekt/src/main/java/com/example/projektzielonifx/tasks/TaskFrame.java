package com.example.projektzielonifx.tasks;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class TaskFrame extends VBox {

    @FXML
    private VBox taskRoot;
    @FXML private Label titleLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label priorityLabel;
    @FXML private Label progressLabel;
    @FXML private Button editButton;

    public TaskFrame(String title, String description, String priority, String progress) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("TaskFrame.fxml"));
        loader.setController(this);

        try {
            VBox root = loader.load();
            this.getChildren().add(taskRoot);

            // Set values
            titleLabel.setText(title);
            descriptionLabel.setText(description);
            priorityLabel.setText(priority);
            progressLabel.setText(progress);

            // Set style based on priority
            String backgroundColor = getPriorityColor(priority);
            taskRoot.setStyle(taskRoot.getStyle() +
                    "-fx-background-color: rgba(" + backgroundColor + ", 0.3); " +
                    "-fx-border-color: rgba(" + backgroundColor + ");");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getPriorityColor(String priority) {
        switch(priority.toLowerCase()) {
            case "low": return "193, 255, 114";
            case "medium": return "255, 224, 102";
            case "high": return "255, 107, 107";
            default: return "240, 240, 240";
        }
    }
}