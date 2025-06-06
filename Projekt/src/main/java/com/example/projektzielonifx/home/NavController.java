package com.example.projektzielonifx.home;

import com.example.projektzielonifx.database.DBUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;

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
    protected int userId;

    /**
     * Przycisk nawigacyjny do sekcji zadań.
     */
    @FXML
    protected Button tasksButton;

    /**
     * Przycisk nawigacyjny do sekcji członków zespołu.
     */
    @FXML
    protected Button teamMembersButton;

    /**
     * Przycisk wylogowania z aplikacji.
     */
    @FXML
    protected Button logoutButton;

    @FXML
    protected Button projectsInfoButton;
    @FXML
    protected Button notificationsButton;
    @FXML
    protected Button reportsButton;
    @FXML
    protected Region reportsButtonDivider;
    @FXML
    protected Button newProjectButton;
    @FXML
    protected Region newProjectDivider;
    protected int roleLevel;
    /**
     * Inicjalizuje kontroler z identyfikatorem użytkownika.
     *
     * @param userId Identyfikator zalogowanego użytkownika
     */
    public void initData(int userId) {
        this.userId = userId;
        roleLevel = DBUtil.getLevel(userId);

        projectsInfoButton.setVisible(false);
        projectsInfoButton.setManaged(false);
        newProjectDivider.setVisible(false);
        newProjectDivider.setManaged(false);

        if(roleLevel == 1) {
            reportsButton.setVisible(false);
            reportsButton.setManaged(false);
            reportsButtonDivider.setVisible(false);
            reportsButtonDivider.setManaged(false);

        }

        if(roleLevel != 4) {
            newProjectButton.setVisible(false);
            newProjectButton.setManaged(false);
            projectsInfoButton.setVisible(true);
            projectsInfoButton.setManaged(true);
        }

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
    protected void setupButtonActions() {
        tasksButton.setOnAction(event -> {
            DBUtil.changeScene(event, "/com/example/projektzielonifx/tasks/TasksView.fxml",
                    "Tasks List", userId, 700,1000);
        });

        teamMembersButton.setOnAction(event -> {
            DBUtil.changeScene(event, "/com/example/projektzielonifx/userstab/UsersTable.fxml",
                    "Users", userId, 700,1000);
        });

        reportsButton.setOnAction(event -> {
            DBUtil.changeScene(event, "/com/example/projektzielonifx/reports/ReportsSelector.fxml",
                    "Raports", userId, 700,1000);
        });

        projectsInfoButton.setOnAction(event -> {
            DBUtil.changeScene(event, "/com/example/projektzielonifx/ProjInfo/ProjInfo.fxml",
                    "Project Info", userId, 700,1000);
        });

        logoutButton.setOnAction(event -> {
            DBUtil.changeScene(event, "/com/example/projektzielonifx/auth/LoginWindow.fxml",
                    "Login", 0, 240,320);
        });

        notificationsButton.setOnAction(event -> {
            DBUtil.changeScene(event, "/com/example/projektzielonifx/notifications/notification_view.fxml",
                    "Notifications", userId, 600,800);
        });

        newProjectButton.setOnAction(event -> {
            DBUtil.changeScene(event, "/com/example/projektzielonifx/newproject/NewProject.fxml",
                    "New Project/Milestone", userId, 700,900);
        });

    }
}
