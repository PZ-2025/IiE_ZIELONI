package com.example.projektzielonifx.home;

import com.example.projektzielonifx.database.DBUtil;
import com.example.projektzielonifx.models.TaskModel;
import com.example.projektzielonifx.tasks.TaskFrame;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Kontroler odpowiedzialny za zarządzanie główną zawartością strony domowej.
 * Wyświetla powitalną wiadomość i zarządza sekcjami zadań.
 * Implementuje interfejs Initializable do inicjalizacji elementów interfejsu.
 */
public class MainContentController implements Initializable {

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
    }

    /**
     * Inicjalizuje kontroler po całkowitym załadowaniu interfejsu.
     * Tworzy i dodaje przykładowe ramki zadań do odpowiednich kontenerów.
     *
     * @param location Lokalizacja używana do rozwiązywania ścieżek względnych, lub null jeśli lokalizacja jest nieznana
     * @param resources Zasoby używane do lokalizacji, lub null jeśli element root nie został zlokalizowany
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Additional initialization if needed
        List<TaskModel> recentTaskData = DBUtil.findRecentTask(userId);
        List<TaskModel> importantTaskData = DBUtil.findImportantTask(userId);

        for (TaskModel task : recentTaskData) {
            TaskFrame recentFrame = new TaskFrame(
                    task.getTitle(),
                    task.getDescription(),
                    task.getPriority(),
                    task.getStatus(),
                    task.getDeadline()
            );
            recentTask.getChildren().add(recentFrame);
       }

        for (TaskModel task : importantTaskData) {
            TaskFrame importantFrame = new TaskFrame(
                    task.getTitle(),
                    task.getDescription(),
                    task.getPriority(),
                    task.getStatus(),
                    task.getDeadline()
            );
            importantTask.getChildren().add(importantFrame);
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
