package com.example.projektzielonifx.reports;

import com.example.projektzielonifx.database.DBUtil;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReportsSelectorTest {

    private ReportsSelector reportsSelector;

    @BeforeEach
    public void setUp() {
        reportsSelector = new ReportsSelector();

        // Mockowanie pól FXML
        reportsSelector.backButton = new Button();
        reportsSelector.reportTypeBox = new ChoiceBox<>();
        reportsSelector.folderLabel = new TextField();
        reportsSelector.folderButton = new Button();
    }

    @Test
    public void testInitializeWithId_setsInitialState() {
        int userId = 42;

        // Mockowanie statycznej metody changeScene
        try (MockedStatic<DBUtil> dbUtilStatic = mockStatic(DBUtil.class)) {
            reportsSelector.initializeWithId(userId);

            // Sprawdzenie wyboru w ChoiceBox
            assertEquals(3, reportsSelector.reportTypeBox.getItems().size());
            assertEquals("Raport wydajności pracownika", reportsSelector.reportTypeBox.getValue());

            // Sprawdzenie początkowej ścieżki folderu (Documents w katalogu domowym)
            String expectedPath = new File(System.getProperty("user.home"), "Documents").getAbsolutePath();
            assertEquals(expectedPath, reportsSelector.folderLabel.getText());

            // Sprawdzenie, że userId się ustawiło
            assertEquals(userId, reportsSelector.userId);

            // Test symulacji kliknięcia backButton i sprawdzenie wywołania changeScene
            reportsSelector.backButton.fire();
            dbUtilStatic.verify(() -> DBUtil.changeScene(any(), eq("/com/example/projektzielonifx/home/HomePage.fxml"), eq("Home Page"), eq(userId), eq(700), eq(1000)), times(1));
        }
    }
}
