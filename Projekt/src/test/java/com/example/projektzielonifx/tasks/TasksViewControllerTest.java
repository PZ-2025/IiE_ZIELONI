package com.example.projektzielonifx.tasks;

import com.example.projektzielonifx.database.DBUtil;
import com.example.projektzielonifx.models.TaskModel;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testfx.framework.junit5.ApplicationTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TasksViewControllerTest extends ApplicationTest {

    private TasksViewController controller;

    @Override
    public void start(Stage stage) throws Exception {
        // Nie tworzymy sceny, bo testujemy tylko kontroler
    }

    @BeforeEach
    public void setUp() {
        controller = new TasksViewController();

        // Inicjalizacja FXML fields ręcznie (możesz to uprościć, jeśli używasz FXMLLoader)
        controller.backButton = new Button();
        controller.titleLabel = new Label();
        controller.scrollPane = new ScrollPane();
        controller.tasksGrid = new GridPane();
    }

    @Test
    public void testInitializeWithId_ConfiguresUIAndLoadsTasks() {
        int userId = 42;

        // Mock statycznej metody DBUtil.findTasks
        try (MockedStatic<DBUtil> mockedDBUtil = Mockito.mockStatic(DBUtil.class)) {
            // Przygotuj listę testowych zadań
            List<TaskModel> fakeTasks = Arrays.asList(
                    new TaskModel("Task 1", "Desc 1", "niski", "Nowy", "2025-05-17"),
                    new TaskModel("Task 2", "Desc 2", "wysoki", "W trakcie", "2025-06-01")
            );

            mockedDBUtil.when(() -> DBUtil.findTasks(userId)).thenReturn(fakeTasks);

            // Wywołaj testowaną metodę
            controller.initializeWithId(userId);

            // Sprawdź ustawienia ScrollPane
            assertTrue(controller.scrollPane.isFitToWidth());
            assertEquals(ScrollPane.ScrollBarPolicy.NEVER, controller.scrollPane.getHbarPolicy());
            assertEquals(ScrollPane.ScrollBarPolicy.AS_NEEDED, controller.scrollPane.getVbarPolicy());

            // Sprawdź, czy przycisk ma ustawione działanie (backButton)
            assertNotNull(controller.backButton.getOnAction());

            // Sprawdź, czy grid zawiera tyle dzieci, ile zadań
            int childrenCount = controller.tasksGrid.getChildren().size();
            assertEquals(fakeTasks.size(), childrenCount);

            // Możesz dodatkowo sprawdzić, czy każde dziecko to TaskFrame
            controller.tasksGrid.getChildren().forEach(node ->
                    assertTrue(node instanceof TaskFrame)
            );
        }
    }
}
