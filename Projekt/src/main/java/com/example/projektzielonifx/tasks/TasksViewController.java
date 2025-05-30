package com.example.projektzielonifx.tasks;

import com.example.projektzielonifx.InitializableWithId;
import com.example.projektzielonifx.database.DBUtil;
import com.example.projektzielonifx.models.TaskModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.projektzielonifx.database.DBUtil.changeScene;
import static com.example.projektzielonifx.database.DBUtil.getLevel;

/**
 * Kontroler widoku zadań z filtrowaniem i wieloma kontenerami.
 * Wyświetla zadania użytkownika, zespołu i projektu w oddzielnych sekcjach.
 */
public class TasksViewController implements InitializableWithId {

    @FXML protected BorderPane mainContainer;
    @FXML protected VBox headerBox;
    @FXML
    Button backButton;
    @FXML protected Label titleLabel;
    @FXML protected ScrollPane scrollPane;
    @FXML protected Button addButton;

    // Filtering controls
    @FXML protected TextField searchField;
    @FXML protected ComboBox<String> priorityFilter;
    @FXML protected ComboBox<String> statusFilter;
    @FXML protected Button clearFiltersButton;

    // Task container sections
    @FXML protected VBox myTasksSection;
    @FXML protected GridPane myTasksGrid;
    @FXML protected VBox teamTasksSection;
    @FXML protected GridPane teamTasksGrid;
    @FXML protected VBox projectTasksSection;
    @FXML protected GridPane projectTasksGrid;
    @FXML protected VBox allTasksSection;
    @FXML protected GridPane allTasksGrid;

    // Number of columns in the grid
    protected final int GRID_COLUMNS = 3;
    protected int userId;
    protected int privilegeLevel;

    // Store original task lists for filtering
    protected List<TaskModel> allMyTasks = new ArrayList<>();
    protected List<TaskModel> allTeamTasks = new ArrayList<>();
    protected List<TaskModel> allProjectTasks = new ArrayList<>();
    protected List<TaskModel> allTasks = new ArrayList<>();

    // Mapping between display values and database values
    protected final Map<String, String> priorityDisplayToDb = new HashMap<>();
    protected final Map<String, String> statusDisplayToDb = new HashMap<>();


    @Override
    public void initializeWithId(int userId) {
        this.userId = userId;
        privilegeLevel = getLevel(userId);

        initializeMappings();
        setupUI();
        setupFilters();
        setupEventHandlers();
        if(privilegeLevel == 1) {
            addButton.setVisible(false);
            addButton.setManaged(false);
        }
        switch (privilegeLevel) {
            case 1:
                loadMyTasks();
                teamTasksSection.setVisible(false);
                teamTasksSection.setManaged(false);
                projectTasksSection.setVisible(false);
                projectTasksSection.setManaged(false);
                allTasksSection.setVisible(false);
                allTasksSection.setManaged(false);
                break;
            case 2:
                loadMyTasks();
                loadTeamTasks();
                projectTasksSection.setVisible(false);
                projectTasksSection.setManaged(false);
                allTasksSection.setVisible(false);
                allTasksSection.setManaged(false);
                break;
            case 3:
                loadMyTasks();
                loadTeamTasks();
                loadProjectTasks();
                allTasksSection.setVisible(false);
                allTasksSection.setManaged(false);
                break;
            case 4:
                loadMyTasks();
                loadAllTasks();
                teamTasksSection.setVisible(false);
                teamTasksSection.setManaged(false);
                projectTasksSection.setVisible(false);
                projectTasksSection.setManaged(false);
                break;
        }
    }

    /**
     * Inicjalizuje mapowania między wartościami wyświetlanymi a wartościami bazy danych
     */
    protected void initializeMappings() {
        // Priority mappings (display to database)
        priorityDisplayToDb.put("Wszystkie", "Wszystkie");
        priorityDisplayToDb.put("Niski", "niski");
        priorityDisplayToDb.put("Sredni", "sredni");
        priorityDisplayToDb.put("Wysoki", "wysoki");

        // Status mappings (display to database)
        statusDisplayToDb.put("Wszystkie", "Wszystkie");
        statusDisplayToDb.put("Do zrobienia", "doZrobienia");
        statusDisplayToDb.put("W trakcie", "wTrakcie");
        statusDisplayToDb.put("Zakończone", "zrobione");
        statusDisplayToDb.put("Wstrzymane", "anulowane");
    }

    /**
     * Konfiguruje interfejs użytkownika
     */
    protected void setupUI() {
        // Configure the scroll pane
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Configure the grids
        setupGrid(myTasksGrid);
        setupGrid(teamTasksGrid);
        setupGrid(projectTasksGrid);
        setupGrid(allTasksGrid);
    }

