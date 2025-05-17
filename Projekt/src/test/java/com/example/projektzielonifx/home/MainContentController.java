package com.example.projektzielonifx.home;

import com.example.projektzielonifx.database.DBUtil;
import javafx.scene.control.Label;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class MainContentControllerTest {

    private MainContentController controller;

    @BeforeEach
    public void setUp() {
        controller = new MainContentController();
        controller.welcomeLabel = new Label(); // Fikcyjna etykieta do testu
    }

    /**
     * Testuje czy metoda initData ustawia poprawnie tekst powitalny.
     */
    @Test
    public void testInitData_setsWelcomeLabel() {
        int testUserId = 123;

        // Mock statycznej metody DBUtil.getUsernameById()
        try (MockedStatic<DBUtil> dbUtilMock = mockStatic(DBUtil.class)) {
            dbUtilMock.when(() -> DBUtil.getUsernameById(testUserId)).thenReturn("Anna");

            controller.initData(testUserId);

            assertEquals("Hello Anna!", controller.welcomeLabel.getText());
        }
    }
}
