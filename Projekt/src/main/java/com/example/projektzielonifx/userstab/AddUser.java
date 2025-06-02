package com.example.projektzielonifx.userstab;

import com.example.projektzielonifx.InitializableWithId;
import com.example.projektzielonifx.database.DBUtil;
import com.example.projektzielonifx.models.User;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.time.LocalDate;

import static com.example.projektzielonifx.database.DBUtil.*;

public class AddUser implements InitializableWithId  {
    public Button deleteButton;
    public VBox rootContainer;
    public VBox mainContent;
    protected int userId;

    @FXML
    protected TextField nameField;

    @FXML
    protected TextField surnameField;

    @FXML
    protected TextField loginField;

    @FXML
    protected PasswordField passwordField;

    @FXML
    protected DatePicker dateOfHirePicker;

    @FXML
    protected ChoiceBox<String> roleChoiceBox;

    @FXML
    protected ChoiceBox<String> teamChoiceBox;

    @FXML
    protected Button backButton;

    @FXML
    protected Button cancelButton;

    @FXML
    protected Button saveButton;

    @FXML
    protected Text titleText;

    protected User editingUser;
    protected boolean isEditMode = false;
    protected int privilegeLevel;

    @Override
    public void initializeWithId(int userId) {
        this.userId = userId;
        privilegeLevel = getLevel(userId);
        setupForm();
        deleteButton.setVisible(false);
        deleteButton.setManaged(false);
    }

    /**
     * Initialize with user data for editing
     */
    public void initializeWithUser(int userId, User userToEdit) {
        this.userId = userId;
        this.editingUser = userToEdit;
        this.isEditMode = true;
        privilegeLevel = getLevel(userId);

        setupForm();
        populateFormWithUserData();
        if(privilegeLevel != 4) {
            deleteButton.setVisible(false);
            deleteButton.setManaged(false);
        } else {
            deleteButton.setVisible(true);
            deleteButton.setManaged(true);
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

    protected void setupForm() {
        // Load choice box data
        roleChoiceBox.setItems(FXCollections.observableList(DBUtil.loadRoles()));
        teamChoiceBox.setItems(DBUtil.getTeams());

        // Update title based on mode
        if (titleText != null) {
            titleText.setText(isEditMode ? "Add User" : "Add New User");
        }

        // Update save button text
        if (saveButton != null) {
            saveButton.setText(isEditMode ? "Update User" : "Save User");
        }

        // Set up button actions
        backButton.setOnAction(event -> {
            changeScene(event, "/com/example/projektzielonifx/userstab/UsersTable.fxml", "Team Members", userId, 700, 1000);
        });

        cancelButton.setOnAction(event -> {
            changeScene(event, "/com/example/projektzielonifx/userstab/UsersTable.fxml", "Team Members", userId, 700, 1000);
        });

        saveButton.setOnAction(event -> {
            handleSaveAction(event);
        });

        deleteButton.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("WARNING");
            alert.setHeaderText("You are about to delete this user");
            alert.setContentText("Are you sure you want to delete this user?");
            alert.showAndWait().ifPresent(response -> {
                if (response.equals(ButtonType.OK)) {
                    DBUtil.deleteUser(editingUser.getId());
                    showAlert("Deleted!","Deleted a user " +editingUser.getFirstName() + " " +editingUser.getLastName(),Alert.AlertType.INFORMATION );
                    changeScene(event, "/com/example/projektzielonifx/userstab/UsersTable.fxml", "Team Members", userId, 700, 1000);
                }
                else if (response.equals(ButtonType.CANCEL)) {
                    alert.close();
                }
            });
        });
        System.out.println(privilegeLevel);
        if(privilegeLevel != 4) {
            nameField.setDisable(true);
            surnameField.setDisable(true);
            loginField.setDisable(true);
            dateOfHirePicker.setDisable(true);
            roleChoiceBox.setDisable(true);
            teamChoiceBox.setDisable(true);
        } else {
            nameField.setDisable(false);
            surnameField.setDisable(false);
            loginField.setDisable(false);
            dateOfHirePicker.setDisable(false);
            roleChoiceBox.setDisable(false);
            teamChoiceBox.setDisable(false);
        }

        setupFocusClearing();
    }

