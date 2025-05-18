package com.example.projektzielonifx.notifications;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.projektzielonifx.database.DatabaseConnection;
import com.example.projektzielonifx.models.Notification;

public class NotificationManager {
    public List<Notification> getUnreadNotifications(int userId) {
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
}