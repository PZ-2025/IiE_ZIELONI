package com.example.projektzielonifx.newproject;

import com.example.projektzielonifx.InitializableWithId;
import com.example.projektzielonifx.models.Project;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import com.example.projektzielonifx.models.Milestone;

import java.time.LocalDate;
import java.util.Objects;

import static com.example.projektzielonifx.database.DBUtil.changeScene;

/**
 * Controller for managing both Projects and Milestones in a unified interface
 */
public class NewProject implements InitializableWithId {

    /* ========== PROJECTS TAB ========== */
    // Project Table
    @FXML protected TableView<Project> projectTable;
    @FXML protected TableColumn<Project, Integer> colProjectId;
    @FXML protected TableColumn<Project, String> colProjectName;
    @FXML protected TableColumn<Project, String> colProjectStatus;
    @FXML protected TableColumn<Project, Integer> colProjectProgress;
    @FXML protected TableColumn<Project, LocalDate> colProjectStart;
    @FXML protected TableColumn<Project, LocalDate> colProjectEnd;
    @FXML protected TableColumn<Project, String> colProjectManager;

    // Project Form
    @FXML protected TextField projectNameField;
    @FXML protected ComboBox<String> projectStatusBox;
    @FXML protected Spinner<Integer> projectProgressSpinner;
    @FXML protected DatePicker projectStartPicker;
    @FXML protected DatePicker projectEndPicker;
    @FXML protected ComboBox<Manager> projectManagerBox;

    // Project Buttons
    @FXML protected Button newProjectBtn;
    @FXML protected Button saveProjectBtn;
    @FXML protected Button deleteProjectBtn;
    @FXML protected Button clearProjectBtn;

    /* ========== MILESTONES TAB ========== */
    // Milestone Table
    @FXML protected TableView<Milestone> milestoneTable;
    @FXML protected TableColumn<Milestone, Integer> colMilestoneId;
    @FXML protected TableColumn<Milestone, String> colMilestoneName;
    @FXML protected TableColumn<Milestone, String> colMilestoneProject;
    @FXML protected TableColumn<Milestone, LocalDate> colMilestoneDeadline;
    @FXML protected TableColumn<Milestone, String> colMilestoneDescription;

    // Milestone Form
    @FXML protected TextField milestoneNameField;
    @FXML protected TextArea milestoneDescriptionArea;
    @FXML protected ComboBox<Project> milestoneProjectBox;
    @FXML protected DatePicker milestoneDeadlinePicker;

    // Milestone Buttons
    @FXML protected Button newMilestoneBtn;
    @FXML protected Button saveMilestoneBtn;
    @FXML protected Button deleteMilestoneBtn;
    @FXML protected Button clearMilestoneBtn;

    /* ========== GENERAL ========== */
    @FXML protected TabPane mainTabPane;
    @FXML protected Button backButton;

    // Services
    protected final ProjectService projectService = new ProjectService();
    protected final ManagerService managerService = new ManagerService();
    protected final MilestoneService milestoneService = new MilestoneService();

    // Data Lists
    protected final ObservableList<Project> projects = FXCollections.observableArrayList();
    protected final ObservableList<Milestone> milestones = FXCollections.observableArrayList();
    protected int userId;

    @FXML
    protected void initialize() {
        setupProjectsTab();
        setupMilestonesTab();
        loadData();
        setupValidation();
        setupDateValidation(); // Add date validation setup
    }