    /**
     * Populate form fields with user data for editing
     */
    protected void populateFormWithUserData() {
        if (editingUser != null) {
            nameField.setText(editingUser.getFirstName());
            surnameField.setText(editingUser.getLastName());
            loginField.setText(editingUser.getLogin());

            // For security, don't populate password field when editing
            passwordField.setPromptText("Leave empty to keep current password");

            // Parse and set hire date
            if (editingUser.getHireDate() != null && !editingUser.getHireDate().isEmpty()) {
                try {
                    LocalDate hireDate = LocalDate.parse(editingUser.getHireDate());
                    dateOfHirePicker.setValue(hireDate);
                } catch (Exception e) {
                    System.err.println("Error parsing hire date: " + e.getMessage());
                }
            }

            // Set role and team
            if (editingUser.getRole() != null) {
                roleChoiceBox.setValue(editingUser.getRole());
            }
            if (editingUser.getTeam() != null) {
                teamChoiceBox.setValue(editingUser.getTeam());
            }

            if(privilegeLevel == 4) {
                deleteButton.setVisible(true);
                deleteButton.setManaged(true);
            }
        }
    }

    protected void handleSaveAction(ActionEvent event) {
        if (!isFormValid()) return;

        // Create new user object if not editing
        if (editingUser == null) {
            editingUser = new User(0, null, null, null, null, null, null, null, null);
        }

        // Update user data from form
        editingUser.setFirstName(nameField.getText().trim());
        editingUser.setLastName(surnameField.getText().trim());
        editingUser.setLogin(loginField.getText().trim());

        // Only update password if it's provided (for editing) or if it's a new user
        String password = passwordField.getText().trim();
        if (!password.isEmpty() || !isEditMode) {
            editingUser.setPasswordHash(password);
        }

        editingUser.setHireDate(String.valueOf(dateOfHirePicker.getValue()));
        editingUser.setRole(roleChoiceBox.getValue());
        editingUser.setTeam(teamChoiceBox.getValue());

        // Check login uniqueness
        boolean loginExists = DBUtil.loginExists(editingUser.getLogin(), editingUser.getId());
        if (loginExists) {
            showAlert("Błąd", "Login już istnieje. Wprowadź inny.", Alert.AlertType.WARNING);
            return;
        }

        // Save or update user
        boolean success;
        String successMessage;

        if (isEditMode && editingUser.getId() != 0) {
            success = DBUtil.updateUser(editingUser);
            successMessage = "Dane użytkownika zostały zaktualizowane.";
        } else {
            success = DBUtil.saveUser(editingUser);
            successMessage = "Nowy użytkownik został dodany.";
        }

        if (success) {
            showAlert("Sukces", successMessage, Alert.AlertType.INFORMATION);
            // Return to users table
            changeScene(event, "/com/example/projektzielonifx/userstab/UsersTable.fxml", "Users", userId, 700, 1000);
        } else {
            showAlert("Błąd", "Błąd przy zapisywaniu danych użytkownika.", Alert.AlertType.ERROR);
        }
    }

    protected boolean isFormValid() {
        // For editing, password can be empty (means keep current password)
        boolean passwordRequired = !isEditMode;
        boolean passwordValid = !passwordRequired || !passwordField.getText().trim().isEmpty();

        if (nameField.getText().trim().isEmpty() ||
                surnameField.getText().trim().isEmpty() ||
                loginField.getText().trim().isEmpty() ||
                !passwordValid ||
                dateOfHirePicker.getValue() == null ||
                roleChoiceBox.getValue() == null ||
                teamChoiceBox.getValue() == null) {

            String message = isEditMode ?
                    "Wypełnij wszystkie pola poprawnie! (Hasło można zostawić puste aby zachować obecne)" :
                    "Wypełnij wszystkie pola poprawnie!";
            showAlert("Błąd", message, Alert.AlertType.WARNING);
            return false;
        }

        if (!loginField.getText().matches("^[a-zA-Z0-9._-]{3,}$")) {
            showAlert("Błąd", "Login musi zawierać co najmniej 3 znaki (litery, cyfry, _ . -)", Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }
}