package com.example.projektzielonifx.database;


import com.example.projektzielonifx.InitializableWithId;
import com.example.projektzielonifx.models.User;
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

public class DBUtil {

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
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (ps != null) ps.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }


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
private static void showAlert(String title, String content, Alert.AlertType type) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setContentText(content);
    alert.show();
}

    public static ObservableList<User> getUsers() {
        System.out.println("Do you even get called o_o");
        ObservableList<User> people = FXCollections.observableArrayList();
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

        } catch (SQLException e) { e.printStackTrace(); }

        return people;
    }
}
