package com.example.projektzielonifx.database;

import com.example.projektzielonifx.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseTest {
    public static void main(String[] args) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            System.out.println("✅ Połączenie z bazą danych udane!");
            conn.close();
        } catch (Exception e) {
            System.out.println("❌ Błąd połączenia: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
