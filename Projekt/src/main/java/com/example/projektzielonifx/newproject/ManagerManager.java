package com.example.projektzielonifx.newproject;
import com.example.projektzielonifx.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ManagerManager {

    /** Zwraca wszystkich użytkowników, których rola = 'projektManager'. */
    public List<Manager> getAll() {
        String sql = """
            SELECT u.id,
                   CONCAT(u.first_name, ' ', u.last_name) AS full_name
            FROM   Users  u
            JOIN   Roles  r ON u.role_id = r.id
            WHERE  r.name = 'projektManager'
            ORDER  BY full_name
            """;

        List<Manager> list = new ArrayList<>();

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement st = c.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {

            while (rs.next()) {
                list.add(new Manager(
                        rs.getInt("id"),
                        rs.getString("full_name")));
            }
        } catch (SQLException e) { e.printStackTrace(); }

        return list;
    }
}