    /**
     * Konfiguruje pojedynczą siatkę zadań
     */
    protected void setupGrid(GridPane grid) {
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(10, 0, 10, 0));
    }

    /**
     * Konfiguruje filtry
     */
    protected void setupFilters() {
        // Priority filter options
        ObservableList<String> priorityOptions = FXCollections.observableArrayList(
                "Wszystkie", "Niski", "Sredni", "Wysoki"
        );
        priorityFilter.setItems(priorityOptions);
        priorityFilter.setValue("Wszystkie");

        // Status filter options
        ObservableList<String> statusOptions = FXCollections.observableArrayList(
                "Wszystkie", "Do zrobienia", "W trakcie", "Zakończone", "Wstrzymane"
        );
        statusFilter.setItems(statusOptions);
        statusFilter.setValue("Wszystkie");
    }

    /**
     * Konfiguruje obsługę zdarzeń
     */
    protected void setupEventHandlers() {
        // Back button
        backButton.setOnAction(event -> {
            changeScene(event, "/com/example/projektzielonifx/home/HomePage.fxml",
                    "Home Page", userId, 700, 1000);
        });

        // Add button
        addButton.setOnAction(event -> {
            DBUtil.changeSceneForNewTask(addButton, "/com/example/projektzielonifx/tasks/EditTask.fxml",
                    "Dodaj Zadanie", userId, 700, 800);
        });

        // Filter event handlers
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        priorityFilter.setOnAction(event -> applyFilters());
        statusFilter.setOnAction(event -> applyFilters());

        // Clear filters button
        clearFiltersButton.setOnAction(event -> clearFilters());
    }

    /**
     * Ładuje wszystkie zadania z bazy danych
     */
    protected void loadAllTasks() {
        allTasks = DBUtil.findAllTasks(userId);
        displayTasks(allTasks, allTasksGrid);

        // Show all tasks section
        allTasksSection.setVisible(!allTasks.isEmpty());
        allTasksSection.setManaged(!allTasks.isEmpty());
    }

    /**
     * Ładuje zadania użytkownika
     */
    protected void loadMyTasks() {
        allMyTasks = DBUtil.findTasks(userId);
        displayTasks(allMyTasks, myTasksGrid);

        // Hide section if no tasks
        myTasksSection.setVisible(!allMyTasks.isEmpty());
        myTasksSection.setManaged(!allMyTasks.isEmpty());
    }

    /**
     * Ładuje zadania zespołu
     */
    protected void loadTeamTasks() {
        allTeamTasks = DBUtil.findTeamTasks(userId);
        displayTasks(allTeamTasks, teamTasksGrid);

        // Hide section if no tasks
        teamTasksSection.setVisible(!allTeamTasks.isEmpty());
        teamTasksSection.setManaged(!allTeamTasks.isEmpty());
    }

    /**
     * Ładuje zadania projektu
     */
    protected void loadProjectTasks() {
        allProjectTasks = DBUtil.findProjectTasks(userId);
        displayTasks(allProjectTasks, projectTasksGrid);

        // Hide section if no tasks
        projectTasksSection.setVisible(!allProjectTasks.isEmpty());
        projectTasksSection.setManaged(!allProjectTasks.isEmpty());
    }

    /**
     * Wyświetla zadania w podanej siatce
     */
    protected void displayTasks(List<TaskModel> tasks, GridPane grid) {
        // Clear existing grid
        grid.getChildren().clear();

        // Populate the grid with task frames
        int column = 0;
        int row = 0;

        for (TaskModel task : tasks) {
            TaskFrame taskFrame = new TaskFrame(
                    task.getId(),
                    task.getTitle(),
                    task.getDescription(),
                    task.getPriority(),
                    task.getStatus(),
                    task.getDeadline(),
                    task.getAssignedTo(),
                    userId
            );

            // Add the task frame to the grid
            grid.add(taskFrame, column, row);

            // Update column and row indices
            column++;
            if (column >= GRID_COLUMNS) {
                column = 0;
                row++;
            }
        }
    }

    /**
     * Aplikuje filtry do wszystkich zadań
     */
    protected void applyFilters() {
        String searchText = searchField.getText().toLowerCase().trim();
        String selectedPriority = priorityFilter.getValue();
        String selectedStatus = statusFilter.getValue();

        // Only filter and display sections that should be visible based on privilege level
        if (privilegeLevel >= 1 && privilegeLevel <= 4) {
            // Filter and display my tasks
            List<TaskModel> filteredMyTasks = filterTasks(allMyTasks, searchText, selectedPriority, selectedStatus);
            displayTasks(filteredMyTasks, myTasksGrid);
            myTasksSection.setVisible(!filteredMyTasks.isEmpty() && privilegeLevel >= 1);
            myTasksSection.setManaged(!filteredMyTasks.isEmpty() && privilegeLevel >= 1);
        }

        if (privilegeLevel >= 2 && privilegeLevel <= 3) {
            // Filter and display team tasks
            List<TaskModel> filteredTeamTasks = filterTasks(allTeamTasks, searchText, selectedPriority, selectedStatus);
            displayTasks(filteredTeamTasks, teamTasksGrid);
            teamTasksSection.setVisible(!filteredTeamTasks.isEmpty() && privilegeLevel >= 2);
            teamTasksSection.setManaged(!filteredTeamTasks.isEmpty() && privilegeLevel >= 2);
        }

        if (privilegeLevel == 3) {
            // Filter and display project tasks
            List<TaskModel> filteredProjectTasks = filterTasks(allProjectTasks, searchText, selectedPriority, selectedStatus);
            displayTasks(filteredProjectTasks, projectTasksGrid);
            projectTasksSection.setVisible(!filteredProjectTasks.isEmpty() && privilegeLevel == 3);
            projectTasksSection.setManaged(!filteredProjectTasks.isEmpty() && privilegeLevel == 3);
        }

        if (privilegeLevel == 4) {
            // Filter and display all tasks (admin view)
            List<TaskModel> filteredAllTasks = filterTasks(allTasks, searchText, selectedPriority, selectedStatus);
            displayTasks(filteredAllTasks, allTasksGrid);
            allTasksSection.setVisible(!filteredAllTasks.isEmpty());
            allTasksSection.setManaged(!filteredAllTasks.isEmpty());
        }
    }

    /**
     * Filtruje listę zadań według podanych kryteriów
     */
    protected List<TaskModel> filterTasks(List<TaskModel> tasks, String searchText,
                                        String selectedPriority, String selectedStatus) {
        // Convert display values to database values
        String dbPriority = priorityDisplayToDb.get(selectedPriority);
        String dbStatus = statusDisplayToDb.get(selectedStatus);

        return tasks.stream()
                .filter(task -> {
                    // Search filter - improved null safety and case handling
                    boolean matchesSearch = searchText.isEmpty() ||
                            (task.getTitle() != null && task.getTitle().toLowerCase().contains(searchText)) ||
                            (task.getDescription() != null && task.getDescription().toLowerCase().contains(searchText)) ||
                            (task.getAssignedTo() != null && task.getAssignedTo().toLowerCase().contains(searchText));

                    // Priority filter - use database value for comparison
                    boolean matchesPriority = "Wszystkie".equals(dbPriority) ||
                            (task.getPriority() != null && task.getPriority().equals(dbPriority));

                    // Status filter - use database value for comparison
                    boolean matchesStatus = "Wszystkie".equals(dbStatus) ||
                            (task.getStatus() != null && task.getStatus().equals(dbStatus));

                    return matchesSearch && matchesPriority && matchesStatus;
                })
                .collect(Collectors.toList());
    }

    /**
     * Czyści wszystkie filtry
     */
    protected void clearFilters() {
        searchField.clear();
        priorityFilter.setValue("Wszystkie");
        statusFilter.setValue("Wszystkie");

        // Reload and show sections based on privilege level
        if (privilegeLevel >= 1 && privilegeLevel <= 3) {
            displayTasks(allMyTasks, myTasksGrid);
            myTasksSection.setVisible(!allMyTasks.isEmpty() && privilegeLevel >= 1);
            myTasksSection.setManaged(!allMyTasks.isEmpty() && privilegeLevel >= 1);
        }

        if (privilegeLevel >= 2 && privilegeLevel <= 3) {
            displayTasks(allTeamTasks, teamTasksGrid);
            teamTasksSection.setVisible(!allTeamTasks.isEmpty() && privilegeLevel >= 2);
            teamTasksSection.setManaged(!allTeamTasks.isEmpty() && privilegeLevel >= 2);
        }

        if (privilegeLevel == 3) {
            displayTasks(allProjectTasks, projectTasksGrid);
            projectTasksSection.setVisible(!allProjectTasks.isEmpty() && privilegeLevel == 3);
            projectTasksSection.setManaged(!allProjectTasks.isEmpty() && privilegeLevel == 3);
        }

        if (privilegeLevel == 4) {
            displayTasks(allTasks, allTasksGrid);
            allTasksSection.setVisible(!allTasks.isEmpty());
            allTasksSection.setManaged(!allTasks.isEmpty());
        }
    }
}