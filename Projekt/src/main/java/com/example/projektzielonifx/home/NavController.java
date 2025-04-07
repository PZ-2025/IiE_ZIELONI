package com.example.projektzielonifx.home;

import com.example.projektzielonifx.database.DBUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Kontroler odpowiedzialny za obsługę panelu nawigacyjnego aplikacji.
 * Zarządza przyciskami nawigacji i ich akcjami.
 * Implementuje interfejs Initializable do inicjalizacji elementów interfejsu.
 */
public class NavController implements Initializable {

    /**
     * Identyfikator zalogowanego użytkownika.
     */
    private int userId;

    /**
     * Przycisk nawigacyjny do sekcji zadań.
     */
    @FXML
    private Button tasksButton;

    /**
     * Przycisk nawigacyjny do sekcji członków zespołu.
     */
    @FXML
    private Button teamMembersButton;

    /**
     * Przycisk wylogowania z aplikacji.
     */
    @FXML
    private Button logoutButton;

    /**
     * Inicjalizuje kontroler z identyfikatorem użytkownika.
     *
     * @param userId Identyfikator zalogowanego użytkownika
     */
    public void initData(int userId) {
        this.userId = userId;
    }

    /**
     * Inicjalizuje kontroler po całkowitym załadowaniu interfejsu.
     * Ustawia akcje dla przycisków nawigacyjnych.
     *
     * @param location Lokalizacja używana do rozwiązywania ścieżek względnych, lub null jeśli lokalizacja jest nieznana
     * @param resources Zasoby używane do lokalizacji, lub null jeśli element root nie został zlokalizowany
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupButtonActions();
    }

    /**
     * Konfiguruje akcje wykonywane po kliknięciu przycisków nawigacyjnych.
     * Ustawia zachowanie dla przycisków: zadania, członkowie zespołu i wylogowanie.
     */
    private void setupButtonActions() {
        tasksButton.setOnAction(event -> {
            Stage currentStage = (Stage) tasksButton.getScene().getWindow();
            currentStage.close();
            // // Open TasksPage (would need to be converted to FXML too)
            // TasksPage tasksPage = new TasksPage(userName, databaseService);
            // Stage tasksStage = new Stage();
            // tasksPage.start(tasksStage);
        });

        teamMembersButton.setOnAction(event -> {
            DBUtil.changeScene(event, "/com/example/projektzielonifx/userstab/UsersTable.fxml", "Users", userId, 700,1000);
        });

        logoutButton.setOnAction(event -> {
            DBUtil.changeScene(event, "/com/example/projektzielonifx/auth/LoginWindow.fxml", "Log In!", 0, 240,320);
        });
    }
}
