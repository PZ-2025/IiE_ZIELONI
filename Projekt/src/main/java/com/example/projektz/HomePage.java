package com.example.projektz;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class HomePage extends Application {

   // Stworzenie zmiennej dla nazwy uzytkownika
    private final String userName;
    // Kontruktor dla dodania nazwy uzytkownika
    public HomePage(String userName) {
        this.userName = userName;
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        // Stworzenie panelu nawigacyjnego
        VBox navPanel = createNavPanel();
        root.setLeft(navPanel);

        // Główny panel
        VBox mainContent = createMainContent();
        root.setCenter(mainContent);

        Scene scene = new Scene(root, 1000, 700);

        // Dodanie styli do okna
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setTitle("Task Management System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createNavPanel() {
        VBox navPanel = new VBox(15);
        navPanel.setPadding(new Insets(20));
        navPanel.setAlignment(Pos.TOP_CENTER);
        navPanel.setPrefWidth(250);
        navPanel.getStyleClass().add("nav-panel"); // Dodanie stylu CSS do panelu nawigacji
//
//        Label navTitle = new Label("NavTab");
//        navTitle.setFont(Font.font("System", FontWeight.BOLD, 20));

        // Nav items
        String[] navItems = {"Tasks", "Reports", "Notifications", "Team Members", "Projects Information"};
        for (String item : navItems) {
            Button navButton = new Button(item);
            navButton.setPrefWidth(200);
            navButton.getStyleClass().add("nav-button"); // Dodanie stylu do przycisku nawigacji

            // Add a divider after each nav button (except the last one)
            if (!item.equals("Projects Information")) {
                Region divider = new Region();
                divider.getStyleClass().add("nav-divider");
                navPanel.getChildren().addAll(navButton, divider);
            } else {
                navPanel.getChildren().add(navButton);
            }

            if(item.equals("Team Members")) {
                Button finalNavButton = navButton;
                navButton.setOnAction(event -> {
                    Stage currentStage = (Stage) finalNavButton.getScene().getWindow();
                    currentStage.close();

                    UsersTab usersTab = new UsersTab(userName);
                    Stage usersStage = new Stage();
                    usersTab.start(usersStage);
                });
            }
        else if (item.equals("Tasks")) {
                navButton.setOnAction(event -> {
            Stage currentStage = (Stage) navButton.getScene().getWindow();
            currentStage.close();

            TasksPage tasksPage = new TasksPage(userName);
            Stage tasksStage = new Stage();
            try {
                tasksPage.start(tasksStage);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });}
        }

        // Spacer, by przycisk LogOut znalazł sie na dole panelu
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Logout button at bottom
        Button logoutButton = new Button("Logout");
        logoutButton.setPrefWidth(200);
        logoutButton.getStyleClass().add("logout-button"); // Dodanie styli do przycisku logout
        navPanel.getChildren().addAll(spacer, logoutButton); // dodanie wszzystkich potomnych elementów do panelu nawigacyjnego

        logoutButton.setOnAction(event -> {
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            currentStage.close();

            // Pass just the full name to HomePage
            LoginWindow loginWindow = new LoginWindow();
            Stage loginStage = new Stage();
            loginWindow.start(loginStage);
        });

        return navPanel;
    }

    private VBox createMainContent() {
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));
        mainContent.getStyleClass().add("main-content"); // Dodawanie stylu do panelu głównego

        HBox topSection = new HBox();
        topSection.setAlignment(Pos.CENTER_LEFT);

        // Użycie przekazanego imienia
        Label welcomeLabel = new Label("Hello " + userName + "!");
        welcomeLabel.getStyleClass().add("welcome-title");

        // Create a spacer to push the button to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button settingsButton = new Button("Settings");
        settingsButton.getStyleClass().add("settings-button"); // dodanie stylu CSS do przycisku
        topSection.getChildren().addAll(welcomeLabel,spacer, settingsButton); // dodanie elementów potomnych do części górnej

        // Kontener na Taski
        HBox taskContainers = new HBox(30);

        // Najwcześniejsze zadanie
        VBox recentTaskSection = new VBox(10);
        Label recentTaskLabel = new Label("Your recent task");
        recentTaskLabel.getStyleClass().add("section-header");
//        recentTaskLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        TaskFrame recentTask = new TaskFrame(
                "UI Design",
                "Create mockups for the dashboard",
                "Low",
                "In Progress"
        ); // stworzenie elemntu zadania za pomocą oddzielnej klasy TaskFrame

        recentTaskSection.getChildren().addAll(recentTaskLabel, recentTask);

        // Important task
        VBox importantTaskSection = new VBox(10);
        Label importantTaskLabel = new Label("Your most important task");
//        importantTaskLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        importantTaskLabel.getStyleClass().add("section-header");

        TaskFrame importantTask = new TaskFrame(
                "Bug Fix",
                "Fix critical authentication issue",
                "High",
                "Not Started"
        );

        importantTaskSection.getChildren().addAll(importantTaskLabel, importantTask);

        taskContainers.getChildren().addAll(recentTaskSection, importantTaskSection);

        mainContent.getChildren().addAll(topSection, taskContainers);



        return mainContent;
    }

    // Main method
    public static void main(String[] args) {
        launch(args);
    }
}
