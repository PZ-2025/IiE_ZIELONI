package com.example.projektzielonifx.userstab;

import com.example.projektzielonifx.database.DBUtil;
import com.example.projektzielonifx.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.net.URL;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UsersTableTest {

    private UsersTable controller;

    @BeforeEach
    public void setUp() {
        controller = new UsersTable();

        // Ręczna inicjalizacja pól FXML (upewnij się, że nie masz problemów z NullPointerException)
        controller.backButton = new Button();
        controller.addButton = new Button();
        controller.tableUsers = new TableView<>();
        controller.idCol = new TableColumn<>();
        controller.fnameCol = new TableColumn<>();
        controller.lnameCol = new TableColumn<>();
        controller.loginCol = new TableColumn<>();
        controller.roleCol = new TableColumn<>();
        controller.teamCol = new TableColumn<>();
        controller.createdCol = new TableColumn<>();
        controller.hireCol = new TableColumn<>();
    }

    @Test
    public void testInitialize_SetsUpColumnsAndButtons() {
        // Uruchom initialize
        controller.initialize(mock(URL.class), mock(ResourceBundle.class));

        // Sprawdź, czy kolumny mają ustawione CellValueFactory (nie null)
        assertNotNull(controller.idCol.getCellValueFactory());
        assertNotNull(controller.fnameCol.getCellValueFactory());
        assertNotNull(controller.lnameCol.getCellValueFactory());
        assertNotNull(controller.loginCol.getCellValueFactory());
        assertNotNull(controller.roleCol.getCellValueFactory());
        assertNotNull(controller.teamCol.getCellValueFactory());
        assertNotNull(controller.createdCol.getCellValueFactory());
        assertNotNull(controller.hireCol.getCellValueFactory());

        // Sprawdź, czy przyciski mają akcje przypisane
        assertNotNull(controller.backButton.getOnAction());
        assertNotNull(controller.addButton.getOnAction());
    }

    @Test
    public void testLoadUserData_SetsTableItems() throws Exception {
        ObservableList<User> fakeUsers = FXCollections.observableArrayList();

        try (MockedStatic<DBUtil> dbUtilMock = Mockito.mockStatic(DBUtil.class)) {
            dbUtilMock.when(DBUtil::getUsers).thenReturn(fakeUsers);

            controller.tableUsers = new TableView<>();
            controller.loadUserData();

            // Sprawdź, czy tabela ma ustawione elementy
            assertEquals(fakeUsers, controller.tableUsers.getItems());
        }
    }

    @Test
    public void testBackButtonAction_CallsChangeScene() {
        try (MockedStatic<DBUtil> dbUtilMock = Mockito.mockStatic(DBUtil.class)) {
            controller.userId = 123;

            controller.backButton = new Button();
            controller.initialize(mock(URL.class), mock(ResourceBundle.class));

            // Wymyślamy akcję eventu (np. nowy ActionEvent)
            ActionEvent event = new ActionEvent(controller.backButton, null);

            // Podmiana statycznej metody changeScene
            dbUtilMock.when(() -> DBUtil.changeScene(event, "/com/example/projektzielonifx/home/HomePage.fxml", "Home Page", controller.userId, 700, 1000))
                    .then(invocation -> null);

            // Wywołaj akcję
            controller.backButton.getOnAction().handle(event);

            // Sprawdź, że metoda changeScene została wywołana
            dbUtilMock.verify(() -> DBUtil.changeScene(event, "/com/example/projektzielonifx/home/HomePage.fxml", "Home Page", controller.userId, 700, 1000));
        }
    }

    @Test
    public void testAddButtonAction_ShowsInformationAlert() {
        controller.addButton = new Button();
        controller.initialize(mock(URL.class), mock(ResourceBundle.class));

        // Zwyczajnie wywołujemy akcję i upewniamy się, że nie ma wyjątków
        controller.addButton.getOnAction().handle(new ActionEvent());

        // Tu można rozbudować test o mockowanie Alert (np. przy pomocy narzędzi do testowania GUI),
        // ale podstawowo nie powinno być wyjątków ani błędów.
    }
}
