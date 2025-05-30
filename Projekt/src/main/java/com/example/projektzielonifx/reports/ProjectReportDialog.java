package com.example.projektzielonifx.reports;

import com.example.projektzielonifx.ReportController;
import com.example.projektzielonifx.database.DBUtil;
import com.example.projektzielonifx.database.DatabaseConnection;
import com.raports.raportlibrary.ProjectProgressReportGenerator;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.example.projektzielonifx.database.DBUtil.loadProjectManagers;
import static com.example.projektzielonifx.database.DBUtil.loadProjectStatuses;

public class ProjectReportDialog implements ReportController {
    private String fileName;
    private File selectedDirectory;
    private int userId;

    private Map<Integer, Integer> projectManagersCache = new HashMap<>();

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> statusComboBox;
    @FXML
    private ComboBox<String> managerComboBox;
    @FXML
    private ListView<CheckBox> listView;
    @FXML
    private Button okButton;
    @FXML
    private Button cancelButton;

    @Override
    public void initialize(String fileName, File selectedDirectory, int userId) {
        this.fileName = fileName;
        this.selectedDirectory = selectedDirectory;
        this.userId = userId;

        Map<String, Integer> projectMap = DBUtil.loadProjects();
        if (projectMap.isEmpty()) {
            DBUtil.showAlert("Error","No employees", Alert.AlertType.ERROR);
        }
        statusComboBox.getItems().add("Wszystkie");
        statusComboBox.getItems().addAll(loadProjectStatuses());
        statusComboBox.setValue("Wszystkie");

        Map<String, Integer> managers = loadProjectManagers();
        managerComboBox.getItems().add("Wszyscy");
        managerComboBox.getItems().addAll(managers.keySet());
        managerComboBox.setValue("Wszyscy");

        // Create observable list and filtered list
        ObservableList<CheckBox> items = FXCollections.observableArrayList();
        FilteredList<CheckBox> filteredItems = new FilteredList<>(items, p -> true);

        // Add all projects to the list
        for (Map.Entry<String, Integer> entry : projectMap.entrySet()) {
            CheckBox cb = new CheckBox(entry.getKey());
            cb.setUserData(entry.getValue());
            items.add(cb);
        }
        // Load project statuses and manager IDs in advance to reduce database queries
        Map<Integer, String> projectStatuses = new HashMap<>();
        projectManagersCache.clear(); // Clear the cache before populating
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, status, manager_id FROM Projects")) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int projectId = rs.getInt("id");
                    projectStatuses.put(projectId, rs.getString("status"));
                    projectManagersCache.put(projectId, rs.getInt("manager_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

// Set up filtering based on search text, status, and manager
        Runnable updateFilter = () -> {
            String searchText = searchField.getText().toLowerCase();
            String selectedStatus = statusComboBox.getValue();
            String selectedManager = managerComboBox.getValue();

            filteredItems.setPredicate(checkBox -> {
                String projectName = checkBox.getText();
                Integer projectId = (Integer) checkBox.getUserData();

                // If search text is empty and "All" is selected for both filters, show all
                if ((searchText == null || searchText.isEmpty()) &&
                        "Wszystkie".equals(selectedStatus) &&
                        "Wszyscy".equals(selectedManager)) {
                    return true;
                }

                // Check if the project matches the search text
                boolean matchesSearch = searchText == null || searchText.isEmpty() ||
                        projectName.toLowerCase().contains(searchText);

                // Check if the project matches the selected status
                boolean matchesStatus = "Wszystkie".equals(selectedStatus) ||
                        selectedStatus.equals(projectStatuses.get(projectId));

                // For manager filtering, check if the selected manager is "Wszyscy" (All)
                // If not, check if the project's manager matches the selected manager
                boolean matchesManager = "Wszyscy".equals(selectedManager);
                if (!matchesManager && projectId != null) {
                    Integer projectManagerId = projectManagersCache.get(projectId);
                    Integer selectedManagerId = managers.get(selectedManager);
                    matchesManager = (selectedManagerId == null) ||
                            (projectManagerId == selectedManagerId);
                }

                return matchesSearch && matchesStatus && matchesManager;
            });
        };

        // Add listeners to search field and combo boxes
        searchField.textProperty().addListener((observable, oldValue, newValue) -> updateFilter.run());
        statusComboBox.valueProperty().addListener((observable, oldValue, newValue) -> updateFilter.run());
        managerComboBox.valueProperty().addListener((observable, oldValue, newValue) -> updateFilter.run());

        // Set the items to the list view
        listView.setItems(filteredItems);

        okButton.setOnAction(e -> {
            Map<String, Integer> selectedProjects = new LinkedHashMap<>();
            for (CheckBox cb : items) {
                if (cb.isSelected()) {
                    selectedProjects.put(cb.getText(), (Integer) cb.getUserData());
                }
            }

            String selectedStatus = statusComboBox.getValue();
            String selectedManager = managerComboBox.getValue();

            // Convert status and manager to appropriate values for filtering
            String statusFilter = "Wszystkie".equals(selectedStatus) ? null : selectedStatus;
            Integer managerFilter = "Wszyscy".equals(selectedManager) ? null : managers.get(selectedManager);

            Integer projectId = projectMap.get(selectedProjects);

            // Get the status for this project from the cache
            String projectStatus = projectStatuses.get(projectId);

            // Get the manager ID for this project from the cache
            Integer managerId = projectManagersCache.get(projectId);

            try {
                if (selectedProjects.isEmpty()) {
                    DBUtil.showAlert("Error","No Projects", Alert.AlertType.ERROR);
                }

                ArrayList<Integer> projectIds = new ArrayList<>(selectedProjects.values());
                ProjectProgressReportGenerator.generateMultipleFilteredReport(projectIds, fileName, selectedDirectory, projectStatus, managerId);

                String projectNames = String.join(", ", selectedProjects.keySet());
                DBUtil.showAlert("Completed","Generated reports for: " +projectNames, Alert.AlertType.INFORMATION);
                Stage currentStage = (Stage) cancelButton.getScene().getWindow();
                currentStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                DBUtil.showAlert("Error","PDF not created", Alert.AlertType.ERROR);
            }

        });

        cancelButton.setOnAction(e -> {
            Stage currentStage = (Stage) cancelButton.getScene().getWindow();
            currentStage.close();
        });
    }
}
