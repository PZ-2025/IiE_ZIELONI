package com.example.projektzielonifx.home;

import com.example.projektzielonifx.database.DBUtil;
import com.example.projektzielonifx.tasks.TaskFrame;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class MainContentController implements Initializable {

    private int userId;

    @FXML private Label welcomeLabel;
    @FXML private VBox mainContent;
    @FXML private VBox recentTask;
    @FXML private VBox importantTask;

    public void initData(int userId) {
        this.userId = userId;

        String username = DBUtil.getUsernameById(userId);

        welcomeLabel.setText("Hello " + username + "!");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Additional initialization if needed
        TaskFrame lowTask = new TaskFrame("UI","Dodaj UI","low","Not done");
        TaskFrame highTask = new TaskFrame("Database","Fix it","High","Not done");

        recentTask.getChildren().add(lowTask);
        importantTask.getChildren().add(highTask);
    }
}

//private void loadTasks() {
//    List<Task> tasks = fetchTasksFromDatabase(); // Your data fetch logic
//
//    for (Task task : tasks) {
//        TaskFrame frame = new TaskFrame(
//                task.getTitle(),
//                task.getDescription(),
//                task.getPriority(),
//                task.getProgress()
//        );
//        mainContentRoot.getChildren().add(frame);
//    }
//}
//}