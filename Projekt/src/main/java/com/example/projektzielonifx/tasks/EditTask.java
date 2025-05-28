package com.example.projektzielonifx.tasks;

import com.example.projektzielonifx.InitializableWithId;
import com.example.projektzielonifx.database.DBUtil;
import com.example.projektzielonifx.models.Milestone;
import com.example.projektzielonifx.models.Priority;
import com.example.projektzielonifx.models.Status;
import com.example.projektzielonifx.models.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.sql.SQLException;

import static com.example.projektzielonifx.database.DBUtil.showAlert;

public class EditTask implements InitializableWithId {

    @FXML
    private TextField titleField;
    // Add methods to get/set field values
    private int userId;

    @FXML
    private ComboBox<Priority> priorityChoice;
    @FXML
    private ComboBox<Status> statusChoice;
    @FXML
    private Button cancelButton;
    @FXML
    private ComboBox<Milestone> milestoneChoice;
    @FXML
    private Button saveButton;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private Spinner<Integer> progressSpinner;
    @FXML
    private DatePicker deadlinePicker;
    private Integer taskId;

    @Override
    public void initializeWithId(int userId) {
        this.userId = userId;
        this.taskId = null; // Default to creating new task
        initializeForm();
    }

    public void initializeWithTaskId(int userId,int taskId) {
        this.userId = userId;
        this.taskId = taskId;
        initializeForm();
        loadTaskData();

    }

    private void initializeForm() {
        // Initialize ComboBoxes
        priorityChoice.getItems().setAll(Priority.values());
        statusChoice.getItems().setAll(Status.values());
        milestoneChoice.setItems(DBUtil.getAllMilestones());

        // Set default selections for new task
        if (taskId == null) {
            milestoneChoice.getSelectionModel().selectFirst();
            priorityChoice.getSelectionModel().selectFirst();
            statusChoice.getSelectionModel().selectFirst();
        }

        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
        progressSpinner.setValueFactory(valueFactory);

        // Set up button actions
        cancelButton.setOnAction(event -> {
            DBUtil.changeScene(event, "/com/example/projektzielonifx/tasks/TasksView.fxml",
                    "Tasks List", userId, 700, 1000);
        });

        saveButton.setOnAction(this::saveTask);
    }
    /**
     * Load existing task data for editing
     */
    private void loadTaskData() {
        Task task = DBUtil.getTaskById(taskId); // You'll need to implement this method in DBUtil
        if (task != null) {
            titleField.setText(task.getTitle());
            descriptionArea.setText(task.getDescription());
            priorityChoice.setValue(task.getPriority());
            statusChoice.setValue(task.getStatus());
            progressSpinner.getValueFactory().setValue(task.getProgress());
            deadlinePicker.setValue(task.getDeadline());

            // Set milestone selection
            for (Milestone milestone : milestoneChoice.getItems()) {
                if (milestone.getId() == task.getMilestoneId()) {
                    milestoneChoice.setValue(milestone);
                    break;
                }
            }
        }
    }

    /**
     * Save task (create new or update existing)
     */
    private void saveTask(ActionEvent event) {
        Task task = new Task();
        updateTaskFromForm(task);
        System.out.println("Czy to dziala");
        if (taskId == null) {
            // Creating new task
            DBUtil.createTask(task);
            showAlert("Sukces", "Zadanie zostało utworzone pomyślnie", Alert.AlertType.INFORMATION);
        } else {
            // Updating existing task
            task.setId(taskId);
            DBUtil.updateTask(task);
            showAlert("Sukces", "Zadanie zostało zaktualizowane pomyślnie", Alert.AlertType.INFORMATION);
        }

        // Return to tasks view
        DBUtil.changeScene(event,
                "/com/example/projektzielonifx/tasks/TasksView.fxml",
                "Tasks List", userId, 700, 1000);

    }

    private void updateTaskFromForm(Task task) {
        task.setTitle(titleField.getText());
        task.setDescription(descriptionArea.getText());
        task.setPriority(priorityChoice.getValue());
        task.setStatus(statusChoice.getValue());
        task.setProgress(progressSpinner.getValue());
        task.setDeadline(deadlinePicker.getValue());
        task.setMilestoneId(milestoneChoice.getValue().getId());
    }
}