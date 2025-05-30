package com.example.projektzielonifx.home;

import com.example.projektzielonifx.database.DBUtil;
import com.example.projektzielonifx.models.TaskModel;
import com.example.projektzielonifx.tasks.TaskFrame;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Kontroler odpowiedzialny za zarządzanie główną zawartością strony domowej.
 * Wyświetla powitalną wiadomość i zarządza sekcjami zadań.
 * Implementuje interfejs Initializable do inicjalizacji elementów interfejsu.
 */
public class MainContentController {

    @FXML
    public Button settingsButton;
    /**
     * Identyfikator zalogowanego użytkownika.
     */
    private int userId;

    /**
     * Etykieta wyświetlająca powitanie użytkownika.
     */
    @FXML
    private Label welcomeLabel;

    /**
     * Kontener na główną zawartość strony.
     */
    @FXML
    private VBox mainContent;

    /**
     * Kontener na ostatnie zadania.
     */
    @FXML
    private VBox recentTask;

    /**
     * Kontener na ważne zadania.
     */
    @FXML
    private VBox importantTask;

    /**
     * Inicjalizuje kontroler z identyfikatorem użytkownika.
     * Ustawia tekst powitalny na podstawie danych użytkownika.
     *
     * @param userId Identyfikator zalogowanego użytkownika
     */
    public void initData(int userId) {
        this.userId = userId;
        String username = DBUtil.getUsernameById(userId);
        welcomeLabel.setText("Hello " + username + "!");

        // Additional initialization if needed
        List<TaskModel> recentTaskData = DBUtil.findRecentTask(userId);
        List<TaskModel> importantTaskData = DBUtil.findImportantTask(userId);

        for (TaskModel task : recentTaskData) {
            TaskFrame recentFrame = new TaskFrame(
                    task.getId(),
                    task.getTitle(),
                    task.getDescription(),
                    task.getPriority(),
                    task.getStatus(),
                    task.getDeadline(),
                    task.getAssignedTo(),
                    userId);
            recentTask.getChildren().add(recentFrame);
       }

        for (TaskModel task : importantTaskData) {
            TaskFrame importantFrame = new TaskFrame(
                    task.getId(),
                    task.getTitle(),
                    task.getDescription(),
                    task.getPriority(),
                    task.getStatus(),
                    task.getDeadline(),
                    task.getAssignedTo(),
                    userId);
            importantTask.getChildren().add(importantFrame);
        }
        if(importantTaskData.isEmpty()) {
            Label importantLabel = new Label("No important tasks found");
            importantLabel.setStyle("-fx-font-weight: bold");
            importantTask.getChildren().add(importantLabel);
        }
        if(recentTaskData.isEmpty()) {
            Label recentLabel = new Label("No recent tasks found");
            recentLabel.setStyle("-fx-font-weight: bold");
            recentTask.getChildren().add(recentLabel);
        }


        settingsButton.setOnAction(event -> {
            DBUtil.openSettings(userId);
        });
        }

    ///**
    // * Ładuje zadania z bazy danych i wyświetla je w interfejsie użytkownika.
    // * Metoda zakomentowana - do implementacji w przyszłości.
    // */
    //private void loadTasks() {
    //    // List tasks = fetchTasksFromDatabase(); // Your data fetch logic
    //    //
    //    // for (Task task : tasks) {
    //    //     TaskFrame frame = new TaskFrame(
    //    //         task.getTitle(),
    //    //         task.getDescription(),
    //    //         task.getPriority(),
    //    //         task.getProgress()
    //    //     );
    //    //     mainContentRoot.getChildren().add(frame);
    //    // }
    //}
}
