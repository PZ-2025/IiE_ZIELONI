package com.example.projektzielonifx.reports;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class ProjectReportDialogTest {

    @Test
    void testManualFieldAssignment() {
        // given
        ProjectReportDialog dialog = new ProjectReportDialog();
        String expectedFileName = "test.pdf";
        File expectedDirectory = new File("testDir");
        int expectedUserId = 1;

        // when
        dialog.fileName = expectedFileName;
        dialog.selectedDirectory = expectedDirectory;
        dialog.userId = expectedUserId;

        // then
        assertEquals(expectedFileName, dialog.fileName);
        assertEquals(expectedDirectory, dialog.selectedDirectory);
        assertEquals(expectedUserId, dialog.userId);
        System.out.println("Test klasy ProjectReportDialog wykonano poprawnie.");
    }
}
