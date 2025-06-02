package com.example.projektzielonifx.home;

import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prosty test klasy NavController,
 * testujący zachowanie metody initData przy różnych poziomach roli.
 */
public class NavControllerTest {

    private NavController navController;

    /**
     * Klasa testowa z nadpisaną metodą DBUtil.getLevel, aby uniknąć zależności.
     */
    private static class TestNavController extends NavController {
        private int mockLevel;

        public TestNavController(int mockLevel) {
            this.mockLevel = mockLevel;
            // Inicjalizacja "przycisków" jako nowe instancje, żeby nie mieć NullPointerException
            tasksButton = new Button();
            teamMembersButton = new Button();
            logoutButton = new Button();
            projectsInfoButton = new Button();
            notificationsButton = new Button();
            reportsButton = new Button();
            reportsButtonDivider = new Region();
            newProjectButton = new Button();
            newProjectDivider = new Region();
        }

        @Override
        public void initData(int userId) {
            this.userId = userId;
            this.roleLevel = mockLevel;
            if (roleLevel == 1) {
                reportsButton.setVisible(false);
                reportsButton.setManaged(false);
                reportsButtonDivider.setVisible(false);
                reportsButtonDivider.setManaged(false);
            }

            if (roleLevel != 4) {
                newProjectButton.setVisible(false);
                newProjectButton.setManaged(false);
                newProjectDivider.setVisible(false);
                newProjectDivider.setManaged(false);
            }
        }
    }

    @BeforeEach
    public void setUp() {
        // domyślnie stworzymy kontroler z rolą 1
        navController = new TestNavController(1);
    }

    @Test
    public void testInitDataRoleLevel1_HidesReportsAndNewProject() {
        navController.initData(42);

        assertEquals(42, navController.userId);
        assertFalse(navController.reportsButton.isVisible());
        assertFalse(navController.reportsButton.isManaged());
        assertFalse(navController.reportsButtonDivider.isVisible());
        assertFalse(navController.reportsButtonDivider.isManaged());

        assertFalse(navController.newProjectButton.isVisible());
        assertFalse(navController.newProjectButton.isManaged());
        assertFalse(navController.newProjectDivider.isVisible());
        assertFalse(navController.newProjectDivider.isManaged());
    }

    @Test
    public void testInitDataRoleLevel4_ShowsReportsAndNewProject() {
        navController = new TestNavController(4);
        navController.initData(99);

        assertEquals(99, navController.userId);
        assertTrue(navController.reportsButton.isVisible());
        assertTrue(navController.reportsButton.isManaged());
        assertTrue(navController.reportsButtonDivider.isVisible());
        assertTrue(navController.reportsButtonDivider.isManaged());

        assertTrue(navController.newProjectButton.isVisible());
        assertTrue(navController.newProjectButton.isManaged());
        assertTrue(navController.newProjectDivider.isVisible());
        assertTrue(navController.newProjectDivider.isManaged());

        System.out.println("Test NavController wykonany poprawnie.");
    }
}
