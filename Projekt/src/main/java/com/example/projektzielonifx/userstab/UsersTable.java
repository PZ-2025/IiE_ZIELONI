package com.example.projektzielonifx.userstab;

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

import java.sql.SQLException;
import java.util.ResourceBundle;

import static com.example.projektzielonifx.database.DBUtil.changeScene;


/**
 * Kontroler odpowiedzialny za wyświetlanie i zarządzanie tabelą użytkowników.
 * Implementuje interfejs Initializable do inicjalizacji elementów interfejsu.
 */
public class UsersTable implements Initializable {
    @FXML private Button backButton;
    @FXML private TableView tableUsers;
    @FXML private TableColumn<User, Integer> idCol;
    @FXML private TableColumn<User,String> fnameCol;
    @FXML private TableColumn<User, String> lnameCol;
    @FXML private TableColumn<User, String> createdCol;
    @FXML private TableColumn<User, String> loginCol;
    @FXML private TableColumn<User, String> hireCol;
    @FXML private TableColumn<User, String> roleCol;
    @FXML private TableColumn<User, String> teamCol;
    /**
     * Identyfikator zalogowanego użytkownika.
     */
    private int userId;

    public void initData(int userId) {
        this.userId = userId;
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        idCol.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        fnameCol.setCellValueFactory(cellData -> cellData.getValue().firstNameProperty());
        lnameCol.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());
        loginCol.setCellValueFactory(cellData -> cellData.getValue().loginProperty());
        roleCol.setCellValueFactory(cellData -> cellData.getValue().roleProperty());
        teamCol.setCellValueFactory(cellData -> cellData.getValue().teamProperty());
        createdCol.setCellValueFactory(cellData -> cellData.getValue().createdAtProperty());
        hireCol.setCellValueFactory(cellData -> cellData.getValue().hireDateProperty());

        tableUsers.setItems(DBUtil.getUsers());

        backButton.setOnAction(event -> {
            changeScene(event, "/com/example/projektzielonifx/home/HomePage.fxml", "Home Page", userId, 700, 1000);
            return; // Success - exit method
        });
    }
}
