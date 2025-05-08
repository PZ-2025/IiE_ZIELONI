package com.example.projektzielonifx.tasks;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class EditTask {

    @FXML
    private TextField titleField;
    // Add methods to get/set field values


    public String getTaskName() {
        return titleField.getText();
    }
}
