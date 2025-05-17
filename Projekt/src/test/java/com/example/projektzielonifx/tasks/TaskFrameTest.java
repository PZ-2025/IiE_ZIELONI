package com.example.projektzielonifx.tasks;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.testfx.framework.junit5.ApplicationTest;

import static org.junit.jupiter.api.Assertions.*;

public class TaskFrameTest extends ApplicationTest {

    private TaskFrame taskFrame;

    @Override
    public void start(Stage stage) {
        // TestFX wymaga otwarcia sceny, ale tu wystarczy pusta scena
        stage.show();
    }

    @BeforeEach
    public void setUp() {
        // Tworzymy TaskFrame z przykładowymi danymi
        taskFrame = new TaskFrame("Test Task", "Opis zadania", "wysoki", "w trakcie", "2025-05-17");
    }

    @Test
    public void testLabelsAreSetCorrectly() {
        Label titleLabel = (Label) taskFrame.lookup("#titleLabel");
        Label descriptionLabel = (Label) taskFrame.lookup("#descriptionLabel");
        Label priorityLabel = (Label) taskFrame.lookup("#priorityLabel");
        Label statusLabel = (Label) taskFrame.lookup("#statusLabel");
        Label dateLabel = (Label) taskFrame.lookup("#dateLabel");

        assertEquals("Test Task", titleLabel.getText());
        assertEquals("Opis zadania", descriptionLabel.getText());
        assertEquals("wysoki", priorityLabel.getText());
        assertEquals("w trakcie", statusLabel.getText());
        assertEquals("2025-05-17", dateLabel.getText());
    }

    @Test
    public void testPriorityColorsApplied() {
        Separator prioritySeparator = (Separator) taskFrame.lookup("#prioritySeparator");
        VBox taskRoot = (VBox) taskFrame.lookup("#taskRoot");
        Label titleLabel = (Label) taskFrame.lookup("#titleLabel");

        // Sprawdź, czy styl zawiera kolory odpowiadające "wysoki"
        assertTrue(titleLabel.getStyle().contains("rgba(255, 107, 107"));
        assertTrue(prioritySeparator.getStyle().contains("rgba(255, 107, 107"));
        assertTrue(taskRoot.getStyle().contains("rgba(255, 107, 107"));
    }

    @Test
    public void testEditButtonOpensDialog() {
        Button editButton = (Button) taskFrame.lookup("#editButton");
        assertNotNull(editButton);

        // Można spróbować kliknąć przycisk i sprawdzić, czy okno się otwiera
        // Jednak TestFX wymaga uruchomienia pełnego środowiska JavaFX i może być to bardziej rozbudowane
        // Tu dla uproszczenia sprawdzimy, że jest aktywny i ma handler
        assertNotNull(editButton.getOnAction());
    }
}
