package com.example.projektzielonifx.ProjInfo;

import com.example.projektzielonifx.database.DBUtil;
import com.example.projektzielonifx.models.ProjectModel;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProjInfoTest {

    private ProjInfo projInfo;

    @BeforeEach
    public void setUp() {
        projInfo = new ProjInfo();

        // Utworzenie mocków pól FXML
        projInfo.welcomeLabel = new Label();
        projInfo.teamLabel = new Label();
        projInfo.roleLabel = new Label();
        projInfo.managerLabel = new Label();
        projInfo.teamLeadLabel = new Label();
        projInfo.projectNamesLabel = new Label();
        projInfo.milestonesLabel = new Label();
        projInfo.totalTasksLabel = new Label();
        projInfo.doneTasksLabel = new Label();
        projInfo.progressPercentLabel = new Label();
        projInfo.progressBar = new ProgressBar();
    }

    @Test
    public void testInitializeWithId_setsLabelsAndProgress() {
        int userId = 1;
        ProjectModel mockProject = new ProjectModel(
                1, "Jan", "Kowalski", "Developer", "Alpha", "2023-01-01", "jank", "2023-01-01",
                "Manager Name", "Team Leader Name", "Project1, Project2", "Milestone1", "5",
                "10", "3", "7", "2", "1"
        );

        // Mockowanie statycznej metody DBUtil.findByUserId
        try (MockedStatic<DBUtil> dbUtilMockedStatic = mockStatic(DBUtil.class)) {
            dbUtilMockedStatic.when(() -> DBUtil.findByUserId(userId)).thenReturn(mockProject);

            projInfo.initializeWithId(userId);

            // Sprawdzenie, czy etykiety zostały poprawnie ustawione
            assertEquals("Welcome, Jan Kowalski", projInfo.welcomeLabel.getText());
            assertEquals("Alpha", projInfo.teamLabel.getText());
            assertEquals("Developer", projInfo.roleLabel.getText());
            assertEquals("Manager Name", projInfo.managerLabel.getText());
            assertEquals("Team Leader Name", projInfo.teamLeadLabel.getText());
            assertEquals("Project1, Project2", projInfo.projectNamesLabel.getText());
            assertEquals("Milestone1", projInfo.milestonesLabel.getText());
            assertEquals("10", projInfo.totalTasksLabel.getText());
            assertEquals("7", projInfo.doneTasksLabel.getText());

            // Progress = done / total = 7/10 = 0.7
            assertEquals(0.7, projInfo.progressBar.getProgress(), 0.01);
            assertEquals("70%", projInfo.progressPercentLabel.getText());
        }
    }

    @Test
    public void testInitializeWithId_handlesNumberFormatException() {
        int userId = 2;
        ProjectModel badProject = new ProjectModel(
                2, "Anna", "Nowak", "Tester", "Beta", "2023-02-01", "annan", "2023-02-01",
                "Manager2", "TeamLeader2", "ProjX", "MileX", "abc", // total_tasks is not a number
                "xyz", "0", "0", "0", "0", "0"
        );

        try (MockedStatic<DBUtil> dbUtilMockedStatic = mockStatic(DBUtil.class)) {
            dbUtilMockedStatic.when(() -> DBUtil.findByUserId(userId)).thenReturn(badProject);

            projInfo.initializeWithId(userId);

            // Progress should default to 0 and percent to "0%"
            assertEquals(0, projInfo.progressBar.getProgress(), 0.01);
            assertEquals("0%", projInfo.progressPercentLabel.getText());
        }
    }
}
