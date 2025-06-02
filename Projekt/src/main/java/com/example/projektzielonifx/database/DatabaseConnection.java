package com.example.projektzielonifx.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;

public class DatabaseConnection {

    public static Connection getConnection() throws SQLException {
        try (InputStream input = DatabaseConnection.class
                .getResourceAsStream("/config.properties")) {

            if (input == null) {
                throw new RuntimeException("config.properties not found in resources/");
            }

            Properties prop = new Properties();
            prop.load(input);

            String url = prop.getProperty("db.url");
            String user = prop.getProperty("db.username");
            String password = prop.getProperty("db.password");

            // Debugging - usuń po testach
            System.out.println("🔗 Connecting to: " + url);
            System.out.println("👤 User: " + user);

            return DriverManager.getConnection(url, user, password);

        } catch (IOException | SQLException e) {
            System.out.println("❌ Database connection failed: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to establish database connection");
        }
    }
}
