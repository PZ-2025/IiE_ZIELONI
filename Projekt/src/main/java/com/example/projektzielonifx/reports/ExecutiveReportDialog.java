package com.example.projektzielonifx.reports;

import com.example.projektzielonifx.ReportController;
import com.example.projektzielonifx.database.DBUtil;
import com.example.projektzielonifx.database.DatabaseConnection;
import com.raports.raportlibrary.ExecutiveOverviewReportGenerator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

public class ExecutiveReportDialog implements ReportController {
    protected String fileName;
    protected File selectedDirectory;
    protected int userId;

    @FXML
    protected TextField searchField;
    @FXML
    protected ComboBox statusComboBox;
    @FXML
    protected ComboBox managerComboBox;
    @FXML
    protected CheckBox overdueAllCheckBox;
    @FXML
    protected CheckBox overdueTasksCheckBox;
    @FXML
    protected CheckBox overdueMilestonesCheckBox;
    @FXML
    protected TextField minCompletionRateField;
    @FXML
    protected TextField maxCompletionRateField;
    @FXML
    protected ListView listView;
    @FXML
    protected Button okButton;
    @FXML
    protected Button cancelButton;
    // Cache for project data
    protected Map<Integer, Integer> projectManagersCache = new HashMap<>();
    protected Map<Integer, String> projectStatusCache = new HashMap<>();
    protected Map<Integer, Integer> overdueTasksCache = new HashMap<>();
    protected Map<Integer, Integer> overdueMilestonesCache = new HashMap<>();
    protected Map<Integer, Double> taskCompletionRateCache = new HashMap<>();

    /**
     * Overrides the loadProjects method to also populate the projectManagersCache.
     *
     * @return A map of project names to their IDs
     */
    protected Map<String, Integer> loadProjects() {
        Map<String, Integer> map = new LinkedHashMap<>();

        // Clear all caches before populating
        projectManagersCache.clear();
        projectStatusCache.clear();
        overdueTasksCache.clear();
        overdueMilestonesCache.clear();
        taskCompletionRateCache.clear();

        try (Connection conn = DatabaseConnection.getConnection()) {
            // First load basic project data
            try (PreparedStatement stmt = conn.prepareStatement("SELECT id, name, manager_id, status FROM Projects");
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int projectId = rs.getInt("id");
                    map.put(rs.getString("name"), projectId);
                    projectManagersCache.put(projectId, rs.getInt("manager_id"));
                    projectStatusCache.put(projectId, rs.getString("status"));
                }
            }

            // Then load overdue data and task completion rates from the view in a single query
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT project_id, overdue_tasks, overdue_milestones, task_completion_rate FROM vw_ExecutiveOverview");
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int projectId = rs.getInt("project_id");
                    overdueTasksCache.put(projectId, rs.getInt("overdue_tasks"));
                    overdueMilestonesCache.put(projectId, rs.getInt("overdue_milestones"));
                    taskCompletionRateCache.put(projectId, rs.getDouble("task_completion_rate"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return map;
    }

    /**
     * Generates an executive overview report.
     *
     * @param fileName The output file name
     * @param selectedDirectory The output folder
     */

    @Override
    public void initialize(String fileName, File selectedDirectory, int userId) {
        Map<String, Integer> projectMap = loadProjects();
        if (projectMap.isEmpty()) {
            DBUtil.showAlert("Error","No projects", Alert.AlertType.ERROR);
            Stage currentStage = (Stage) cancelButton.getScene().getWindow();
            currentStage.close();
        }

        statusComboBox.getItems().add("Wszystkie");
        statusComboBox.getItems().addAll(DBUtil.loadProjectStatuses());
        statusComboBox.setValue("Wszystkie");

        Map<String, Integer> managers = DBUtil.loadProjectManagers();
        managerComboBox.getItems().add("Wszyscy");
        managerComboBox.getItems().addAll(managers.keySet());
        managerComboBox.setValue("Wszyscy");

// Make checkboxes mutually exclusive
        overdueAllCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                overdueTasksCheckBox.setSelected(false);
                overdueMilestonesCheckBox.setSelected(false);
            }
        });

        overdueTasksCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                overdueAllCheckBox.setSelected(false);
                overdueMilestonesCheckBox.setSelected(false);
            }
        });

        overdueMilestonesCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                overdueAllCheckBox.setSelected(false);
                overdueTasksCheckBox.setSelected(false);
            }
        });

// Only allow numeric input with optional decimal point and validate range 0-100%
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty()) {
                return change;
            }
            try {
                if (newText.matches("^\\d*\\.?\\d*$")) {
                    if (newText.equals(".")) {
                        return change;
                    }
                    double value = Double.parseDouble(newText);
                    if (value >= 0 && value <= 100) {
                        return change;
                    } else {
                        // Don't reject the change, but show an error message
                        TextField field = (TextField) change.getControl();
                        field.setStyle("-fx-border-color: red;");
                        field.setTooltip(new Tooltip("Wartość musi być między 0 a 100%"));
                        // We still return the change to allow typing, but mark it as invalid
                        return change;
                    }
                }
            } catch (NumberFormatException e) {
                // Ignore parsing errors
            }
            return null;
        };

        minCompletionRateField.setTextFormatter(new TextFormatter<>(filter));
        maxCompletionRateField.setTextFormatter(new TextFormatter<>(filter));

