package com.example.projektzielonifx.database;

import com.example.projektzielonifx.InitializableWithId;
import com.example.projektzielonifx.ReportController;
import com.example.projektzielonifx.auth.SecurePasswordManager;
import com.example.projektzielonifx.models.*;
import com.example.projektzielonifx.settings.ThemeManager;
import com.example.projektzielonifx.tasks.EditTask;
import com.example.projektzielonifx.userstab.AddUser;
import javafx.application.Platform;
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
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.sql.Date;
import java.util.*;

import static com.example.projektzielonifx.auth.SecurePasswordManager.hashPassword;
import static com.example.projektzielonifx.auth.SecurePasswordManager.verifyPassword;

/**
 * Klasa narzędziowa zawierająca metody pomocnicze do operacji bazodanowych
 * i zarządzania interfejsem użytkownika
 * Obsługuje logowanie, zmianę scen i pobieranie danych z bazy.
 */
public class DBUtil {
    /**
     * Weryfikuje dane logowania użytkownika i zmienia scenę na główną stronę aplikacji
     * w przypadku powodzenia logowania.
     *
     * @param event Zdarzenie wywołujące logowanie
     * @param user  Nazwa użytkownika
     * @param pass  Hasło użytkownika
     */

    /**
     * Migrate existing plain text passwords to hashed passwords
     * WARNING: This assumes you can temporarily access plain text passwords
     * If passwords are already in database as plain text, you need to either:
     * 1. Force all users to reset passwords, or
     * 2. Run this migration if you still have access to plain text passwords
     *
     * @return true if migration was successful, false otherwise
     */
    public static boolean migrateExistingPasswords() {
        Connection conn = null;
        PreparedStatement selectPs = null;
        PreparedStatement updatePs = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            // Select all users with plain text passwords
            selectPs = conn.prepareStatement("SELECT id, password_hash FROM Users");
            rs = selectPs.executeQuery();

            // Prepare update statement
            updatePs = conn.prepareStatement("UPDATE Users SET password_hash = ? WHERE id = ?");

            while (rs.next()) {
                int userId = rs.getInt("id");
                String currentPassword = rs.getString("password_hash"); // Currently plain text

                // Check if password is already hashed (bcrypt hashes start with $2a$, $2b$, or $2y$)
                if (!currentPassword.startsWith("$2")) {
                    String hashedPassword = SecurePasswordManager.hashPassword(currentPassword);
                    updatePs.setString(1, hashedPassword);
                    updatePs.setInt(2, userId);
                    updatePs.executeUpdate();

                    System.out.println("Migrated password for user ID: " + userId);
                }
            }

            System.out.println("Password migration completed successfully!");
            return true;

        } catch (SQLException e) {
            System.out.println("Linijka 104");
            System.err.println("Database error during password migration: " + e.getMessage());
            e.printStackTrace();

        } finally {
            // Clean up resources
            try {
                if (rs != null) rs.close();
                if (selectPs != null) selectPs.close();
                if (updatePs != null) updatePs.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.out.println("Linijka 105");
                System.err.println("Error closing database resources: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return false;
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
            stage.setTitle("GreenTask - " + title);
            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
            ThemeManager.getInstance().addManagedScene(scene); // Register the scene
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Changes scene and passes user data for editing
     */
    public static void changeSceneWithUser(Window window, String fxmlPath, String title, int userId, int height, int width, User userToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(DBUtil.class.getResource(fxmlPath));
            Parent root = loader.load();

            // Get the controller and initialize it with user data
            Object controller = loader.getController();
            if (controller instanceof AddUser) {
                ((AddUser) controller).initializeWithUser(userId, userToEdit);
            }

            Stage stage = (Stage) window;
            Scene scene = new Scene(root, width, height);
            ThemeManager.getInstance().addManagedScene(scene); // Register the scene

            stage.setScene(scene);
            stage.setTitle("GreenTask - " + title);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Unable to load scene: " + e.getMessage(), Alert.AlertType.ERROR);
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
            dialogStage.setTitle("GreenTask - " + title);
            dialogStage.initModality(Modality.APPLICATION_MODAL); // Make it modal
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            ThemeManager.getInstance().addManagedScene(scene); // Register the scene

            dialogStage.centerOnScreen();
            dialogStage.showAndWait(); // This blocks until the dialog is closed

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void openSettings(int userId) {
        try {
            FXMLLoader loader = new FXMLLoader(DBUtil.class.getResource("/com/example/projektzielonifx/settings/theme-settings.fxml"));
            Parent root = loader.load();

            // Create a new stage for the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("GreenTask - Settings");
            dialogStage.initModality(Modality.APPLICATION_MODAL); // Make it modal
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            ThemeManager.getInstance().addManagedScene(scene);
            dialogStage.centerOnScreen();
            dialogStage.showAndWait(); // This blocks until the dialog is closed

        } catch (IOException e) {
            e.printStackTrace();
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
                        "password_hash",
                        rs.getString("created_at")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return people;
    }
    public static int getTeamId(int userId) {
        int teamId = 0;
        String query = "SElECT team_id FROM Users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            System.out.println(ps);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                teamId = rs.getInt("team_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
            return teamId;
        }

    public static ObservableList<User> getUsersForTeam(int userId) {
        ObservableList people = FXCollections.observableArrayList();
        int teamId = getTeamId(userId);
        String query = "SELECT * FROM vw_TeamLeader_Squad WHERE team_id = ?;";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, teamId);
            System.out.println(ps);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                people.add(new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("role"),
                        "",
                        rs.getString("hire_date"),
                        rs.getString("login"),
                        "password_hash",
                        ""
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(people.getFirst());

        return people;
    }
    public static int getProjectId(int userId) {
        int projectId = 0;
        String query = "SELECT DISTINCT p.id AS project_id\n" +
                "FROM Users u\n" +
                "LEFT JOIN ProjectTeams pt ON pt.team_id = u.team_id\n" +
                "LEFT JOIN Projects p ON p.id = pt.project_id\n" +
                "WHERE u.id = ? AND p.id IS NOT NULL\n" +
                "UNION\n" +
                "SELECT p.id AS project_id\n" +
                "FROM Projects p\n" +
                "WHERE p.manager_id = ?;";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            System.out.println(ps);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                projectId = rs.getInt("project_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return projectId;
    }

    public static ObservableList<User> getUsersForManager(int userId) {
        ObservableList people = FXCollections.observableArrayList();
        int projectId = getProjectId(userId);
        String query = "SELECT * FROM vw_Manager_Team WHERE manager_id = ?;";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, projectId);
            System.out.println(ps.toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                people.add(new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("role"),
                        rs.getString("team"),
                        rs.getString("hire_date"),
                        "",
                        "password_hash",
                        ""
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

    // find tasks for logged in user
    public static List<TaskModel> findTasks(int userId) {
        List<TaskModel> tasks = new ArrayList<>();
        String sql = "SELECT * FROM vw_TaskAssignmentDetails where user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                String id = rs.getString("task_id");
                String title = rs.getString("title");
                String description = rs.getString("description");
                String priority = rs.getString("priority");
                String status = rs.getString("task_status");
                String progress = rs.getString("task_progress");
                String createdAt = rs.getString("first_assigned_at");
                String deadline = rs.getString("task_deadline");
                String assignedTo = "";

                tasks.add(new TaskModel(id,title,description,priority,status,progress,createdAt,deadline,assignedTo));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return tasks;
    }

    public static List<TaskModel> findTeamTasks(int userId) {
        int teamId = getTeamId(userId);
        List<TaskModel> tasks = new ArrayList<>();
        String sql = "SELECT \n" +
                "    CAST(t.id AS CHAR) AS id,\n" +
                "    t.title,\n" +
                "    t.description,\n" +
                "    t.priority,\n" +
                "    t.status,\n" +
                "    CAST(t.progress AS CHAR) AS progress,\n" +
                "    DATE_FORMAT(t.created_at, '%Y-%m-%d %H:%i:%s') AS created_at,\n" +
                "    DATE_FORMAT(t.deadline, '%Y-%m-%d') AS deadline,\n" +
                "    CONCAT(u.first_name, ' ', u.last_name) AS assigned_to\n" +
                "FROM Tasks t\n" +
                "JOIN TaskAssignments ta ON t.id = ta.task_id\n" +
                "JOIN Users u ON ta.user_id = u.id\n" +
                "WHERE u.team_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                String id = rs.getString("id");
                String title = rs.getString("title");
                String description = rs.getString("description");
                String priority = rs.getString("priority");
                String status = rs.getString("status");
                String progress = rs.getString("progress");
                String createdAt = rs.getString("created_at");
                String deadline = rs.getString("deadline");
                String assignedTo = rs.getString("assigned_to");

                tasks.add(new TaskModel(id,title,description,priority,status,progress,createdAt,deadline,assignedTo));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return tasks;
    }

    public static List<TaskModel> findProjectTasks(int userId) {
        List<TaskModel> tasks = new ArrayList<>();
        String sql = "SELECT \n" +
                "    CAST(t.id AS CHAR) AS id,\n" +
                "    t.title,\n" +
                "    t.description,\n" +
                "    t.priority,\n" +
                "    t.status,\n" +
                "    CAST(t.progress AS CHAR) AS progress,\n" +
                "    DATE_FORMAT(t.created_at, '%Y-%m-%d %H:%i:%s') AS created_at,\n" +
                "    DATE_FORMAT(t.deadline, '%Y-%m-%d') AS deadline,\n" +
                "    CONCAT(u.first_name, ' ', u.last_name) AS assigned_to\n" +
                "FROM Projects p\n" +
                "JOIN ProjectTeams pt ON p.id = pt.project_id\n" +
                "JOIN Teams tm ON pt.team_id = tm.id\n" +
                "JOIN Users u ON tm.id = u.team_id\n" +
                "JOIN TaskAssignments ta ON u.id = ta.user_id\n" +
                "JOIN Tasks t ON ta.task_id = t.id\n" +
                "WHERE p.manager_id = ?; ";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                String id = rs.getString("id");
                String title = rs.getString("title");
                String description = rs.getString("description");
                String priority = rs.getString("priority");
                String status = rs.getString("status");
                String progress = rs.getString("progress");
                String createdAt = rs.getString("created_at");
                String deadline = rs.getString("deadline");
                String assignedTo = rs.getString("assigned_to");

                tasks.add(new TaskModel(id,title,description,priority,status,progress,createdAt,deadline,assignedTo));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return tasks;
    }

    public static List<TaskModel> findAllTasks(int userId) {
        List<TaskModel> tasks = new ArrayList<>();
        String sql = "SELECT \n" +
                "    CAST(t.id AS CHAR) AS id,\n" +
                "    t.title,\n" +
                "    t.description,\n" +
                "    t.priority,\n" +
                "    t.status,\n" +
                "    CAST(t.progress AS CHAR) AS progress,\n" +
                "    DATE_FORMAT(t.created_at, '%Y-%m-%d %H:%i:%s') AS created_at,\n" +
                "    DATE_FORMAT(t.deadline, '%Y-%m-%d') AS deadline,\n" +
                "    CONCAT(u.first_name, ' ', u.last_name) AS assigned_to\n" +
                "FROM Tasks t\n" +
                "JOIN Milestones m ON t.milestone_id = m.id\n" +
                "JOIN Projects p ON m.project_id = p.id\n" +
                "LEFT JOIN TaskAssignments ta ON t.id = ta.task_id\n" +
                "LEFT JOIN Users u ON ta.user_id = u.id\n" +
                "WHERE ta.user_id != ?; ";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                String id = rs.getString("id");
                String title = rs.getString("title");
                String description = rs.getString("description");
                String priority = rs.getString("priority");
                String status = rs.getString("status");
                String progress = rs.getString("progress");
                String createdAt = rs.getString("created_at");
                String deadline = rs.getString("deadline");
                String assignedTo = rs.getString("assigned_to");

                tasks.add(new TaskModel(id,title,description,priority,status,progress,createdAt,deadline,assignedTo));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return tasks;
    }


    // find the task with closest due date
    public static List<TaskModel> findRecentTask(int userId) {
        List<TaskModel> tasks = new ArrayList<>();
        String sql = "SELECT\n" +
                "    t.id AS task_id,\n" +
                "    t.title,\n" +
                "    t.description,\n" +
                "    t.deadline AS task_deadline,\n" +
                "    DATEDIFF(t.deadline, CURDATE()) AS days_remaining,\n" +
                "    t.status AS task_status,\n" +
                "    t.priority,\n" +
                "    CONCAT(u.first_name, ' ', u.last_name) AS assigned_to\n" +
                "FROM Tasks t\n" +
                "INNER JOIN TaskAssignments ta ON t.id = ta.task_id\n" +
                "INNER JOIN Users u ON ta.user_id = u.id\n" +
                "WHERE\n" +
                "    t.deadline IS NOT NULL\n" +
                "    AND t.deadline >= CURDATE()\n" +
                "    AND u.id = ?\n" +
                "ORDER BY\n" +
                "    days_remaining ASC,\n" +
                "    t.id DESC\n" +
                "LIMIT 1;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if(rs.next()) {
                String id = rs.getString("task_id");
                String title = rs.getString("title");
                String description = rs.getString("description");
                String priority = rs.getString("priority");
                String status = rs.getString("task_status");
                String progress = "";
                String createdAt = "";
                String deadline = rs.getString("task_deadline");
                String assignedTo = "";
                tasks.add(new TaskModel(id,title,description,priority,status,progress,createdAt,deadline,assignedTo));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return tasks;
    }

    // find task with the most important priority
    public static List<TaskModel> findImportantTask(int userId) {
        List<TaskModel> tasks = new ArrayList<>();
        String sql = "SELECT\n" +
                "    t.id AS task_id,\n" +
                "    t.title,\n" +
                "    t.description,\n" +
                "    t.deadline AS task_deadline,\n" +
                "    DATEDIFF(t.deadline, CURDATE()) AS days_remaining,\n" +
                "    t.status AS task_status,\n" +
                "    t.priority,\n" +
                "    CONCAT(u.first_name, ' ', u.last_name) AS assigned_to\n" +
                "FROM Tasks t\n" +
                "INNER JOIN TaskAssignments ta ON t.id = ta.task_id\n" +
                "INNER JOIN Users u ON ta.user_id = u.id\n" +
                "WHERE\n" +
                "    t.deadline IS NOT NULL\n" +
                "    AND DATEDIFF(t.deadline, CURDATE()) >= 0  -- deadline >0\n" +
                "    AND t.status != 'zrobione'                 -- tylko wtrakcie\n" +
                "    AND u.id = ?\n" +
                "ORDER BY\n" +
                "    FIELD(t.priority, 'wysoki', 'sredni', 'niski'),\n" +
                "    days_remaining ASC,                            \n" +
                "    t.id DESC                                       \n" +
                "LIMIT 1;";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if(rs.next()) {
                String id = rs.getString("task_id");
                String title = rs.getString("title");
                String description = rs.getString("description");
                String priority = rs.getString("priority");
                String status = rs.getString("task_status");
                String progress = "";
                String createdAt = "";
                String deadline = rs.getString("task_deadline");
                String assignedTo = "";
                tasks.add(new TaskModel(id,title,description,priority,status,progress,createdAt,deadline,assignedTo));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return tasks;
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
    public static ObservableList<String> getTeams() {
        ObservableList<String> teams = FXCollections.observableArrayList();
        String sql = "SELECT name FROM Teams";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                teams.add(rs.getString("name"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return teams;
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

    protected static String untranslateRoleName(String dbRole) {
        switch (dbRole) {
            case "Team Lider":
                return "teamLider";
            case "Projekt Manager":
                return "projektManager";
            case "Pracownik":
                return "pracownik";
            case "Prezes":
                return "prezes";
            default:
                return dbRole;
        }
    }

    public static boolean loginExists(String login, int excludeUserId) {
        String sql = "SELECT COUNT(*) FROM Users WHERE login = ? AND id != ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, login);
            stmt.setInt(2, excludeUserId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public static boolean saveUser(User user) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String newRole = untranslateRoleName(user.getRole());
            String hashedPassword = hashPassword(user.getPassword());
            System.out.println(">>> Rozpoczynam zapis użytkownika...");
            System.out.println("[DEBUG] Dane użytkownika:");
            System.out.println("Imię: " + user.getFirstName());
            System.out.println("Nazwisko: " + user.getLastName());
            System.out.println("Login: " + user.getLogin());
            System.out.println("Data zatrudnienia: " + user.getHireDate());
            System.out.println("Rola (nazwa): " + user.getRole());
            System.out.println("Zespół (nazwa): " + user.getTeam());

            if (user.getRole() == null || user.getTeam() == null) {
                System.err.println("[ERROR] Rola lub zespół jest null. Przerwano zapis.");
                return false;
            }

            String roleQuery = "SELECT id FROM Roles WHERE name = ?";
            PreparedStatement roleStmt = conn.prepareStatement(roleQuery);
            roleStmt.setString(1, newRole);
            ResultSet roleRs = roleStmt.executeQuery();
            if (!roleRs.next()) {
                System.err.println("[ERROR] Rola nie znaleziona w bazie: " + user.getRole());
                return false;
            }
            int roleId = roleRs.getInt("id");
            System.out.println("[DEBUG] ID roli = " + roleId);

            String teamQuery = "SELECT id FROM Teams WHERE name = ?";
            PreparedStatement teamStmt = conn.prepareStatement(teamQuery);
            teamStmt.setString(1, user.getTeam());
            ResultSet teamRs = teamStmt.executeQuery();
            if (!teamRs.next()) {
                System.err.println("[ERROR] Zespół nie znaleziony w bazie: " + user.getTeam());
                return false;
            }
            int teamId = teamRs.getInt("id");
            System.out.println("[DEBUG] ID zespołu = " + teamId);

            String insertSql = "INSERT INTO Users (first_name, last_name, login, password_hash, hire_date, role_id, team_id, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
            PreparedStatement stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, user.getFirstName());
            stmt.setString(2, user.getLastName());
            stmt.setString(3, user.getLogin());
            stmt.setString(4, hashedPassword);
            stmt.setDate(5, Date.valueOf(user.getHireDate()));
            stmt.setInt(6, roleId);
            stmt.setInt(7, teamId);

            int affected = stmt.executeUpdate();
            System.out.println("[DEBUG] Dodano użytkownika, affected rows = " + affected);

            if (affected > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    user.setId(keys.getInt(1));
                    System.out.println("[DEBUG] Nowy ID użytkownika = " + user.getId());
                }
                return true;
            }

            return false;

        } catch (Exception e) {
            System.err.println("[EXCEPTION] Błąd przy dodawaniu użytkownika:");
            e.printStackTrace();
            return false;
        }
    }

    protected static int getRoleId(Connection conn, String roleName) throws SQLException {
        String sql = "SELECT id FROM Roles WHERE name = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, roleName);
        ResultSet rs = stmt.executeQuery();
        if (!rs.next()) throw new SQLException("Brak roli: " + roleName);
        return rs.getInt("id");
    }

    protected static int getTeamId(Connection conn, String teamName) throws SQLException {
        String sql = "SELECT id FROM Teams WHERE name = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, teamName);
        ResultSet rs = stmt.executeQuery();
        if (!rs.next()) throw new SQLException("Brak zespołu: " + teamName);
        return rs.getInt("id");
    }


    public static boolean updateUser(User user) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String hashedPassword = hashPassword(user.getPassword());
            int roleId = getRoleId(conn, untranslateRoleName(user.getRole()));
            int teamId = getTeamId(conn, user.getTeam());

            String updateSql = "UPDATE Users SET first_name = ?, last_name = ?, login = ?, password_hash = ?, " +
                    "hire_date = ?, role_id = ?, team_id = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(updateSql);
            stmt.setString(1, user.getFirstName());
            stmt.setString(2, user.getLastName());
            stmt.setString(3, user.getLogin());
            stmt.setString(4, hashedPassword);
            stmt.setDate(5, Date.valueOf(user.getHireDate()));
            stmt.setInt(6, roleId);
            stmt.setInt(7, teamId);
            stmt.setInt(8, user.getId());

            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
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

    public static List<Notification> getUnreadNotifications(int userId) {
        List<Notification> notifications = new ArrayList<>();
        String query = "SELECT * FROM Notifications WHERE user_id = ? AND is_read = FALSE ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                notifications.add(new Notification(
                        rs.getInt("id"),
                        rs.getString("message"),
                        rs.getString("type"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notifications;
    }

    public static void markAsRead(int notificationId) {
        final String sql = "UPDATE Notifications SET is_read = TRUE WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, notificationId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();                 // produkcyjnie → własny logger
        }
    }

    public static void markAllNotificationsAsRead(int userId) {
        final String sql = "UPDATE Notifications SET is_read = TRUE WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();                 // produkcyjnie → własny logger
        }
    }

    public static ObservableList<Milestone> getAllMilestones() {
        ObservableList<Milestone> milestones = FXCollections.observableArrayList();
        String sql = "SELECT id, name, project_id,deadline FROM Milestones";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Milestone m = new Milestone(rs.getInt("id"), rs.getInt("project_id"), rs.getString("name"), rs.getDate("deadline").toLocalDate());
                milestones.add(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return milestones;
    }

    public static void createTask(Task task,int userId) {
        String sql = "INSERT INTO Tasks (milestone_id, title, description, priority, status, progress, created_at, deadline) " +
                "VALUES (?, ?, ?, ?, ?, ?, NOW(), ?)";
        int taskId;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, task.getMilestoneId());
            stmt.setString(2, task.getTitle());
            stmt.setString(3, task.getDescription());
            stmt.setString(4, task.getPriority().name());
            stmt.setString(5, task.getStatus().name());
            stmt.setInt(6, task.getProgress());
            stmt.setDate(7, Date.valueOf(task.getDeadline()));

            stmt.executeUpdate();
            try (ResultSet gk = stmt.getGeneratedKeys()) {
                gk.next();
                taskId = gk.getInt(1);
            }
            if (task.getAssignedUserId()>0) {
                assignUser(conn, taskId, userId, task.getAssignedUserId());
            }

        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Changes scene and initializes controller with both userId and taskId for editing
     */
    public static void changeSceneWithTaskId(Node node, String fxmlPath, String title, int userId, int taskId, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(DBUtil.class.getResource(fxmlPath));
            Parent root = loader.load();

            // Get the controller and initialize it with taskId for editing
            EditTask controller = loader.getController();
            controller.initializeWithTaskId(userId, taskId);

            Stage stage = (Stage) node.getScene().getWindow();
            stage.setTitle("GreenTask - " + title);
            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            ThemeManager.getInstance().addManagedScene(scene); // Register the scene

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Błąd", "Nie udało się załadować sceny", Alert.AlertType.ERROR);
        }
    }

    /**
     * Overloaded method for creating new tasks (no taskId)
     */
    public static void changeSceneForNewTask(Node node, String fxmlPath, String title, int userId, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(DBUtil.class.getResource(fxmlPath));
            Parent root = loader.load();

            // Get the controller and initialize it without taskId for creating new task
            EditTask controller = loader.getController();
            controller.initializeWithId(userId);

            Stage stage = (Stage) node.getScene().getWindow();
            stage.setTitle("GreenTask - " + title);
            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            ThemeManager.getInstance().addManagedScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Błąd", "Nie udało się załadować sceny", Alert.AlertType.ERROR);
        }
    }

    public static void updateTask(Task task,int userId) {
        String sql = "UPDATE Tasks SET milestone_id = ?, title = ?, description = ?, priority = ?, status = ?, progress = ?, deadline = ? " +
                "WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, task.getMilestoneId());
            stmt.setString(2, task.getTitle());
            stmt.setString(3, task.getDescription());
            stmt.setString(4, task.getPriority().name());
            stmt.setString(5, task.getStatus().name());
            stmt.setInt(6, task.getProgress());
            stmt.setDate(7, Date.valueOf(task.getDeadline()));
            stmt.setInt(8, task.getId());
            System.out.println("To jest sql: "+stmt);
            stmt.executeUpdate();

            if (task.getAssignedUserId()>0 && getLevel(userId) > 1) {
                try (PreparedStatement del = conn.prepareStatement("DELETE FROM TaskAssignments WHERE task_id=?")) {
                    del.setInt(1, task.getId());
                    del.executeUpdate();
                    System.out.println("usunieto taskassignment");
                }
                assignUser(conn, task.getId(), userId, task.getAssignedUserId());
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static Task getTaskById(Integer taskId) {
        String sql = "SELECT * FROM vw_TaskAssignmentDetails where task_id = ?";
        Task task = new Task();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, taskId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                task.setId(rs.getInt("task_id"));
                task.setTitle(rs.getString("title"));
                task.setDescription(rs.getString("description"));
                task.setPriority(Priority.valueOf(rs.getString("priority")));
                task.setStatus(Status.valueOf(rs.getString("task_status")));
                task.setProgress(rs.getInt("task_progress"));
                task.setDeadline(rs.getDate("task_deadline").toLocalDate());
                task.setAssignedUserId(rs.getInt("user_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String sql2 = "SELECT * FROM Tasks where id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql2)) {
            ps.setInt(1, taskId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                task.setMilestoneId(rs.getInt("milestone_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return task;
    }

    /* Assign Task to user */
    /* ------------------- util ------------------- */

    protected static void assignUser(Connection c, int taskId, int assignedBy, int userId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO TaskAssignments(task_id,assigned_by,user_id,assigned_at) VALUES (?,?,?,NOW())")) {
            ps.setInt(1, taskId);
            ps.setInt(2, assignedBy);
            ps.setInt(3, userId);
            ps.executeUpdate();
            System.out.println(ps.toString());
        }
    }

    /* ------------------- DELETE TASK ------------------- */

    public static void deleteTask(int id) {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM Tasks WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* PRIVILEGE */
    public static int getLevel(int userId) {
        String sql = "SELECT r.privilege_level\n" +
                "FROM Users u\n" +
                "JOIN Roles r ON u.role_id = r.id\n" +
                "WHERE u.id = ?;";
        Integer level = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                level = (rs.getInt("privilege_level"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return level;
    }

    public static boolean deleteUser(int id) {
//        String sql = "DELETE FROM Users WHERE id = ?";
//        try (Connection conn = DatabaseConnection.getConnection();
//             PreparedStatement stmt = conn.prepareStatement(sql)) {
//
//            stmt.setInt(1, id);
//            return stmt.executeUpdate() > 0;
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
        System.out.println("usunieto " +id);
        return true;
    }

    /* Zwracanie listy userow zalezne od roli */

    public static Map<String, Integer> loadEmployees(int userId) {
        Map<String, Integer> map = new LinkedHashMap<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "";
            if(getLevel(userId) == 4) {
                sql = "SELECT id, CONCAT(first_name, ' ', last_name) AS name, role FROM vw_Prezes_AllEmployees";
            } else if(getLevel(userId) == 3) {
                sql = "SELECT id, CONCAT(first_name, ' ', last_name) AS name, role FROM vw_Manager_Team WHERE manager_id = ?";
            } else if(getLevel(userId) == 2) {
                sql = "SELECT id, CONCAT(first_name, ' ', last_name) AS name, role FROM vw_TeamLeader_Squad where team_id = ?";
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                if(getLevel(userId) != 4) {
                    stmt.setInt(1, userId);
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
    public void createMilestone(Milestone m) throws SQLException {
        String sql = "INSERT INTO Milestones (project_id,name,description,deadline,progress) VALUES (1,?,?,?,0)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, m.getName());
            ps.setString(2, m.getDescription());
            ps.setDate(3, m.getDeadline()==null? null : Date.valueOf(m.getDeadline()));
            ps.executeUpdate();
        }
    }

}

