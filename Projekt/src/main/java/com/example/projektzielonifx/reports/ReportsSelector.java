package com.example.projektzielonifx.reports;

import com.example.projektzielonifx.InitializableWithId;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import java.io.File;


import static com.example.projektzielonifx.database.DBUtil.changeScene;
import static com.example.projektzielonifx.database.DBUtil.openReportDialog;

public class ReportsSelector implements InitializableWithId {
    @FXML
    private Button backButton;
    @FXML
    private ChoiceBox reportTypeBox;
    private int userId;
    private File selectedDirectory;

    @FXML
    private TextField folderLabel;

    @FXML
    private Button folderButton;

    @FXML
    private Button generateButton;

    @FXML
    private TextField fileNameField;

    @Override
    public void initializeWithId(int userId) {
        this.userId = userId;

        backButton.setOnAction(event -> {
            changeScene(event, "/com/example/projektzielonifx/home/HomePage.fxml", "Home Page", userId, 700, 1000);
            return; // Success - exit method
        });

        reportTypeBox.getItems().addAll(
                "Raport wydajności pracownika",
                "Raport postępu projektu",
                "Raport zarządczy projektu"
        );
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