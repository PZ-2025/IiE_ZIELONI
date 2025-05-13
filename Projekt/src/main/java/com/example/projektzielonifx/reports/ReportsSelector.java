package com.example.projektzielonifx.reports;

import com.example.projektzielonifx.InitializableWithId;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

import static com.example.projektzielonifx.database.DBUtil.changeScene;

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

    }
}
