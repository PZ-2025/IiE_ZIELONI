package com.example.projektzielonifx.auth;

import com.example.projektzielonifx.database.DatabaseConnection;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Service for migrating existing plain text passwords to hashed passwords
 */
public class PasswordMigrationService extends Service<Boolean> {

    @Override
    protected Task<Boolean> createTask() {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                updateMessage("Checking passwords...");
                updateProgress(0, 100);

                Connection conn = null;
                PreparedStatement selectPs = null;
                PreparedStatement updatePs = null;
                ResultSet rs = null;

                try {
                    conn = DatabaseConnection.getConnection();

                    // First, count total users that need migration
                    PreparedStatement countPs = conn.prepareStatement("SELECT COUNT(*) FROM Users WHERE password_hash NOT LIKE '$2%'");
                    ResultSet countRs = countPs.executeQuery();
                    int totalToMigrate = 0;
                    if (countRs.next()) {
                        totalToMigrate = countRs.getInt(1);
                    }
                    countRs.close();
                    countPs.close();

                    if (totalToMigrate == 0) {
                        updateMessage("No passwords need migration");
                        updateProgress(100, 100);
                        System.out.println("No passwords needed migration.");
                        return true;
                    }

                    // Select all users with plain text passwords
                    selectPs = conn.prepareStatement("SELECT id, password_hash FROM Users");
                    rs = selectPs.executeQuery();

                    // Prepare update statement
                    updatePs = conn.prepareStatement("UPDATE Users SET password_hash = ? WHERE id = ?");

                    int migratedCount = 0;

                    while (rs.next()) {
                        int userId = rs.getInt("id");
                        String currentPassword = rs.getString("password_hash");

                        // Check if password is already hashed
                        if (!currentPassword.startsWith("$2")) {
                            String hashedPassword = SecurePasswordManager.hashPassword(currentPassword);
                            updatePs.setString(1, hashedPassword);
                            updatePs.setInt(2, userId);
                            updatePs.executeUpdate();

                            migratedCount++;
                            System.out.println("Migrated password for user ID: " + userId);

                            // Update progress
                            double progress = (double) migratedCount / totalToMigrate * 100;
                            updateProgress(progress, 100);
                            updateMessage("Migrated " + migratedCount + "/" + totalToMigrate + " passwords");
                        }
                    }

                    updateProgress(100, 100);
                    updateMessage("Migration complete!");

                    // Small delay to show completion
                    Thread.sleep(500);

                    System.out.println("Password migration completed successfully! Migrated " + migratedCount + " passwords.");
                    return true;

                } catch (SQLException e) {
                    System.err.println("Database error during password migration: " + e.getMessage());
                    e.printStackTrace();
                    return false;

                } finally {
                    // Clean up resources
                    try {
                        if (rs != null) rs.close();
                        if (selectPs != null) selectPs.close();
                        if (updatePs != null) updatePs.close();
                        if (conn != null) conn.close();
                    } catch (SQLException e) {
                        System.err.println("Error closing database resources: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        };
    }
}