package com.example.projektzielonifx.ProjInfo;

import com.example.projektzielonifx.InitializableWithId;
import com.example.projektzielonifx.database.DBUtil;
import com.example.projektzielonifx.models.ProjectModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.net.URL;
import java.util.ResourceBundle;

import static com.example.projektzielonifx.database.DBUtil.changeScene;

public class ProjInfo implements InitializableWithId, Initializable {
    private int userId;
    @FXML
    private Button backButton;
    @FXML private Label welcomeLabel;
    @FXML private Label teamLabel;
    @FXML private Label roleLabel;
    @FXML private Label managerLabel;
    @FXML private Label teamLeadLabel;
    @FXML private Label projectNamesLabel;
    @FXML private Label milestonesLabel;
    @FXML private Label totalTasksLabel;
    @FXML private Label doneTasksLabel;
    @FXML private Label progressPercentLabel;
    @FXML private ProgressBar progressBar;

    @Override
    public void initializeWithId(int userId) {
        this.userId = userId;
        try {
            ProjectModel info = DBUtil.findByUserId(userId);
            welcomeLabel.setText("Welcome, " + info.getFirst_name() + " " + info.getLast_name());
            teamLabel.setText(info.getTeam());
            roleLabel.setText(info.getRole());
            managerLabel.setText(info.getManager_name());
            teamLeadLabel.setText(info.getTeam_leader_name());
            projectNamesLabel.setText(info.getProjects_assigned());
            milestonesLabel.setText(info.getMilestone_assigned());
            totalTasksLabel.setText(info.getTotal_tasks());
            doneTasksLabel.setText(info.getDone());

            // Calculate progress percentage
            try {
                int totalTasks = Integer.parseInt(info.getTotal_tasks());
                int doneTasks = Integer.parseInt(info.getDone());
                double progress = totalTasks > 0 ? (double) doneTasks / totalTasks : 0;
                System.out.println(progress);
                progressBar.setProgress(progress);
                progressPercentLabel.setText(String.format("%.0f%%", progress * 100));
            } catch (NumberFormatException e) {
                progressBar.setProgress(0);
                progressPercentLabel.setText("0%");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initialize(URL url, ResourceBundle resourceBundle) {
        backButton.setOnAction(event -> {
            changeScene(event, "/com/example/projektzielonifx/home/HomePage.fxml", "Home Page", userId, 700, 1000);
            return; // Success - exit method
        });
    }
}
