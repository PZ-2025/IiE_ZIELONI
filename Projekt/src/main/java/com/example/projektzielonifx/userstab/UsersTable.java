package com.example.projektzielonifx.userstab;

import com.example.projektzielonifx.InitializableWithId;
import com.example.projektzielonifx.database.DBUtil;
import com.example.projektzielonifx.models.User;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import java.net.URL;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;

import java.sql.SQLException;
import java.util.ResourceBundle;

import static com.example.projektzielonifx.database.DBUtil.*;


/**
 * Kontroler odpowiedzialny za wyświetlanie i zarządzanie tabelą użytkowników.
 * Implementuje interfejs Initializable do inicjalizacji elementów interfejsu.
 */
public class UsersTable implements Initializable, InitializableWithId {
    @FXML protected Button backButton;
    @FXML protected Button addButton;
    @FXML protected TableView<User> tableUsers;
    @FXML protected TableColumn<User, Integer> idCol;
    @FXML protected TableColumn<User, String> fnameCol;
    @FXML protected TableColumn<User, String> lnameCol;
    @FXML protected TableColumn<User, String> createdCol;
    @FXML protected TableColumn<User, String> loginCol;
    @FXML protected TableColumn<User, String> hireCol;
    @FXML protected TableColumn<User, String> roleCol;
    @FXML protected TableColumn<User, String> teamCol;
    /**
     * Identyfikator zalogowanego użytkownika.
     */
    protected int userId;
    protected int privilegeLevel;

    public void initializeWithId(int userId) {
        this.userId = userId;
        privilegeLevel = DBUtil.getLevel(userId);
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set up table columns
//        idCol.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        fnameCol.setCellValueFactory(cellData -> cellData.getValue().firstNameProperty());
        lnameCol.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());
        loginCol.setCellValueFactory(cellData -> cellData.getValue().loginProperty());
        roleCol.setCellValueFactory(cellData -> cellData.getValue().roleProperty());
        teamCol.setCellValueFactory(cellData -> cellData.getValue().teamProperty());
        createdCol.setCellValueFactory(cellData -> cellData.getValue().createdAtProperty());
        hireCol.setCellValueFactory(cellData -> cellData.getValue().hireDateProperty());

        // Load user data
        loadUserData();

        // Set up double-click event handler for editing users
        tableUsers.setOnMouseClicked(this::handleTableClick);

        // Set up button actions
        backButton.setOnAction(event -> {
            changeScene(event, "/com/example/projektzielonifx/home/HomePage.fxml", "Home Page", userId, 700, 1000);
        });
        if(privilegeLevel != 4) {
            addButton.setVisible(false);
            addButton.setManaged(false);
        }
        addButton.setOnAction(event -> {
            DBUtil.changeScene(event, "/com/example/projektzielonifx/userstab/add_user.fxml",
                    "Add User", userId, 650,600);
        });
    }

    /**
     * Handles mouse click events on the table.
     * Opens edit form on double-click.
     */
    protected void handleTableClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            User selectedUser = tableUsers.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                if(privilegeLevel != 4 && selectedUser.getId() != userId) {
                    showAlert("Wrong user","You can only edit your own data.",AlertType.WARNING);
                } else {
                    openEditUserForm(selectedUser);
                }
            }
        }
    }

    /**
     * Opens the edit user form with the selected user's data.
     */
    protected void openEditUserForm(User user) {
        try {
            // Store the user to be edited in a way that AddUser can access it
            // You'll need to modify your changeScene method or create a new one that can pass user data
            DBUtil.changeSceneWithUser(
                    backButton.getScene().getWindow(),
                    "/com/example/projektzielonifx/userstab/add_user.fxml",
                    "Edit User - " + user.getFirstName() + " " + user.getLastName(),
                    userId,
                    650,
                    600,
                    user
            );
        } catch (Exception e) {
            showErrorAlert("Error opening edit form", "Unable to open edit form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected void loadUserData() {
        try {
            tableUsers.setItems(DBUtil.getUsers());
        } catch (Exception e) {
            showErrorAlert("Error loading users", "Unable to load user data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected void showErrorAlert(String title, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}