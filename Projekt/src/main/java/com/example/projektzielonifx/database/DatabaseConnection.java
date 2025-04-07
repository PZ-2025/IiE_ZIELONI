package com.example.projektzielonifx.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;

/**
 * Klasa obsługująca połączenie z bazą danych.
 * Odczytuje parametry połączenia z pliku konfiguracyjnego i zapewnia
 * dostęp do bazy danych dla innych komponentów aplikacji.
 */
public class DatabaseConnection {

    /**
     * Tworzy i zwraca obiekt połączenia z bazą danych na podstawie parametrów
     * odczytanych z pliku konfiguracyjnego.
     *
     * @return Obiekt Connection reprezentujący aktywne połączenie z bazą danych
     * @throws SQLException gdy wystąpi błąd podczas nawiązywania połączenia z bazą danych
     * @throws RuntimeException gdy plik konfiguracyjny nie zostanie znaleziony lub
     *                          gdy nie uda się nawiązać połączenia
     */
    public static Connection getConnection() throws SQLException {
        // Use the full package path to your config file
        try (InputStream input = DatabaseConnection.class
                .getResourceAsStream("/com/example/projektzielonifx/config.properties")) {
            if (input == null) {
                throw new RuntimeException("config.properties not found in com/example/projektzielonifx/");
            }

            Properties prop = new Properties();
            prop.load(input);
            String url = prop.getProperty("db.url");
            String user = prop.getProperty("db.username");
            String password = prop.getProperty("db.password");

            return DriverManager.getConnection(url, user, password);
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to establish database connection");
        }
    }
}
