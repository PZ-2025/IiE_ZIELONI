package com.example.projektzielonifx.home;

import com.example.projektzielonifx.database.DBUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class NavController implements Initializable {

    private int userId;

    @FXML private Button tasksButton;
    @FXML private Button teamMembersButton;
    @FXML private Button logoutButton;

    public void initData(int userId) {
        this.userId = userId;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupButtonActions();
    }

    private void setupButtonActions() {
        tasksButton.setOnAction(event -> {
            Stage currentStage = (Stage) tasksButton.getScene().getWindow();
            currentStage.close();

//            // Open TasksPage (would need to be converted to FXML too)
//            TasksPage tasksPage = new TasksPage(userName, databaseService);
//            Stage tasksStage = new Stage();
//            tasksPage.start(tasksStage);
        });

        teamMembersButton.setOnAction(event -> {
            DBUtil.changeScene(event, "/com/example/projektzielonifx/userstab/UsersTable.fxml", "Users", userId, 700,1000);
        });

        logoutButton.setOnAction(event -> {
            DBUtil.changeScene(event, "/com/example/projektzielonifx/auth/LoginWindow.fxml", "Log In!", 0, 240,320);
        });
    }
}