// Create observable list and filtered list
        ObservableList<String> items = FXCollections.observableArrayList(projectMap.keySet());
        FilteredList<String> filteredItems = new FilteredList<>(items, p -> true);

        // Set up filtering based on search text, status, manager, and completion rate
        Runnable updateFilter = () -> {
            String searchText = searchField.getText().toLowerCase();
            String selectedStatus = (String) statusComboBox.getValue();
            String selectedManager = (String) managerComboBox.getValue();
            boolean showOverdueAll = overdueAllCheckBox.isSelected();
            boolean showOverdueTasks = overdueTasksCheckBox.isSelected();
            boolean showOverdueMilestones = overdueMilestonesCheckBox.isSelected();

            // Get min and max completion rate values
            Double minCompletionRate = null;
            Double maxCompletionRate = null;

            try {
                if (!minCompletionRateField.getText().isEmpty()) {
                    minCompletionRate = Double.parseDouble(minCompletionRateField.getText());
                }
            } catch (NumberFormatException ex) {
                // Ignore parsing errors
            }

            try {
                if (!maxCompletionRateField.getText().isEmpty()) {
                    maxCompletionRate = Double.parseDouble(maxCompletionRateField.getText());
                }
            } catch (NumberFormatException ex) {
                // Ignore parsing errors
            }

            // Use final variables for lambda
            final Double finalMinCompletionRate = minCompletionRate;
            final Double finalMaxCompletionRate = maxCompletionRate;

            filteredItems.setPredicate(projectName -> {
                // If search text is empty and no filters are applied, show all
                if ((searchText == null || searchText.isEmpty()) &&
                        "Wszystkie".equals(selectedStatus) &&
                        "Wszyscy".equals(selectedManager) &&
                        !showOverdueAll && !showOverdueTasks && !showOverdueMilestones &&
                        finalMinCompletionRate == null && finalMaxCompletionRate == null) {
                    return true;
                }

                Integer projectId = projectMap.get(projectName);

                // Check if the project matches the search text
                boolean matchesSearch = searchText == null || searchText.isEmpty() ||
                        projectName.toLowerCase().contains(searchText);

                // Check if the project matches the selected status using cached data
                boolean matchesStatus = "Wszystkie".equals(selectedStatus);
                if (!matchesStatus && projectId != null) {
                    String projectStatus = projectStatusCache.get(projectId);
                    matchesStatus = selectedStatus.equals(projectStatus);
                }

                // For manager filtering, check if the selected manager is "Wszyscy" (All)
                // If not, check if the project's manager matches the selected manager
                boolean matchesManager = "Wszyscy".equals(selectedManager);
                if (!matchesManager && projectId != null) {
                    Integer projectManagerId = projectManagersCache.get(projectId);
                    Integer selectedManagerId = managers.get(selectedManager);
                    matchesManager = (selectedManagerId == null) ||
                            (projectManagerId == selectedManagerId);
                }

                // Check if the project has overdue tasks or milestones if those filters are applied using cached data
                boolean matchesOverdue = true;
                if (projectId != null && (showOverdueAll || showOverdueTasks || showOverdueMilestones)) {
                    Integer overdueTasks = overdueTasksCache.get(projectId);
                    Integer overdueMilestones = overdueMilestonesCache.get(projectId);

                    // Default to 0 if not in cache
                    overdueTasks = (overdueTasks != null) ? overdueTasks : 0;
                    overdueMilestones = (overdueMilestones != null) ? overdueMilestones : 0;

                    if (showOverdueAll) {
                        // For "all delays", check if either tasks or milestones are overdue
                        if (overdueTasks == 0 && overdueMilestones == 0) {
                            matchesOverdue = false;
                        }
                    } else {
                        if (showOverdueTasks && overdueTasks == 0) {
                            matchesOverdue = false;
                        }

                        if (showOverdueMilestones && overdueMilestones == 0) {
                            matchesOverdue = false;
                        }
                    }
                }

                // Check if the project's completion rate is within the specified range
                boolean matchesCompletionRate = true;
                if (projectId != null && (finalMinCompletionRate != null || finalMaxCompletionRate != null)) {
                    Double completionRate = taskCompletionRateCache.get(projectId);
                    if (completionRate != null) {
                        if (finalMinCompletionRate != null && completionRate < finalMinCompletionRate) {
                            matchesCompletionRate = false;
                        }
                        if (finalMaxCompletionRate != null && completionRate > finalMaxCompletionRate) {
                            matchesCompletionRate = false;
                        }
                    }
                }

                return matchesSearch && matchesStatus && matchesManager && matchesOverdue && matchesCompletionRate;
            });
        };
        searchField.textProperty().addListener((observable, oldValue, newValue) -> updateFilter.run());
        statusComboBox.valueProperty().addListener((observable, oldValue, newValue) -> updateFilter.run());
        managerComboBox.valueProperty().addListener((observable, oldValue, newValue) -> updateFilter.run());
        overdueAllCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> updateFilter.run());
        overdueTasksCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> updateFilter.run());
        overdueMilestonesCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> updateFilter.run());
        minCompletionRateField.textProperty().addListener((observable, oldValue, newValue) -> updateFilter.run());
        maxCompletionRateField.textProperty().addListener((observable, oldValue, newValue) -> updateFilter.run());

        // Set the items to the list view
        listView.setItems(filteredItems);


        okButton.setOnAction(e -> {
            String selectedProject = (String) listView.getSelectionModel().getSelectedItem();
            if (selectedProject != null) {
                String selectedStatus = (String) statusComboBox.getValue();
                String selectedManager = (String) managerComboBox.getValue();

                // Get the project ID from the map using the selected project name
                Integer projectId = projectMap.get(selectedProject);

                // Get the status for this project from the cache
                String projectStatus = projectStatusCache.get(projectId);

                // Get the manager ID for this project from the cache
                Integer managerId = projectManagersCache.get(projectId);

                // Convert status and manager to appropriate values for filtering
                String statusFilter = "Wszystkie".equals(selectedStatus) ? null : selectedStatus;
                Integer managerFilter = "Wszyscy".equals(selectedManager) ? null : managers.get(selectedManager);

                // Get completion rate values
                Double minCompletionRate = null;
                Double maxCompletionRate = null;
                boolean hasValidationError = false;

                try {
                    if (!minCompletionRateField.getText().isEmpty()) {
                        double value = Double.parseDouble(minCompletionRateField.getText());
                        if (value >= 0 && value <= 100) {
                            minCompletionRate = value;
                            minCompletionRateField.setStyle("");
                            minCompletionRateField.setTooltip(null);
                        } else {
                            hasValidationError = true;
                            minCompletionRateField.setStyle("-fx-border-color: red;");
                            minCompletionRateField.setTooltip(new Tooltip("Wartość musi być między 0 a 100%"));
                        }
                    }
                } catch (NumberFormatException ex) {
                    hasValidationError = true;
                    minCompletionRateField.setStyle("-fx-border-color: red;");
                    minCompletionRateField.setTooltip(new Tooltip("Wprowadź poprawną wartość liczbową"));
                }

                try {
                    if (!maxCompletionRateField.getText().isEmpty()) {
                        double value = Double.parseDouble(maxCompletionRateField.getText());
                        if (value >= 0 && value <= 100) {
                            maxCompletionRate = value;
                            maxCompletionRateField.setStyle("");
                            maxCompletionRateField.setTooltip(null);
                        } else {
                            hasValidationError = true;
                            maxCompletionRateField.setStyle("-fx-border-color: red;");
                            maxCompletionRateField.setTooltip(new Tooltip("Wartość musi być między 0 a 100%"));
                        }
                    }
                } catch (NumberFormatException ex) {
                    hasValidationError = true;
                    maxCompletionRateField.setStyle("-fx-border-color: red;");
                    maxCompletionRateField.setTooltip(new Tooltip("Wprowadź poprawną wartość liczbową"));
                }

                // Check if min is greater than max
                if (minCompletionRate != null && maxCompletionRate != null && minCompletionRate > maxCompletionRate) {
                    hasValidationError = true;
                    minCompletionRateField.setStyle("-fx-border-color: red;");
                    maxCompletionRateField.setStyle("-fx-border-color: red;");
                    minCompletionRateField.setTooltip(new Tooltip("Wartość minimalna nie może być większa niż maksymalna"));
                    maxCompletionRateField.setTooltip(new Tooltip("Wartość maksymalna nie może być mniejsza niż minimalna"));
                }

                // If there are validation errors, don't proceed
                if (hasValidationError) {
                    return;
                }

                // Determine which overdue filter to use
                boolean showOverdueTasks = overdueTasksCheckBox.isSelected();
                boolean showOverdueMilestones = overdueMilestonesCheckBox.isSelected();
                boolean showOverdueAll = overdueAllCheckBox.isSelected();

                // If "all delays" is selected, pass that instead of individual filters
                boolean effectiveOverdueTasks = showOverdueAll || showOverdueTasks;
                boolean effectiveOverdueMilestones = showOverdueAll || showOverdueMilestones;


                try {
                    ExecutiveOverviewReportGenerator.generateFilteredReport(
                            projectId,
                            fileName,
                            selectedDirectory,
                            projectStatus,
                            managerId,
                            effectiveOverdueTasks,
                            effectiveOverdueMilestones,
                            minCompletionRate,
                            maxCompletionRate
                    );
                    DBUtil.showAlert("Completed","Generated reports ", Alert.AlertType.INFORMATION);
                    Stage currentStage = (Stage) cancelButton.getScene().getWindow();
                    currentStage.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    DBUtil.showAlert("Error","PDF not created", Alert.AlertType.ERROR);
                }
            }
            Stage currentStage = (Stage) cancelButton.getScene().getWindow();
            currentStage.close();
        });
        cancelButton.setOnAction(e -> {
            Stage currentStage = (Stage) cancelButton.getScene().getWindow();
            currentStage.close();
        });

    }


}
