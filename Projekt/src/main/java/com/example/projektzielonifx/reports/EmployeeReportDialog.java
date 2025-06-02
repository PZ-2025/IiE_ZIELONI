package com.example.projektzielonifx.reports;

import com.example.projektzielonifx.ReportController;
import com.example.projektzielonifx.database.DBUtil;
import com.raports.raportlibrary.EmployeePerformanceReportGenerator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class EmployeeReportDialog implements ReportController {
    protected String fileName;
    protected File selectedDirectory;
    protected int userId;

    @FXML
    protected TextField searchField;
    @FXML
    protected ListView<CheckBox> listView;
    @FXML
    protected TextField minPerformanceField;
    @FXML
    protected TextField maxPerformanceField;

    @FXML
    protected Button okButton;
    @FXML
    protected Button cancelButton;
    @FXML
    protected Button selectAllButton;
    @FXML
    protected Button clearAllButton;
@FXML
protected HBox roleFilterBox;

    @Override
    public void initialize(String fileName, File selectedDirectory, int userId) {
        this.fileName = fileName;
        this.selectedDirectory = selectedDirectory;
        this.userId = userId;

        Map<String, Integer> employeeMap = DBUtil.loadEmployees(userId);
        if (employeeMap.isEmpty()) {
            DBUtil.showAlert("Error","No employees", Alert.AlertType.ERROR);
        }
        /**
         * Shows a dialog for selecting multiple employees with filtering options.
         *
         * @param title The dialog title
         * @param options The employees to display
         * @param onSelected Callback when employees are selected
         */
        // Load employee performance data
        Map<Integer, Double> employeePerformance = DBUtil.loadEmployeePerformanceData();

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

        minPerformanceField.setTextFormatter(new TextFormatter<>(filter));
        maxPerformanceField.setTextFormatter(new TextFormatter<>(filter));

        // Create role filter checkboxes
        List<String> roles = DBUtil.loadRoles();
        Map<String, CheckBox> roleCheckboxes = new HashMap<>();

        for (String role : roles) {
            CheckBox cb = new CheckBox(role);
            cb.setSelected(true); // All roles selected by default
            cb.getStyleClass().add("font-color"); // Add a dedicated class

            roleCheckboxes.put(role, cb);
            roleFilterBox.getChildren().add(cb);
        }

        // Create observable list and filtered list
        ObservableList<CheckBox> items = FXCollections.observableArrayList();
        FilteredList<CheckBox> filteredItems = new FilteredList<>(items, p -> true);

        // Add all employees to the list
        for (Map.Entry<String, Integer> entry : employeeMap.entrySet()) {
            CheckBox cb = new CheckBox(entry.getKey());
            cb.setUserData(entry.getValue());
            items.add(cb);
        }

        // Set up filtering based on search text, role selection, and performance range
        Runnable updateFilter = () -> {
            String searchText = searchField.getText();

            // Get min and max performance values
            Double minPerformance = null;
            Double maxPerformance = null;

            try {
                if (!minPerformanceField.getText().isEmpty()) {
                    minPerformance = Double.parseDouble(minPerformanceField.getText());
                }
            } catch (NumberFormatException ex) {
                // Ignore parsing errors
            }

            try {
                if (!maxPerformanceField.getText().isEmpty()) {
                    maxPerformance = Double.parseDouble(maxPerformanceField.getText());
                }
            } catch (NumberFormatException ex) {
                // Ignore parsing errors
            }

            // Use final variables for lambda
            final Double finalMinPerformance = minPerformance;
            final Double finalMaxPerformance = maxPerformance;

            filteredItems.setPredicate(checkBox -> {
                String itemText = checkBox.getText().toLowerCase();
                Integer selectedUser = (Integer) checkBox.getUserData();

                // Check if the item matches the search text
                boolean matchesSearch = searchText == null || searchText.isEmpty() ||
                        itemText.contains(searchText.toLowerCase());

                // Check if the item's role is selected
                boolean matchesRole = false;
                for (Map.Entry<String, CheckBox> roleEntry : roleCheckboxes.entrySet()) {
                    if (roleEntry.getValue().isSelected() &&
                            itemText.contains("(" + roleEntry.getKey().toLowerCase() + ")")) {
                        matchesRole = true;
                        break;
                    }
                }

                // If no roles are selected, make the list empty
                if (roleCheckboxes.values().stream().noneMatch(CheckBox::isSelected)) {
                    matchesRole = false;
                }

                // Check if the employee's performance is within the specified range
                boolean matchesPerformance = true;
                if (selectedUser != null && (finalMinPerformance != null || finalMaxPerformance != null)) {
                    Double performance = employeePerformance.get(selectedUser);
                    if (performance != null) {
                        if (finalMinPerformance != null && performance < finalMinPerformance) {
                            matchesPerformance = false;
                        }
                        if (finalMaxPerformance != null && performance > finalMaxPerformance) {
                            matchesPerformance = false;
                        }
                    }
                }

                return matchesSearch && matchesRole && matchesPerformance;
            });
        };
        // Add listeners to search field, role checkboxes, and performance fields
        searchField.textProperty().addListener((observable, oldValue, newValue) -> updateFilter.run());

        for (CheckBox roleCb : roleCheckboxes.values()) {
            roleCb.selectedProperty().addListener((observable, oldValue, newValue) -> updateFilter.run());
        }

        minPerformanceField.textProperty().addListener((observable, oldValue, newValue) -> updateFilter.run());
        maxPerformanceField.textProperty().addListener((observable, oldValue, newValue) -> updateFilter.run());

        // Set the items to the list view
        listView.setItems(filteredItems);

        // Apply initial filter
        updateFilter.run();
        // Set button actions
        selectAllButton.setOnAction(e -> {
            for (CheckBox cb : filteredItems) {
                cb.setSelected(true);
            }
        });

        clearAllButton.setOnAction(e -> {
            for (CheckBox cb : filteredItems) {
                cb.setSelected(false);
            }
        });

        cancelButton.setOnAction(e -> {
            Stage currentStage = (Stage) cancelButton.getScene().getWindow();
            currentStage.close();
        });

        okButton.setOnAction(e -> {
            Map<String, Integer> selectedEmployees = new LinkedHashMap<>();
            for (CheckBox cb : items) {
                if (cb.isSelected()) {
                    selectedEmployees.put(cb.getText(), (Integer) cb.getUserData());
                }
            }

            // Get min and max performance values
            Double minPerformance = null;
            Double maxPerformance = null;
            boolean hasValidationError = false;

            try {
                if (!minPerformanceField.getText().isEmpty()) {
                    double value = Double.parseDouble(minPerformanceField.getText());
                    if (value >= 0 && value <= 100) {
                        minPerformance = value;
                        minPerformanceField.setStyle("");
                        minPerformanceField.setTooltip(null);
                    } else {
                        hasValidationError = true;
                        minPerformanceField.setStyle("-fx-border-color: red;");
                        minPerformanceField.setTooltip(new Tooltip("Wartość musi być między 0 a 100%"));
                    }
                }
            } catch (NumberFormatException ex) {
                hasValidationError = true;
                minPerformanceField.setStyle("-fx-border-color: red;");
                minPerformanceField.setTooltip(new Tooltip("Wprowadź poprawną wartość liczbową"));
            }

            try {
                if (!maxPerformanceField.getText().isEmpty()) {
                    double value = Double.parseDouble(maxPerformanceField.getText());
                    if (value >= 0 && value <= 100) {
                        maxPerformance = value;
                        maxPerformanceField.setStyle("");
                        maxPerformanceField.setTooltip(null);
                    } else {
                        hasValidationError = true;
                        maxPerformanceField.setStyle("-fx-border-color: red;");
                        maxPerformanceField.setTooltip(new Tooltip("Wartość musi być między 0 a 100%"));
                    }
                }
            } catch (NumberFormatException ex) {
                hasValidationError = true;
                maxPerformanceField.setStyle("-fx-border-color: red;");
                maxPerformanceField.setTooltip(new Tooltip("Wprowadź poprawną wartość liczbową"));
            }

            // Check if min is greater than max
            if (minPerformance != null && maxPerformance != null && minPerformance > maxPerformance) {
                hasValidationError = true;
                minPerformanceField.setStyle("-fx-border-color: red;");
                maxPerformanceField.setStyle("-fx-border-color: red;");
                minPerformanceField.setTooltip(new Tooltip("Wartość minimalna nie może być większa niż maksymalna"));
                maxPerformanceField.setTooltip(new Tooltip("Wartość maksymalna nie może być mniejsza niż minimalna"));
            }

            // If there are validation errors, don't proceed
            if (hasValidationError) {
                return;
            }

            try {
                if (selectedEmployees.isEmpty()) {
                    DBUtil.showAlert("Error","Pick Employees", Alert.AlertType.ERROR);
                }
                List<Integer> userIds = selectedEmployees.values().stream().collect(Collectors.toList());
                EmployeePerformanceReportGenerator.generateMultipleEmployeeReport(userIds, fileName, selectedDirectory, minPerformance, maxPerformance);

                String employeeNames = String.join(", ", selectedEmployees.keySet());
                DBUtil.showAlert("Completed","Generated reports for: " +employeeNames, Alert.AlertType.INFORMATION);
                Stage currentStage = (Stage) cancelButton.getScene().getWindow();
                currentStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                DBUtil.showAlert("Error","PDF not created", Alert.AlertType.ERROR);
            }
        });
    }
    /**
     * Waliduje wartości pól min i max performance.
     * Zwraca true jeśli wartości są poprawne (0-100, min <= max).
     * @param minText tekst z pola minPerformanceField
     * @param maxText tekst z pola maxPerformanceField
     * @return true jeśli poprawne, false jeśli błąd walidacji
     */
    public static boolean validatePerformanceFields(String minText, String maxText) {
        Double minPerformance = null;
        Double maxPerformance = null;

        try {
            if (minText != null && !minText.isEmpty()) {
                minPerformance = Double.parseDouble(minText);
                if (minPerformance < 0 || minPerformance > 100) {
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            return false;
        }

        try {
            if (maxText != null && !maxText.isEmpty()) {
                maxPerformance = Double.parseDouble(maxText);
                if (maxPerformance < 0 || maxPerformance > 100) {
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            return false;
        }

        if (minPerformance != null && maxPerformance != null && minPerformance > maxPerformance) {
            return false;
        }

        return true;
    }


}
