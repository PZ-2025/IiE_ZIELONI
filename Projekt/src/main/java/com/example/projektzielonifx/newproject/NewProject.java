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
    @FXML private TableView<Project> projectTable;
    @FXML private TableColumn<Project, Integer> colProjectId;
    @FXML private TableColumn<Project, String> colProjectName;
    @FXML private TableColumn<Project, String> colProjectStatus;
    @FXML private TableColumn<Project, Integer> colProjectProgress;
    @FXML private TableColumn<Project, LocalDate> colProjectStart;
    @FXML private TableColumn<Project, LocalDate> colProjectEnd;
    @FXML private TableColumn<Project, String> colProjectManager;

    // Project Form
    @FXML private TextField projectNameField;
    @FXML private ComboBox<String> projectStatusBox;
    @FXML private Spinner<Integer> projectProgressSpinner;
    @FXML private DatePicker projectStartPicker;
    @FXML private DatePicker projectEndPicker;
    @FXML private ComboBox<Manager> projectManagerBox;

    // Project Buttons
    @FXML private Button newProjectBtn;
    @FXML private Button saveProjectBtn;
    @FXML private Button deleteProjectBtn;
    @FXML private Button clearProjectBtn;

    /* ========== MILESTONES TAB ========== */
    // Milestone Table
    @FXML private TableView<Milestone> milestoneTable;
    @FXML private TableColumn<Milestone, Integer> colMilestoneId;
    @FXML private TableColumn<Milestone, String> colMilestoneName;
    @FXML private TableColumn<Milestone, String> colMilestoneProject;
    @FXML private TableColumn<Milestone, LocalDate> colMilestoneDeadline;
    @FXML private TableColumn<Milestone, String> colMilestoneDescription;

    // Milestone Form
    @FXML private TextField milestoneNameField;
    @FXML private TextArea milestoneDescriptionArea;
    @FXML private ComboBox<Project> milestoneProjectBox;
    @FXML private DatePicker milestoneDeadlinePicker;

    // Milestone Buttons
    @FXML private Button newMilestoneBtn;
    @FXML private Button saveMilestoneBtn;
    @FXML private Button deleteMilestoneBtn;
    @FXML private Button clearMilestoneBtn;

    /* ========== GENERAL ========== */
    @FXML private TabPane mainTabPane;
    @FXML private Button backButton;

    // Services
    private final ProjectService projectService = new ProjectService();
    private final ManagerService managerService = new ManagerService();
    private final MilestoneService milestoneService = new MilestoneService();

    // Data Lists
    private final ObservableList<Project> projects = FXCollections.observableArrayList();
    private final ObservableList<Milestone> milestones = FXCollections.observableArrayList();
    private int userId;

    @FXML
    private void initialize() {
        setupProjectsTab();
        setupMilestonesTab();
        loadData();
        setupValidation();
    }

    /* ========== PROJECTS TAB SETUP ========== */
    private void setupProjectsTab() {
        // Setup project table columns
        colProjectId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProjectName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colProjectStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colProjectStart.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colProjectEnd.setCellValueFactory(new PropertyValueFactory<>("endDate"));

        // Progress column with progress bar
        colProjectProgress.setCellValueFactory(new PropertyValueFactory<>("progress"));
        colProjectProgress.setCellFactory(tc -> new TableCell<>() {
            private final ProgressBar bar = new ProgressBar(0);
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
    private void setupMilestonesTab() {
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
    private void setupValidation() {
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

    /* ========== DATA LOADING ========== */
    private void loadData() {
        loadProjects();
        loadMilestones();
        loadManagers();
    }

    private void loadProjects() {
        projects.setAll(projectService.list());
        milestoneProjectBox.setItems(projects);
    }

    private void loadMilestones() {
        milestones.setAll(milestoneService.list());
    }

    private void loadManagers() {
        projectManagerBox.setItems(FXCollections.observableArrayList(managerService.list()));
    }

    @FXML
    private void onSaveProject() {
        if (!validateProjectForm()) return;

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
    private void onDeleteProject() {
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
    private void onClearProject() {
        projectTable.getSelectionModel().clearSelection();
        clearProjectForm();
    }

    @FXML
    private void onSaveMilestone() {
        if (!validateMilestoneForm()) return;

        Milestone selectedMilestone = milestoneTable.getSelectionModel().getSelectedItem();
        Project selectedProject = milestoneProjectBox.getValue();

        if (selectedMilestone == null) {
            // Create new milestone
            Milestone newMilestone = buildMilestoneFromForm(new Milestone(0, ""));
            int newId = milestoneService.add(newMilestone, selectedProject.getId());
            if (newId > 0) {
                // Update this part to include projectId
                newMilestone = new Milestone(newId, selectedProject.getId(), newMilestone.getName());
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
    private void onDeleteMilestone() {
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
    private void onClearMilestone() {
        milestoneTable.getSelectionModel().clearSelection();
        clearMilestoneForm();
    }

    /* ========== HELPER METHODS ========== */
    private void populateProjectForm(Project project) {
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

    private void populateMilestoneForm(Milestone milestone) {
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

    private Project buildProjectFromForm(Project target) {
        target.setName(projectNameField.getText());
        target.setStatus(projectStatusBox.getValue());
        target.setProgress(projectProgressSpinner.getValue());
        target.setStartDate(projectStartPicker.getValue());
        target.setEndDate(projectEndPicker.getValue());
        target.setManagerId(Objects.requireNonNull(projectManagerBox.getValue()).getId());
        return target;
    }

    private Milestone buildMilestoneFromForm(Milestone target) {
        target.setName(milestoneNameField.getText());
        target.setDescription(milestoneDescriptionArea.getText());
        target.setDeadline(milestoneDeadlinePicker.getValue());
        return target;
    }

    private boolean validateProjectForm() {
        return !projectNameField.getText().isBlank()
                && projectStatusBox.getValue() != null
                && projectStartPicker.getValue() != null
                && projectManagerBox.getValue() != null;
    }

    private boolean validateMilestoneForm() {
        return !milestoneNameField.getText().isBlank()
                && milestoneProjectBox.getValue() != null
                && milestoneDeadlinePicker.getValue() != null;
    }

    private void clearProjectForm() {
        projectNameField.clear();
        projectStatusBox.getSelectionModel().clearSelection();
        projectProgressSpinner.getValueFactory().setValue(0);
        projectStartPicker.setValue(null);
        projectEndPicker.setValue(null);
        projectManagerBox.getSelectionModel().clearSelection();
    }

    private void clearMilestoneForm() {
        milestoneNameField.clear();
        milestoneDescriptionArea.clear();
        milestoneDeadlinePicker.setValue(null);
        milestoneProjectBox.getSelectionModel().clearSelection();
    }

    private Manager findManagerById(int id) {
        return projectManagerBox.getItems().stream()
                .filter(m -> m.getId() == id)
                .findFirst().orElse(null);
    }

    private Project findProjectById(int id) {
        return milestoneProjectBox.getItems().stream()
                .filter(p -> p.getId() == id)
                .findFirst().orElse(null);
    }

    private String getManagerNameById(int id) {
        Manager manager = findManagerById(id);
        return manager == null ? "?" : manager.getFullName();
    }

    private String getProjectNameById(int id) {
        Project project = findProjectById(id);
        return project == null ? "Unknown Project" : project.getName();
    }

    private void showSuccessAlert(String message) {
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