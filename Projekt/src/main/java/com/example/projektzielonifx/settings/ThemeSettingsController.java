package com.example.projektzielonifx.settings;

import javafx.fxml.FXML;
import javafx.fxml.Initializable; // Use standard JavaFX Initializable
import javafx.scene.control.Alert;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;

// Remove "InitializableWithId" and implement standard Initializable
public class ThemeSettingsController implements Initializable {
    @FXML
    protected ToggleButton darkModeToggle;

    protected ThemeManager themeManager;
    protected boolean hasChanges = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize ThemeManager here
        themeManager = ThemeManager.getInstance();
        darkModeToggle.setSelected(themeManager.isDarkMode());
    }

    @FXML
    protected void onDarkModeToggle() {
        hasChanges = true;
    }

    @FXML
    protected void onApplyChanges() {
        if (hasChanges) {
            boolean newDarkModeState = darkModeToggle.isSelected();
            themeManager.setTheme(newDarkModeState); // No NPE now

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Theme Changed");
            alert.setHeaderText(null);
            alert.setContentText("Theme updated to " + (newDarkModeState ? "Dark Mode" : "Light Mode"));
            alert.showAndWait();
            closeWindow();
        }
    }

    protected void closeWindow() {
        Stage stage = (Stage) darkModeToggle.getScene().getWindow();
        stage.close();
    }
}