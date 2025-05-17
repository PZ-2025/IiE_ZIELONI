package com.example.projektzielonifx.database;

import com.example.projektzielonifx.InitializableWithId;
import com.example.projektzielonifx.ReportController;
import com.example.projektzielonifx.models.ProjectModel;
import com.example.projektzielonifx.models.TaskModel;
import com.example.projektzielonifx.models.User;
import com.example.projektzielonifx.tasks.TasksViewController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;

/**
 * Klasa narzędziowa zawierająca metody pomocnicze do operacji bazodanowych
 * i zarządzania interfejsem użytkownika
 * Obsługuje logowanie, zmianę scen i pobieranie danych z bazy.
 */
public class DBUtil {

    /**
     * Zmienia aktualną scenę JavaFX na nową, załadowaną z podanego pliku FXML.
     * Jeśli kontroler nowej sceny implementuje InitializableWithId, przekazuje mu identyfikator użytkownika.
     *
     * @param event    Zdarzenie, które wywołało zmianę sceny
     * @param fxmlFile Ścieżka do pliku FXML definiującego nową scenę
     * @param title    Tytuł nowego okna
     * @param userId   Identyfikator aktualnie zalogowanego użytkownika
     * @param height   Wysokość nowego okna
     * @param width    Szerokość nowego okna
     */
    public static void changeScene(ActionEvent event, String fxmlFile, String title, int userId, double height, double width) {
        try {
            FXMLLoader loader = new FXMLLoader(DBUtil.class.getResource(fxmlFile));
            Parent root = loader.load();

            // Pass the user ID to the controller
            if (loader.getController() instanceof InitializableWithId) {
                ((InitializableWithId) loader.getController()).initializeWithId(userId);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(new Scene(root, width, height));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void openReportDialog(String fxmlFile, String title, String fileName,
                                        File selectedDirectory, int userId) {
        try {
            FXMLLoader loader = new FXMLLoader(DBUtil.class.getResource(fxmlFile));
            Parent root = loader.load();

            // Get the controller and initialize it with the required parameters
            ReportController controller = loader.getController();
            controller.initialize(fileName, selectedDirectory, userId);

            // Create a new stage for the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.APPLICATION_MODAL); // Make it modal
            dialogStage.setScene(new Scene(root));
            dialogStage.centerOnScreen();
            dialogStage.showAndWait(); // This blocks until the dialog is closed

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Weryfikuje dane logowania użytkownika i zmienia scenę na główną stronę aplikacji
     * w przypadku powodzenia logowania.
     *
     * @param event Zdarzenie wywołujące logowanie
     * @param user  Nazwa użytkownika
     * @param pass  Hasło użytkownika
     */
    public static void logInUser(ActionEvent event, String user, String pass) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(
                    "SELECT id, login, password_hash from Users where login = ?");
            ps.setString(1, user);
            rs = ps.executeQuery();

            if (!rs.isBeforeFirst()) {
                showAlert("Error", "User not found", Alert.AlertType.ERROR);
            } else {
                while (rs.next()) {
                    int userId = rs.getInt("id");
                    String retrievedPassword = rs.getString("password_hash");

                    if (retrievedPassword.equals(pass)) {
                        changeScene(event, "/com/example/projektzielonifx/home/HomePage.fxml", "Home Page", userId, 700, 1000);
                        return; // Success - exit method
                    } else {
                        showAlert("Error", "Wrong Password!", Alert.AlertType.ERROR);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Could not connect to database", Alert.AlertType.ERROR);
        } finally {
            // Close resources in reverse order
            try {
                if (rs != null) rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Pobiera imię użytkownika na podstawie jego identyfikatora.
     *
     * @param userId Identyfikator użytkownika
     * @return Imię użytkownika lub "Guest" jeśli użytkownik nie został znaleziony,
     * "Error" w przypadku wystąpienia błędu
     */
    public static String getUsernameById(int userId) {
        String query = "SELECT first_name FROM Users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            return rs.next() ? rs.getString("first_name") : "Guest";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error";
        }
    }

    /**
     * Wyświetla okno dialogowe z alertem.
     *
     * @param title   Tytuł okna alertu
     * @param content Treść komunikatu
     * @param type    Typ alertu (informacja, ostrzeżenie, błąd)
     */
    public static void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.show();
    }

    /**
     * Pobiera listę wszystkich użytkowników z bazy danych.
     *
     * @return ObservableList zawierająca obiekty użytkowników
     */

    public static ObservableList<User> getUsers() {
        ObservableList people = FXCollections.observableArrayList();
        String query = "SELECT * FROM vw_UserDetails";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                people.add(new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("role"),
                        rs.getString("team"),
                        rs.getString("hire_date"),
                        rs.getString("login"),
                        rs.getString("created_at")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return people;
    }

    public static ProjectModel findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM vw_UserCompleteDetails WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new ProjectModel(
                        rs.getInt("user_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("role"),
                        rs.getString("team"),
                        rs.getString("hire_date"),
                        rs.getString("login"),
                        rs.getString("user_created_at"),
                        rs.getString("manager_name"),
                        rs.getString("team_leader_name"),
                        rs.getString("projects_assigned"),
                        rs.getString("milestones_assigned"),
                        rs.getString("tasks_assigned"),
                        rs.getString("total_tasks"),
                        rs.getString("todo"),
                        rs.getString("in_progress"),
                        rs.getString("done"),
                        rs.getString("canceled")
                );
            } else {
                throw new SQLException("Brak danych dla użytkownika " + userId);
            }
        }
    }

    public static List<TaskModel> findTasks(int userId) {
        List<TaskModel> tasks = new ArrayList<>();
        String sql = "SELECT * FROM Tasks";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
//            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String id = rs.getString("id");
                String milestone = rs.getString("milestone_id");
                String title = rs.getString("title");
                String description = rs.getString("description");
                String priority = rs.getString("priority");
                String status = rs.getString("status");
                String progress = rs.getString("progress");
                String createdAt = rs.getString("created_at");
                String deadline = rs.getString("deadline");
                String canceledBy = rs.getString("canceled_by");

                tasks.add(new TaskModel(id,milestone,title,description,priority,status,progress,createdAt,deadline,canceledBy));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return tasks;
    }


    /**
     * Loads all employees from the database.
     *
     * @return A map of employee names to their IDs
     */
    public static Map<String, Integer> loadEmployees() {
        return loadEmployeesByRole(null);
    }

    /**
     * Loads employees with a specific role from the database.
     *
     * @param role The role to filter by, or null for all roles
     * @return A map of employee names to their IDs
     */
    protected static Map<String, Integer> loadEmployeesByRole(String role) {
        Map<String, Integer> map = new LinkedHashMap<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT u.id, CONCAT(u.first_name, ' ', u.last_name) AS name, r.name AS role " +
                    "FROM Users u JOIN Roles r ON u.role_id = r.id";

            if (role != null && !role.isEmpty()) {
                sql += " WHERE r.name = ?";
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                if (role != null && !role.isEmpty()) {
                    stmt.setString(1, role);
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String name = rs.getString("name");
                        String dbRole = rs.getString("role");
                        String translatedRole = translateRoleName(dbRole);
                        map.put(name + " (" + translatedRole + ")", rs.getInt("id"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * Loads all projects from the database.
     *
     * @return A map of project names to their IDs
     */
    public static Map<String, Integer> loadProjects() {
        Map<String, Integer> map = new LinkedHashMap<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, name FROM Projects");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) map.put(rs.getString("name"), rs.getInt("id"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * Loads all distinct project statuses from the database.
     *
     * @return A list of project statuses
     */
    public static List<String> loadProjectStatuses() {
        List<String> statuses = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT status FROM Projects");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                statuses.add(rs.getString("status"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statuses;
    }

    /**
     * Loads all project managers from the database.
     *
     * @return A map of manager names to their IDs
     */
    public static Map<String, Integer> loadProjectManagers() {
        Map<String, Integer> managers = new LinkedHashMap<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT u.id, CONCAT(u.first_name, ' ', u.last_name) AS name " +
                             "FROM Users u " +
                             "JOIN Roles r ON u.role_id = r.id " +
                             "WHERE r.name = 'projektManager'");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                managers.put(rs.getString("name"), rs.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return managers;
    }

    /**
     * Loads all roles from the database and translates them to user-friendly format.
     *
     * @return A list of translated role names
     */
    public static List<String> loadRoles() {
        List<String> roles = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT name FROM Roles");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String dbRole = rs.getString("name");
                String translatedRole = translateRoleName(dbRole);
                roles.add(translatedRole);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roles;
    }

    /**
     * Translates database role names to user-friendly format.
     *
     * @param dbRole The role name from the database
     * @return The translated role name
     */
    protected static String translateRoleName(String dbRole) {
        switch (dbRole) {
            case "teamLider":
                return "Team Lider";
            case "projektManager":
                return "Projekt Manager";
            case "pracownik":
                return "Pracownik";
            case "prezes":
                return "Prezes";
            default:
                return dbRole;
        }
    }
    /**
     * Loads employee performance data from the database.
     *
     * @return A map of user IDs to their completion rates
     */
    public static Map<Integer, Double> loadEmployeePerformanceData() {
        Map<Integer, Double> performanceMap = new HashMap<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT user_id, completion_rate FROM vw_EmployeePerformance");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                performanceMap.put(rs.getInt("user_id"), rs.getDouble("completion_rate"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return performanceMap;
    }
}