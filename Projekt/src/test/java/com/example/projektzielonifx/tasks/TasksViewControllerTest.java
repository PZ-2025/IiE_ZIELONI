package com.example.projektzielonifx.tasks;

import com.example.projektzielonifx.database.DBUtil;
import com.example.projektzielonifx.models.TaskModel;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testfx.framework.junit5.ApplicationTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TasksViewControllerTest extends ApplicationTest {

    private TasksViewController controller;

    @Override
    public void start(Stage stage) {
        // Nie tworzymy sceny, testujemy tylko kontroler
    }

    @BeforeEach
    public void setUp() {
        controller = new TasksViewController();

        // Inicjalizacja pól FXML używanych w initializeWithId i testach
        controller.backButton = new Button();
        controller.addButton = new Button();
        controller.titleLabel = new Label();
        controller.scrollPane = new ScrollPane();

        controller.searchField = new TextField();
        controller.priorityFilter = new ComboBox<>();
        controller.statusFilter = new ComboBox<>();
        controller.clearFiltersButton = new Button();

        controller.myTasksGrid = new GridPane();
        controller.teamTasksGrid = new GridPane();
        controller.projectTasksGrid = new GridPane();
        controller.allTasksGrid = new GridPane();

        controller.myTasksSection = new VBox();
        controller.teamTasksSection = new VBox();
        controller.projectTasksSection = new VBox();
        controller.allTasksSection = new VBox();
    }

    @Test
    public void testInitializeWithId_ConfiguresUIAndLoadsTasks_Level1() {
        int userId = 42;

        try (MockedStatic<DBUtil> mockedDBUtil = Mockito.mockStatic(DBUtil.class)) {
            // Przygotuj listę testowych zadań
            List<TaskModel> fakeTasks = Arrays.asList(
                    new TaskModel("1", "Task 1", "Description 1", "niski", "doZrobienia", null, "2024-01-01", "2024-02-01", String.valueOf(userId)),
                    new TaskModel("2", "Task 2", "Description 2", "wysoki", "zrobione", "100%", "2024-01-02", "2024-02-02", "user2")
            );

            mockedDBUtil.when(() -> DBUtil.getLevel(userId)).thenReturn(1);
            mockedDBUtil.when(() -> DBUtil.findTasks(userId)).thenReturn(fakeTasks);

            controller.initializeWithId(userId);

            // Sprawdzenie właściwości ScrollPane
            assertTrue(controller.scrollPane.isFitToWidth());
            assertEquals(ScrollPane.ScrollBarPolicy.NEVER, controller.scrollPane.getHbarPolicy());
            assertEquals(ScrollPane.ScrollBarPolicy.AS_NEEDED, controller.scrollPane.getVbarPolicy());

            // Back button ma przypisane zdarzenie
            assertNotNull(controller.backButton.getOnAction());

            // Add button powinien być niewidoczny dla poziomu 1
            assertFalse(controller.addButton.isVisible());

            // myTasksGrid powinien mieć tyle dzieci ile fakeTasks
            int childrenCount = controller.myTasksGrid.getChildren().size();
            assertEquals(fakeTasks.size(), childrenCount);

            // Każde dziecko to TaskFrame
            controller.myTasksGrid.getChildren().forEach(node -> assertTrue(node instanceof TaskFrame));

            // Sekcje teamTasks i projectTasks oraz allTasks powinny być ukryte
            assertFalse(controller.teamTasksSection.isVisible());
            assertFalse(controller.projectTasksSection.isVisible());
            assertFalse(controller.allTasksSection.isVisible());
        }
    }


    @Test
    public void testApplyFilters_FiltersCorrectly() {
        int userId = 42;

        try (MockedStatic<DBUtil> mockedDBUtil = Mockito.mockStatic(DBUtil.class)) {
            List<TaskModel> fakeTasks = Arrays.asList(
                    new TaskModel(
                            "1",
                            "Task 1",
                            "Description 1",
                            "niski",
                            "doZrobienia",
                            null,
                            "2024-01-01",
                            "2024-02-01",
                            String.valueOf(userId)
                    ),
                    new TaskModel(
                            "2",
                            "Task 2",
                            "Description 2",
                            "wysoki",
                            "zrobione",
                            "100%",
                            "2024-01-02",
                            "2024-02-02",
                            "user2"
                    )
            );

            mockedDBUtil.when(() -> DBUtil.getLevel(userId)).thenReturn(1);
            mockedDBUtil.when(() -> DBUtil.findTasks(userId)).thenReturn(fakeTasks);

            controller.initializeWithId(userId);

            // Ustaw filtr wyszukiwania
            controller.searchField.setText("fix");
            controller.priorityFilter.setValue("Wszystkie");
            controller.statusFilter.setValue("Wszystkie");

            controller.applyFilters();

            // Po filtracji zostaje tylko 1 zadanie (tytuł "Fix bug")
            assertEquals(1, controller.myTasksGrid.getChildren().size());

            // Wyczyść filtry
            controller.clearFilters();

            // Powinno pokazać wszystkie zadania
            assertEquals(fakeTasks.size(), controller.myTasksGrid.getChildren().size());
        }
    }
}