    /* ========== PROJECTS TAB SETUP ========== */
    protected void setupProjectsTab() {
        // Setup project table columns
        colProjectId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProjectName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colProjectStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colProjectStart.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colProjectEnd.setCellValueFactory(new PropertyValueFactory<>("endDate"));

        // Progress column with progress bar
        colProjectProgress.setCellValueFactory(new PropertyValueFactory<>("progress"));
        colProjectProgress.setCellFactory(tc -> new TableCell<>() {
            protected final ProgressBar bar = new ProgressBar(0);
            {
                bar.setMaxWidth(Double.MAX_VALUE);
            }
            @Override
            protected void updateItem(Integer progress, boolean empty) {
                super.updateItem(progress, empty);
                if (empty || progress == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    bar.setProgress(progress / 100.0);
                    setGraphic(bar);
                    setText(progress + "%");
                }
            }
        });

        // Manager column
        colProjectManager.setCellValueFactory(cd -> Bindings.createStringBinding(() ->
                getManagerNameById(cd.getValue().getManagerId())));

        // Setup project form
        projectStatusBox.setItems(FXCollections.observableArrayList(
                "planowany", "wTrakcie", "zakonczony", "anulowany"));

        projectProgressSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0));

        projectManagerBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Manager manager) {
                return manager == null ? "" : manager.getFullName();
            }
            @Override
            public Manager fromString(String string) {
                return null;
            }
        });

        // Table selection listener
        projectTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldProject, newProject) -> populateProjectForm(newProject));

        projectTable.setItems(projects);
    }

    /* ========== MILESTONES TAB SETUP ========== */
    protected void setupMilestonesTab() {
        // Setup milestone table columns
        colMilestoneId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colMilestoneName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colMilestoneDeadline.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        colMilestoneDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colMilestoneProject.setCellValueFactory(cd -> Bindings.createStringBinding(() ->
                getProjectNameById(cd.getValue().getProjectId())));

        milestoneProjectBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Project project) {
                return project == null ? "" : project.getName();
            }
            @Override
            public Project fromString(String string) {
                return null;
            }
        });

        // Table selection listener
        milestoneTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldMilestone, newMilestone) -> populateMilestoneForm(newMilestone));

        milestoneTable.setItems(milestones);
    }

    /* ========== VALIDATION SETUP ========== */
    protected void setupValidation() {
        // Project form validation
        saveProjectBtn.disableProperty().bind(Bindings.createBooleanBinding(() ->
                        projectNameField.getText().isBlank() ||
                                projectStatusBox.getValue() == null ||
                                projectStartPicker.getValue() == null ||
                                projectManagerBox.getValue() == null,
                projectNameField.textProperty(),
                projectStatusBox.valueProperty(),
                projectStartPicker.valueProperty(),
                projectManagerBox.valueProperty()));

        // Milestone form validation
        saveMilestoneBtn.disableProperty().bind(Bindings.createBooleanBinding(() ->
                        milestoneNameField.getText().isBlank() ||
                                milestoneProjectBox.getValue() == null ||
                                milestoneDeadlinePicker.getValue() == null,
                milestoneNameField.textProperty(),
                milestoneProjectBox.valueProperty(),
                milestoneDeadlinePicker.valueProperty()));
    }

    /* ========== DATE VALIDATION SETUP ========== */
    protected void setupDateValidation() {
        // Project date validation - prevent invalid end dates
        projectEndPicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null && projectStartPicker.getValue() != null) {
                if (newDate.isBefore(projectStartPicker.getValue())) {
                    showValidationAlert("Invalid Date",
                            "Project end date cannot be before the start date.");
                    projectEndPicker.setValue(oldDate);
                }
            }
        });

        projectStartPicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null && projectEndPicker.getValue() != null) {
                if (projectEndPicker.getValue().isBefore(newDate)) {
                    showValidationAlert("Invalid Date",
                            "Project start date cannot be after the end date.");
                    projectStartPicker.setValue(oldDate);
                }
            }
        });

        // Milestone deadline validation
        milestoneDeadlinePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            validateMilestoneDeadline(newDate, oldDate);
        });

        milestoneProjectBox.valueProperty().addListener((obs, oldProject, newProject) -> {
            // Re-validate deadline when project changes
            if (milestoneDeadlinePicker.getValue() != null) {
                validateMilestoneDeadline(milestoneDeadlinePicker.getValue(),
                        milestoneDeadlinePicker.getValue());
            }
        });
    }

    /* ========== DATE VALIDATION METHODS ========== */
    protected void validateMilestoneDeadline(LocalDate newDate, LocalDate fallbackDate) {
        if (newDate == null || milestoneProjectBox.getValue() == null) {
            return;
        }

        Project selectedProject = milestoneProjectBox.getValue();
        LocalDate projectStart = selectedProject.getStartDate();
        LocalDate projectEnd = selectedProject.getEndDate();

        boolean isValid = true;
        String errorMessage = "";

        if (projectStart != null && newDate.isBefore(projectStart)) {
            isValid = false;
            errorMessage = "Milestone deadline cannot be before the project start date (" +
                    projectStart + ").";
        } else if (projectEnd != null && newDate.isAfter(projectEnd)) {
            isValid = false;
            errorMessage = "Milestone deadline cannot be after the project end date (" +
                    projectEnd + ").";
        }

        if (!isValid) {
            showValidationAlert("Invalid Milestone Deadline", errorMessage);
            milestoneDeadlinePicker.setValue(fallbackDate == newDate ? null : fallbackDate);
        }
    }

    protected void showValidationAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /* ========== DATA LOADING ========== */
    protected void loadData() {
        loadProjects();
        loadMilestones();
        loadManagers();
    }

    protected void loadProjects() {
        projects.setAll(projectService.list());
        milestoneProjectBox.setItems(projects);
    }

    protected void loadMilestones() {
        milestones.setAll(milestoneService.list());
    }

    protected void loadManagers() {
        projectManagerBox.setItems(FXCollections.observableArrayList(managerService.list()));
    }

    @FXML
    protected void onSaveProject() {
        if (!validateProjectForm() || !validateProjectDates()) return;

        Project selectedProject = projectTable.getSelectionModel().getSelectedItem();
        if (selectedProject == null) {
            // Create new project
            Project newProject = buildProjectFromForm(new Project(0, 0, "", 0, "", null, null));
            int newId = projectService.add(newProject);
            if (newId > 0) {
                newProject.setId(newId);
                projects.add(newProject);
                showSuccessAlert("Project created successfully!");
            }
        } else {
            // Update existing project
            buildProjectFromForm(selectedProject);
            if (projectService.save(selectedProject)) {
                projectTable.refresh();
                showSuccessAlert("Project updated successfully!");
            }
        }
        clearProjectForm();
        loadProjects(); // Refresh project list for milestones
    }

    @FXML
    protected void onDeleteProject() {
        Project selectedProject = projectTable.getSelectionModel().getSelectedItem();
        if (selectedProject != null) {
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Delete Project");
            confirmDialog.setHeaderText("Are you sure you want to delete this project?");
            confirmDialog.setContentText("This action cannot be undone.");

            if (confirmDialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                if (projectService.remove(selectedProject.getId())) {
                    projects.remove(selectedProject);
                    clearProjectForm();
                    loadProjects(); // Refresh project list for milestones
                    showSuccessAlert("Project deleted successfully!");
                }
            }
        }
    }

    @FXML
    protected void onClearProject() {
        projectTable.getSelectionModel().clearSelection();
        clearProjectForm();
    }

    @FXML
    protected void onSaveMilestone() {
        if (!validateMilestoneForm() || !validateMilestoneDeadlineBeforeSave()) return;

        Milestone selectedMilestone = milestoneTable.getSelectionModel().getSelectedItem();
        Project selectedProject = milestoneProjectBox.getValue();

        if (selectedMilestone == null) {
            // Create new milestone
            Milestone newMilestone = buildMilestoneFromForm(new Milestone(0, ""));
            int newId = milestoneService.add(newMilestone, selectedProject.getId());
            if (newId > 0) {
                // Update this part to include projectId
                newMilestone = new Milestone(newId, selectedProject.getId(), newMilestone.getName(),newMilestone.getDeadline());
                newMilestone.setDescription(milestoneDescriptionArea.getText());
                newMilestone.setDeadline(milestoneDeadlinePicker.getValue());
                milestones.add(newMilestone);
                showSuccessAlert("Milestone created successfully!");
            }
        } else {
            // Update existing milestone
            buildMilestoneFromForm(selectedMilestone);
            if (milestoneService.save(selectedMilestone, selectedProject.getId())) {
                milestoneTable.refresh();
                showSuccessAlert("Milestone updated successfully!");
            }
        }
        clearMilestoneForm();
    }

    @FXML
    protected void onDeleteMilestone() {
        Milestone selectedMilestone = milestoneTable.getSelectionModel().getSelectedItem();
        if (selectedMilestone != null) {
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Delete Milestone");
            confirmDialog.setHeaderText("Are you sure you want to delete this milestone?");
            confirmDialog.setContentText("This action cannot be undone.");

            if (confirmDialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                if (milestoneService.remove(selectedMilestone.getId())) {
                    milestones.remove(selectedMilestone);
                    clearMilestoneForm();
                    showSuccessAlert("Milestone deleted successfully!");
                }
            }
        }
    }

    @FXML
    protected void onClearMilestone() {
        milestoneTable.getSelectionModel().clearSelection();
        clearMilestoneForm();
    }

    /* ========== HELPER METHODS ========== */
    protected void populateProjectForm(Project project) {
        if (project == null) {
            clearProjectForm();
            return;
        }

        projectNameField.setText(project.getName());
        projectStatusBox.setValue(project.getStatus());
        projectProgressSpinner.getValueFactory().setValue(project.getProgress());
        projectStartPicker.setValue(project.getStartDate());
        projectEndPicker.setValue(project.getEndDate());
        projectManagerBox.getSelectionModel().select(findManagerById(project.getManagerId()));
    }

    protected void populateMilestoneForm(Milestone milestone) {
        if (milestone == null) {
            clearMilestoneForm();
            return;
        }

        milestoneNameField.setText(milestone.getName());
        milestoneDescriptionArea.setText(milestone.getDescription());
        milestoneDeadlinePicker.setValue(milestone.getDeadline());

        // Add this line to select the correct project
        milestoneProjectBox.getSelectionModel().select(findProjectById(milestone.getProjectId()));
    }

    protected Project buildProjectFromForm(Project target) {
        target.setName(projectNameField.getText());
        target.setStatus(projectStatusBox.getValue());
        target.setProgress(projectProgressSpinner.getValue());
        target.setStartDate(projectStartPicker.getValue());
        target.setEndDate(projectEndPicker.getValue());
        target.setManagerId(Objects.requireNonNull(projectManagerBox.getValue()).getId());
        return target;
    }

    protected Milestone buildMilestoneFromForm(Milestone target) {
        target.setName(milestoneNameField.getText());
        target.setDescription(milestoneDescriptionArea.getText());
        target.setDeadline(milestoneDeadlinePicker.getValue());
        return target;
    }

    protected boolean validateProjectForm() {
        return !projectNameField.getText().isBlank()
                && projectStatusBox.getValue() != null
                && projectStartPicker.getValue() != null
                && projectManagerBox.getValue() != null;
    }

    protected boolean validateProjectDates() {
        LocalDate startDate = projectStartPicker.getValue();
        LocalDate endDate = projectEndPicker.getValue();

        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            showValidationAlert("Invalid Project Dates",
                    "Project end date cannot be before the start date.");
            return false;
        }
        return true;
    }

    protected boolean validateMilestoneForm() {
        return !milestoneNameField.getText().isBlank()
                && milestoneProjectBox.getValue() != null
                && milestoneDeadlinePicker.getValue() != null;
    }

    protected boolean validateMilestoneDeadlineBeforeSave() {
        LocalDate deadline = milestoneDeadlinePicker.getValue();
        Project selectedProject = milestoneProjectBox.getValue();

        if (deadline == null || selectedProject == null) {
            return true; // Let other validation handle these cases
        }

        LocalDate projectStart = selectedProject.getStartDate();
        LocalDate projectEnd = selectedProject.getEndDate();

        if (projectStart != null && deadline.isBefore(projectStart)) {
            showValidationAlert("Invalid Milestone Deadline",
                    "Milestone deadline cannot be before the project start date (" + projectStart + ").");
            return false;
        }

        if (projectEnd != null && deadline.isAfter(projectEnd)) {
            showValidationAlert("Invalid Milestone Deadline",
                    "Milestone deadline cannot be after the project end date (" + projectEnd + ").");
            return false;
        }

        return true;
    }

    protected void clearProjectForm() {
        projectNameField.clear();
        projectStatusBox.getSelectionModel().clearSelection();
        projectProgressSpinner.getValueFactory().setValue(0);
        projectStartPicker.setValue(null);
        projectEndPicker.setValue(null);
        projectManagerBox.getSelectionModel().clearSelection();
    }

    protected void clearMilestoneForm() {
        milestoneNameField.clear();
        milestoneDescriptionArea.clear();
        milestoneDeadlinePicker.setValue(null);
        milestoneProjectBox.getSelectionModel().clearSelection();
    }

    protected Manager findManagerById(int id) {
        return projectManagerBox.getItems().stream()
                .filter(m -> m.getId() == id)
                .findFirst().orElse(null);
    }

    protected Project findProjectById(int id) {
        return milestoneProjectBox.getItems().stream()
                .filter(p -> p.getId() == id)
                .findFirst().orElse(null);
    }

    protected String getManagerNameById(int id) {
        Manager manager = findManagerById(id);
        return manager == null ? "?" : manager.getFullName();
    }

    protected String getProjectNameById(int id) {
        Project project = findProjectById(id);
        return project == null ? "Unknown Project" : project.getName();
    }

    protected void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void initializeWithId(int userId) {
        this.userId = userId;
        backButton.setOnAction(event -> {
            changeScene(event, "/com/example/projektzielonifx/home/HomePage.fxml",
                    "Home Page", userId, 700, 1000);
        });
    }
}