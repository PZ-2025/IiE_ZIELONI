package com.example.projektzielonifx.reports;

import com.example.projektzielonifx.InitializableWithId;
import com.example.projektzielonifx.database.DBUtil;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


import static com.example.projektzielonifx.database.DBUtil.changeScene;
import static com.example.projektzielonifx.database.DBUtil.openReportDialog;

public class ReportsSelector implements InitializableWithId {
    @FXML
    protected Button backButton;
    @FXML
    protected ChoiceBox reportTypeBox;
    protected int userId;
    protected File selectedDirectory;

    @FXML
    protected TextField folderLabel;

    @FXML
    protected Button folderButton;

    @FXML
    protected Button generateButton;

    @FXML
    protected TextField fileNameField;
    protected int privilege;
    @Override
    public void initializeWithId(int userId) {
        this.userId = userId;
        privilege = DBUtil.getLevel(userId);

        backButton.setOnAction(event -> {
            changeScene(event, "/com/example/projektzielonifx/home/HomePage.fxml", "Home Page", userId, 700, 1000);
            return; // Success - exit method
        });

// Add items based on privilege level (cumulative - higher levels get more options)
        if (privilege >= 2) {
            reportTypeBox.getItems().add("Raport wydajności pracownika");
        }
        if (privilege >= 3) {
            reportTypeBox.getItems().add("Raport postępu projektu");
        }
        if (privilege >= 4) {
            reportTypeBox.getItems().add("Raport zarządczy projektu");
        }

        reportTypeBox.setValue("Raport wydajności pracownika");

        selectedDirectory = new File(System.getProperty("user.home"), "Documents");
        folderLabel.setText(selectedDirectory.getAbsolutePath());

        folderButton.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Wybierz folder zapisu");
            Stage currentStage = (Stage) folderButton.getScene().getWindow();
            File chosen = chooser.showDialog(currentStage);
            if (chosen != null) {
                selectedDirectory = chosen;
                folderLabel.setText(chosen.getAbsolutePath());
            }
        });


        // Setup generate button
        generateButton.setOnAction(e -> {
            String selectedType = (String) reportTypeBox.getValue();
            String fileName = fileNameField.getText().trim();
            if (fileName.isEmpty()) {
                fileName = null;
            }

            if (selectedType.equals("Raport wydajności pracownika")) {
                openReportDialog("/com/example/projektzielonifx/reports/employee_report_dialog.fxml", "Raport wydajności pracownika",
                        fileName, selectedDirectory, userId);
            } else if (selectedType.equals("Raport postępu projektu")) {
                openReportDialog("/com/example/projektzielonifx/reports/project_report.fxml", "Raport postępu projektu",
                        fileName, selectedDirectory, userId);
            } else {
                openReportDialog("/com/example/projektzielonifx/reports/executive_report_dialog.fxml", "Raport wykonawczy",
                        fileName, selectedDirectory, userId);
            }
        });
    }
}