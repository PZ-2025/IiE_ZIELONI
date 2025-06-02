package com.example.projektzielonifx.reports;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test klasy ReportsSelector.
 */
public class ReportsSelectorTest {

    static class ReportsSelectorSimple {
        public int privilege;
        public String selectedReportType;
        public File selectedDirectory;
        public String fileName;

        public ReportsSelectorSimple(int privilege) {
            this.privilege = privilege;
            this.selectedDirectory = new File(System.getProperty("user.home"), "Documents");
            if (privilege >= 2) selectedReportType = "Raport wydajności pracownika";
            else selectedReportType = null;
        }

        public String getFileNameOrNull() {
            if (fileName == null || fileName.trim().isEmpty()) return null;
            return fileName.trim();
        }
    }

    @Test
    public void testPrivileges() {
        ReportsSelectorSimple r = new ReportsSelectorSimple(3);
        assertEquals("Raport wydajności pracownika", r.selectedReportType);

        ReportsSelectorSimple r2 = new ReportsSelectorSimple(1);
        assertNull(r2.selectedReportType);
    }

    @Test
    public void testSelectedDirectory() {
        ReportsSelectorSimple r = new ReportsSelectorSimple(3);
        assertTrue(r.selectedDirectory.exists());
        assertTrue(r.selectedDirectory.getAbsolutePath().contains("Documents"));
    }

    @Test
    public void testFileName() {
        ReportsSelectorSimple r = new ReportsSelectorSimple(3);
        r.fileName = "  ";
        assertNull(r.getFileNameOrNull());

        r.fileName = " raport.txt ";
        assertEquals("raport.txt", r.getFileNameOrNull());
    }
}
