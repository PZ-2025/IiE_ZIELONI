package com.example.projektzielonifx.database;

import com.example.projektzielonifx.InitializableWithId;
import com.example.projektzielonifx.ReportController;
import com.example.projektzielonifx.models.*;
import com.example.projektzielonifx.settings.ThemeManager;
import com.example.projektzielonifx.tasks.EditTask;
import com.example.projektzielonifx.tasks.TasksViewController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.sql.Date;
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
            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
            ThemeManager.getInstance().addManagedScene(scene); // Register the scene
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
            dialogStage.setTitle("Settings");
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
                        "password_hash",
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

                tasks.add(new TaskModel(id,title,description,priority,status,progress,createdAt,deadline));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return tasks;
    }

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

                tasks.add(new TaskModel(id,title,description,priority,status,progress,createdAt,deadline));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return tasks;
    }


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

                tasks.add(new TaskModel(id,title,description,priority,status,progress,createdAt,deadline));
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
            System.out.println(">>> Rozpoczynam zapis użytkownika...");
            System.out.println("[DEBUG] Dane użytkownika:");
            System.out.println("Imię: " + user.getFirstName());
            System.out.println("Nazwisko: " + user.getLastName());
            System.out.println("Login: " + user.getLogin());
            System.out.println("Hasło: " + user.getPassword());
            System.out.println("Data zatrudnienia: " + user.getHireDate());
            System.out.println("Rola (nazwa): " + user.getRole());
            System.out.println("Zespół (nazwa): " + user.getTeam());

            if (user.getRole() == null || user.getTeam() == null) {
                System.err.println("[ERROR] Rola lub zespół jest null. Przerwano zapis.");
                return false;
            }

            String roleQuery = "SELECT id FROM Roles WHERE name = ?";
            PreparedStatement roleStmt = conn.prepareStatement(roleQuery);
            roleStmt.setString(1, user.getRole());
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
            stmt.setString(4, user.getPassword());
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

    private static int getRoleId(Connection conn, String roleName) throws SQLException {
        String sql = "SELECT id FROM Roles WHERE name = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, roleName);
        ResultSet rs = stmt.executeQuery();
        if (!rs.next()) throw new SQLException("Brak roli: " + roleName);
        return rs.getInt("id");
    }

    private static int getTeamId(Connection conn, String teamName) throws SQLException {
        String sql = "SELECT id FROM Teams WHERE name = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, teamName);
        ResultSet rs = stmt.executeQuery();
        if (!rs.next()) throw new SQLException("Brak zespołu: " + teamName);
        return rs.getInt("id");
    }


    public static boolean updateUser(User user) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            int roleId = getRoleId(conn, user.getRole());
            int teamId = getTeamId(conn, user.getTeam());

            String updateSql = "UPDATE Users SET first_name = ?, last_name = ?, login = ?, password_hash = ?, " +
                    "hire_date = ?, role_id = ?, team_id = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(updateSql);
            stmt.setString(1, user.getFirstName());
            stmt.setString(2, user.getLastName());
            stmt.setString(3, user.getLogin());
            stmt.setString(4, user.getPassword());
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

    public static ObservableList<Milestone> getAllMilestones() {
        ObservableList<Milestone> milestones = FXCollections.observableArrayList();
        String sql = "SELECT id, name FROM Milestones";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Milestone m = new Milestone(rs.getInt("id"), rs.getString("name"));
                milestones.add(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return milestones;
    }

    public static void createTask(Task task) {
        String sql = "INSERT INTO Tasks (milestone_id, title, description, priority, status, progress, created_at, deadline) " +
                "VALUES (?, ?, ?, ?, ?, ?, NOW(), ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, task.getMilestoneId());
            stmt.setString(2, task.getTitle());
            stmt.setString(3, task.getDescription());
            stmt.setString(4, task.getPriority().name());
            stmt.setString(5, task.getStatus().name());
            stmt.setInt(6, task.getProgress());
            stmt.setDate(7, Date.valueOf(task.getDeadline()));

            stmt.executeUpdate();
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
            stage.setTitle(title);
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
            stage.setTitle(title);
            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            ThemeManager.getInstance().addManagedScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Błąd", "Nie udało się załadować sceny", Alert.AlertType.ERROR);
        }
    }

    public static void updateTask(Task task) {
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

            stmt.executeUpdate();
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Task getTaskById(Integer taskId) {
        String sql = "SELECT * FROM Tasks WHERE id = ?";
        Task task = new Task();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, taskId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                task.setId(rs.getInt("id"));
                task.setMilestoneId(rs.getInt("milestone_id"));
                task.setTitle(rs.getString("title"));
                task.setDescription(rs.getString("description"));
                task.setPriority(Priority.valueOf(rs.getString("priority")));
                task.setStatus(Status.valueOf(rs.getString("status")));
                task.setProgress(rs.getInt("progress"));
                task.setDeadline(rs.getDate("deadline").toLocalDate());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return task;
    }
}

