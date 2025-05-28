package com.example.projektzielonifx.userstab;

import com.example.projektzielonifx.InitializableWithId;
import com.example.projektzielonifx.database.DBUtil;
import com.example.projektzielonifx.models.User;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import static com.example.projektzielonifx.database.DBUtil.changeScene;
import static com.example.projektzielonifx.database.DBUtil.showAlert;

public class AddUser implements InitializableWithId  {
    private int userId;

    @FXML
    private TextField nameField;

    @FXML
    private TextField surnameField;

    @FXML
    private TextField loginField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private DatePicker dateOfHirePicker;

    @FXML
    private ChoiceBox<String> roleChoiceBox;

    @FXML
    private ChoiceBox<String> teamChoiceBox;

    @FXML
    private Button backButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button saveButton;

    private User editingUser;

    @Override
    public void initializeWithId(int userId) {
        this.userId = userId;

        roleChoiceBox.setItems(FXCollections.observableList(DBUtil.loadRoles()));
        teamChoiceBox.setItems(DBUtil.getTeams());

        backButton.setOnAction(event -> {
            changeScene(event, "/com/example/projektzielonifx/userstab/UsersTable.fxml", "Team Members", userId, 700, 1000);
        });

        cancelButton.setOnAction(event -> {
            changeScene(event, "/com/example/projektzielonifx/userstab/UsersTable.fxml", "Team Members", userId, 700, 1000);
        });

        saveButton.setOnAction(event -> {
            if (!isFormValid()) return;
            if(editingUser == null) editingUser = new User(0,null,null,null,null,null,null,null,null);
            showAlert("Poprawne","poprawne",Alert.AlertType.INFORMATION);

            editingUser.setFirstName(nameField.getText().trim());
            editingUser.setLastName(surnameField.getText().trim());
            editingUser.setLogin(loginField.getText().trim());
            editingUser.setPasswordHash(passwordField.getText().trim());
            editingUser.setHireDate(String.valueOf(dateOfHirePicker.getValue()));
            editingUser.setRole(roleChoiceBox.getValue());
            editingUser.setTeam(teamChoiceBox.getValue());

            // Sprawdź unikalność loginu tylko jeśli dodajemy nowego użytkownika lub login się zmienił
            boolean loginExists = DBUtil.loginExists(editingUser.getLogin(), editingUser.getId());
            if (loginExists) {
                showAlert("Bład","Login już istnieje. Wprowadź inny.", Alert.AlertType.WARNING);
                return;
            }

            boolean success;
            if (editingUser.getId() == 0) {
                success = DBUtil.saveUser(editingUser);
            } else {
                success = DBUtil.updateUser(editingUser);
            }

            if(success) {
                showAlert("Poprawne","poprawne",Alert.AlertType.INFORMATION);
            } else {
                showAlert("Bład","Bład przy zapisywaniu",Alert.AlertType.ERROR);
            }

        });
    }

    private boolean isFormValid() {
        if (nameField.getText().trim().isEmpty() ||
                surnameField.getText().trim().isEmpty() ||
                loginField.getText().trim().isEmpty() ||
                passwordField.getText().trim().isEmpty() ||
                dateOfHirePicker.getValue() == null ||
                roleChoiceBox.getValue() == null ||
                teamChoiceBox.getValue() == null) {
            showAlert("Bład", "Wypełnij wszystkie pola poprawnie!", Alert.AlertType.WARNING);
            return false;
        }

        if (!loginField.getText().matches("^[a-zA-Z0-9._-]{3,}$")) {
            showAlert("Bład","Login musi zawierać co najmniej 3 znaki (litery, cyfry, _ . -)", Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }
}
