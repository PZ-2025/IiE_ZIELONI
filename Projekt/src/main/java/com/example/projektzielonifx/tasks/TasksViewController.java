package com.example.projektzielonifx.tasks;

import com.example.projektzielonifx.InitializableWithId;
import com.example.projektzielonifx.database.DBUtil;
import com.example.projektzielonifx.models.TaskModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static com.example.projektzielonifx.database.DBUtil.changeScene;

/**
 * Kontroler widoku zadań, odpowiedzialny za wyświetlanie zadań w siatce 3x3.
 * Pobiera dane z bazy danych i tworzy ramki zadań (TaskFrame) dla każdego zadania.
 */
public class TasksViewController implements InitializableWithId {

    @FXML
    private BorderPane mainContainer;

    @FXML
    private VBox headerBox;

    @FXML
    private Button backButton;

    @FXML
    private Label titleLabel;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private GridPane tasksGrid;

    // Number of columns in the grid
    private final int GRID_COLUMNS = 3;
    private int userId;


    @Override
    public void initializeWithId(int userId) {
        this.userId = userId;

        // Configure the scroll pane
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Configure the grid
        tasksGrid.setHgap(20);
        tasksGrid.setVgap(20);
        tasksGrid.setPadding(new Insets(20));

        // Set up button actions
        backButton.setOnAction(event -> {
            changeScene(event, "/com/example/projektzielonifx/home/HomePage.fxml", "Home Page", userId, 700, 1000);
        });

        // Load tasks from database
        loadTasks();
    }

    /**
     * Pobiera zadania z bazy danych i dodaje je do siatki.
     */
    private void loadTasks() {
        // Clear existing grid
        tasksGrid.getChildren().clear();
        List<TaskModel> tasks = DBUtil.findTasks(userId);

        // Populate the grid with task frames
        int column = 0;
        int row = 0;

        for (TaskModel task : tasks) {
            TaskFrame taskFrame = new TaskFrame(
                    task.getTitle(),
                    task.getDescription(),
                    task.getPriority(),
                    task.getStatus(),
                    task.getDeadline()
            );

            // Add the task frame to the grid
            tasksGrid.add(taskFrame, column, row);

            // Update column and row indices
            column++;
            if (column >= GRID_COLUMNS) {
                column = 0;
                row++;
            }
        }
    }
}