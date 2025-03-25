package com.example.projektz;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UsersTab extends Application {
    private final String userName;
    public UsersTab(String userName) {
        this.userName = userName;
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        // Przycisk powrotu do HomePage
        Button backButton = new Button("Powrót do HomePage");
        backButton.setOnAction(event -> {
            primaryStage.close(); // Zamyka bieżące okno
            HomePage homePage = new HomePage(userName); // Zakładam, że masz klasę HomePage
            Stage homeStage = new Stage();
            homePage.start(homeStage); // Otwiera nowe okno HomePage
        });
        Button addButton = new Button("Dodaj pracownika");
        addButton.setOnAction(event -> {
            AddUser addUser = new AddUser(); // Zakładam, że masz klasę HomePage
            Stage addStage = new Stage();
            addUser.start(addStage); // Otwiera nowe okno HomePage
        });

        // Kontener na przycisk powrotu (lewa strona)
        VBox leftPanel = new VBox(backButton, addButton);
        leftPanel.setAlignment(Pos.TOP_LEFT);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setSpacing(10);

        // Tabela do wyświetlenia danych z pliku users.txt
        TableView<User> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Kolumny tabeli
        TableColumn<User, String> userNameColumn = new TableColumn<>("User name:");
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<User, String> passColumn = new TableColumn<>("Haslo");
        passColumn.setCellValueFactory(new PropertyValueFactory<>("password"));

        TableColumn<User, String> fullName = new TableColumn<>("Imie i nazwisko");
        fullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        TableColumn<User, String> positionColumn = new TableColumn<>("Stanowisko");
        positionColumn.setCellValueFactory(new PropertyValueFactory<>("position"));

        // Dodanie kolumn do tabeli
        tableView.getColumns().addAll(userNameColumn, passColumn, fullName, positionColumn);

        // Wczytanie danych z pliku users.txt
        List<User> userList = loadUsersFromFile("users.txt");
        tableView.getItems().addAll(userList);

        // Kontener na tabelę (prawa strona)
        VBox rightPanel = new VBox(tableView);
        rightPanel.setPadding(new Insets(10));
        rightPanel.setSpacing(10);

        // Ustawienie lewego i prawego panelu w głównym kontenerze
        root.setLeft(leftPanel);
        root.setCenter(rightPanel);

        // Scena i okno
        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setTitle("Users Tab - " + userName); // Tytuł okna
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    // Metoda do wczytywania danych z pliku users.txt
    private List<User> loadUsersFromFile(String filePath) {
        List<User> userList = new ArrayList<>();

        try ( InputStream inputStream = UsersTab.class.getClassLoader().getResourceAsStream(filePath);
             BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    userList.add(new User(parts[0], parts[1], parts[2], parts[3]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return userList;
    }

    // Klasa reprezentująca użytkownika (dla tabeli)
    public static class User {
        private final String user;
        private final String password;
        private final String fullName;
        private final String position;

        public User(String user, String password, String fullName, String position) {
            this.user = user;
            this.password = password;
            this.fullName = fullName;
            this.position = position;
        }

        public String getUsername() {
            return user;
        }

        public String getPassword() {
            return password;
        }

        public String getFullName() {
            return fullName;
        }

        public String getPosition() {
            return position;
        }
    }

    // Uruchomienie aplikacji
    public static void main(String[] args) {
        launch(args);
    }
}