package com.example.projektzielonifx.tasks;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class EditTask {

    @FXML
    private TextField titleField;

    @FXML
    private TextArea descriptionField; // jeśli masz opis zadania

    // Getter do tytułu zadania
    public String getTaskName() {
        return titleField.getText();
    }

    // Setter do tytułu zadania
    public void setTaskName(String name) {
        titleField.setText(name);
    }

    // Getter do opisu zadania (jeśli jest)
    public String getDescription() {
        return descriptionField != null ? descriptionField.getText() : null;
    }

    // Setter do opisu zadania (jeśli jest)
    public void setDescription(String description) {
        if (descriptionField != null) {
            descriptionField.setText(description);
        }
    }
}
