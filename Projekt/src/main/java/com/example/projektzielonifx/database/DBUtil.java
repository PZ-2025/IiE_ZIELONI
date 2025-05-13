package com.example.projektzielonifx.database;

import com.example.projektzielonifx.InitializableWithId;
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
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
    private static void showAlert(String title, String content, Alert.AlertType type) {
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
}