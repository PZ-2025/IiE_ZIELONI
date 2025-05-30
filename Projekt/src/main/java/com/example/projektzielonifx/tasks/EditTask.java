package com.example.projektzielonifx.tasks;

import com.example.projektzielonifx.InitializableWithId;
import com.example.projektzielonifx.database.DBUtil;
import com.example.projektzielonifx.models.Milestone;
import com.example.projektzielonifx.models.Priority;
import com.example.projektzielonifx.models.Status;
import com.example.projektzielonifx.models.Task;
import com.example.projektzielonifx.models.User;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.controlsfx.control.SearchableComboBox;

import java.sql.SQLException;
import java.time.LocalDate;

import static com.example.projektzielonifx.database.DBUtil.getLevel;
import static com.example.projektzielonifx.database.DBUtil.showAlert;

public class EditTask implements InitializableWithId {

    public VBox rootContainer;
    public VBox mainContent;
    @FXML
    protected TextField titleField;
    @FXML
    protected SearchableComboBox<User> assignedUserChoice;
    @FXML
    protected Label statusLabel;
    // Add methods to get/set field values
    protected int userId;

    @FXML
    protected ComboBox<Priority> priorityChoice;
    @FXML
    protected Button cancelButton;
    @FXML
    protected ComboBox<Milestone> milestoneChoice;
    @FXML
    protected Button saveButton;
    @FXML
    protected TextArea descriptionArea;
    @FXML
    protected Spinner<Integer> progressSpinner;
    @FXML
    protected DatePicker deadlinePicker;
    protected Integer taskId;
    @FXML
    protected Button deleteButton;
    protected int privilegeLevel;
    protected int originalProgress = 0; // Store the original progress value

    @Override
    public void initializeWithId(int userId) {
        this.userId = userId;
        privilegeLevel = getLevel(userId);

        this.taskId = null; // Default to creating new task
        initializeForm();
        if(privilegeLevel == 1){
            disableFormElementsForLimitedUser();
        }
    }

    /**
     * Disable all form elements except progressSpinner for privilege level 1 users
     */
    protected void disableFormElementsForLimitedUser() {
        titleField.setDisable(true);
        descriptionArea.setDisable(true);
        priorityChoice.setDisable(true);
        assignedUserChoice.setDisable(true);
        milestoneChoice.setDisable(true);
        deadlinePicker.setDisable(true);
        deleteButton.setDisable(true);

        // Set minimum value for progress spinner to prevent reducing progress
        if (taskId != null) { // Only for existing tasks
            SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(
                    originalProgress, 100, originalProgress);
            progressSpinner.setValueFactory(valueFactory);
        }
    }

    /**
     * Setup focus clearing functionality when clicking on empty areas
     */
    protected void setupFocusClearing() {
        if (rootContainer != null) {
            rootContainer.setOnMouseClicked(event -> {
                // Only clear focus if clicking on the container itself, not on its children
                if (event.getTarget() == rootContainer) {
                    rootContainer.requestFocus();
                }
            });
            mainContent.setOnMouseClicked(event -> {
                if(event.getTarget() == mainContent){
                    mainContent.requestFocus();
                }
            });
            mainContent.setFocusTraversable(true);
            // Make the container focusable so it can receive focus
            rootContainer.setFocusTraversable(true);
        }
    }

    public void initializeWithTaskId(int userId,int taskId) {
        privilegeLevel = getLevel(userId);

        this.userId = userId;
        this.taskId = taskId;
        initializeForm();
        loadTaskData();
        if(privilegeLevel == 1){
            disableFormElementsForLimitedUser();
        }

    }

