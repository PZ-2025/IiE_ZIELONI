package com.example.projektz;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TasksPage extends Application {
    private final String userName;
    public TasksPage(String userName) {
        this.userName = userName;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane root = new BorderPane();

        // Przycisk powrotu do HomePage
        Button backButton = new Button("Powrót do HomePage");
        backButton.setOnAction(event -> {
            primaryStage.close(); // Zamyka bieżące okno
            HomePage homePage = new HomePage(userName); // Zakładam, że masz klasę HomePage
            Stage homeStage = new Stage();
            homePage.start(homeStage); // Otwiera nowe okno HomePage
        });

        // Kontener na przycisk powrotu (lewa strona)
        VBox leftPanel = new VBox(backButton);
        leftPanel.setAlignment(Pos.TOP_LEFT);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setSpacing(10);

        // Kontener na taski (prawa strona)
        ScrollPane taskFrame = new ScrollPane();

        VBox rightPanel = new VBox(taskFrame);
        rightPanel.setPadding(new Insets(10));
        rightPanel.setSpacing(10);

        // Ustawienie lewego i prawego panelu w głównym kontenerze
        root.setLeft(leftPanel);
        root.setCenter(rightPanel);
        TaskFrame recentTask = new TaskFrame(
                "UI Design",
                "Create mockups for the dashboard",
                "Low",
                "In Progress"
        );
    taskFrame.setContent(recentTask);

        // Scena i okno
        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setTitle("Users Tab - " + userName); // Tytuł okna
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) { launch(args); }
}