    protected void initializeForm() {
        // Initialize ComboBoxes
        priorityChoice.getItems().setAll(Priority.values());
        milestoneChoice.setItems(DBUtil.getAllMilestones());

        // Initialize user choice with searchable functionality
        setupUserChoice();

        // Set default selections for new task
        if (taskId == null) {
            milestoneChoice.getSelectionModel().selectFirst();
            priorityChoice.getSelectionModel().selectFirst();
        }

        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
        progressSpinner.setValueFactory(valueFactory);

        // Add listener to progress spinner to update status label
        progressSpinner.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
                updateStatusLabel(newValue);
            }
        });

        // Initialize status label
        updateStatusLabel(0);

        // Add click handler to root container to clear focus
        setupFocusClearing();

        // Set up button actions
        cancelButton.setOnAction(event -> {
            DBUtil.changeScene(event, "/com/example/projektzielonifx/tasks/TasksView.fxml",
                    "Tasks List", userId, 700, 1000);
        });

        saveButton.setOnAction(this::saveTask);

        deleteButton.setOnAction(event -> {DBUtil.deleteTask(taskId);});
    }

    /**
     * Setup the searchable user choice box
     */
    protected void setupUserChoice() {
        // Get all users from database (you'll need to implement this in DBUtil)
        ObservableList<User> users = switch (privilegeLevel) {
            case 2 -> FXCollections.observableArrayList(DBUtil.getUsersForTeam(userId));
            case 3 -> FXCollections.observableArrayList(DBUtil.getUsersForManager(userId));
            default -> FXCollections.observableArrayList(DBUtil.getUsers());
        };
        //        users = FXCollections.observableArrayList(DBUtil.getUsers());
        assignedUserChoice.setItems(users);

        // Set up string converter to display user names properly
        assignedUserChoice.setConverter(new StringConverter<User>() {
            @Override
            public String toString(User user) {
                return user != null ? user.getFirstName() + " " + user.getLastName() : "";
            }

            @Override
            public User fromString(String string) {
                // This is used for searching - match against the display string
                return assignedUserChoice.getItems().stream()
                        .filter(user -> (user.getFirstName() + " " + user.getLastName()).equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        // Set prompt text
        assignedUserChoice.setPromptText("Search and select user...");
    }

    /**
     * Update the status label based on progress value
     * @param progress The current progress value
     */
    protected void updateStatusLabel(Integer progress) {
        if (progress == null) progress = 0;

        if (progress == 0) {
            statusLabel.setText("Do zrobienia");
            statusLabel.getStyleClass().removeAll("status-in-progress", "status-completed");
            statusLabel.getStyleClass().add("status-todo");
        } else if (progress < 100) {
            statusLabel.setText("W trakcie");
            statusLabel.getStyleClass().removeAll("status-todo", "status-completed");
            statusLabel.getStyleClass().add("status-in-progress");
        } else {
            statusLabel.setText("Zrobione");
            statusLabel.getStyleClass().removeAll("status-todo", "status-in-progress");
            statusLabel.getStyleClass().add("status-completed");
        }
    }

    /**
     * Load existing task data for editing
     */
    protected void loadTaskData() {
        Task task = DBUtil.getTaskById(taskId); // You'll need to implement this method in DBUtil
        if (task != null) {
            titleField.setText(task.getTitle());
            descriptionArea.setText(task.getDescription());
            priorityChoice.setValue(task.getPriority());
            progressSpinner.getValueFactory().setValue(task.getProgress());
            deadlinePicker.setValue(task.getDeadline());
            // Store the original progress value
            originalProgress = task.getProgress();

            // Update status label based on loaded progress
            updateStatusLabel(task.getProgress());

            // Set milestone selection
            for (Milestone milestone : milestoneChoice.getItems()) {
                if (milestone.getId() == task.getMilestoneId()) {
                    milestoneChoice.setValue(milestone);
                    break;
                }
            }
            // Set assigned user selection
            if (task.getAssignedUserId() != null) {
                for (User user : assignedUserChoice.getItems()) {
                    if (user.getId() == task.getAssignedUserId()) {
                        assignedUserChoice.setValue(user);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Save task (create new or update existing)
     */
    protected void saveTask(ActionEvent event) {
        // Validate progress for privilege level 1 users
        if (privilegeLevel == 1 && taskId != null) {
            int currentProgress = progressSpinner.getValue();
            if (currentProgress < originalProgress) {
                showAlert("Błąd", "Nie możesz zmniejszyć postępu zadania poniżej wartości " + originalProgress + "%",
                        Alert.AlertType.ERROR);
                return; // Don't save if validation fails
            }
        }

        Task task = new Task();
        updateTaskFromForm(task);
        boolean isValid = checkValidationTask(task);

        if (!isValid) {
            return;
        }

        if (taskId == null) {
            // Creating new task
            DBUtil.createTask(task,userId);
            showAlert("Sukces", "Zadanie zostało utworzone pomyślnie", Alert.AlertType.INFORMATION);
        } else {
            // Updating existing task
            task.setId(taskId);
            System.out.println(task);
            DBUtil.updateTask(task,userId);
            showAlert("Sukces", "Zadanie zostało zaktualizowane pomyślnie", Alert.AlertType.INFORMATION);
        }

        // Return to tasks view
        DBUtil.changeScene(event,
                "/com/example/projektzielonifx/tasks/TasksView.fxml",
                "Tasks List", userId, 700, 1000);

    }

    protected boolean checkValidationTask(Task task) {
        System.out.println("Validating task...");
        System.out.println("Title: '" + task.getTitle() + "'");
        System.out.println("Description: '" + task.getDescription() + "'");
        System.out.println("Deadline: " + task.getDeadline());
        System.out.println("Priority: " + task.getPriority());
        System.out.println("Assigned User ID: " + task.getAssignedUserId());

        LocalDate selectedDate = deadlinePicker.getValue();
        System.out.println("DatePicker raw value: " + selectedDate);
        if (selectedDate == null) {
            System.out.println("Date is null!");
            showAlert("Błąd", "Data nie może być pusta", Alert.AlertType.WARNING);
            return false;
        }

        // Check deadline first
        if (task.getDeadline() == null) {
            showAlert("Błąd", "Data nie może być pusta", Alert.AlertType.WARNING);
            return false;
        }

        // Check title - handle null and empty/whitespace strings
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            showAlert("Błąd", "Tytuł nie może być pusty", Alert.AlertType.WARNING);
            return false;
        }

        // Check description - handle null and empty/whitespace strings
        if (task.getDescription() == null || task.getDescription().trim().isEmpty()) {
            showAlert("Błąd", "Opis nie może być pusty", Alert.AlertType.WARNING);
            return false;
        }

        // Check priority
        if (task.getPriority() == null) {
            showAlert("Błąd", "Priorytet musi być wybrany", Alert.AlertType.WARNING);
            return false;
        }

        // Check assigned user
        if (task.getAssignedUserId() == null) {
            showAlert("Błąd", "Użytkownik musi być przypisany", Alert.AlertType.WARNING);
            return false;
        }

        // Check milestone
        if (task.getMilestoneId() == null || task.getMilestoneId() <= 0) {
            showAlert("Błąd", "Kamień milowy musi być wybrany", Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    protected void updateTaskFromForm(Task task) {
        // Get text values and trim them
        String title = titleField.getText();
        String description = descriptionArea.getText();

        task.setTitle(title != null ? title.trim() : "");
        task.setDescription(description != null ? description.trim() : "");
        task.setPriority(priorityChoice.getValue());

        // Calculate status based on progress
        int progress = progressSpinner.getValue();
        if (progress == 0) {
            task.setStatus(Status.doZrobienia);
        } else if (progress < 100) {
            task.setStatus(Status.wTrakcie);
        } else {
            task.setStatus(Status.zrobione);
        }

        task.setProgress(progress);
        task.setDeadline(deadlinePicker.getValue());

        // Set milestone ID
        Milestone selectedMilestone = milestoneChoice.getValue();
        if (selectedMilestone != null) {
            task.setMilestoneId(selectedMilestone.getId());
        } else {
            task.setMilestoneId(null);
        }

        // Set assigned user ID
        User assignedUser = assignedUserChoice.getValue();
        if (assignedUser != null) {
            task.setAssignedUserId(assignedUser.getId());
        } else {
            task.setAssignedUserId(null);
        }
    }


}