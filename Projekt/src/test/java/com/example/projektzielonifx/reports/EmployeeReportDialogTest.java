package com.example.projektzielonifx.reports;

import static org.junit.jupiter.api.Assertions.*;

import com.example.projektzielonifx.reports.EmployeeReportDialog;
import org.junit.jupiter.api.Test;

public class EmployeeReportDialogTest {

    /**
     * Testuje walidację wartości performance.
     */
    @Test
    public void testValidatePerformanceFields() {
        // poprawne przypadki
        assertTrue(EmployeeReportDialog.validatePerformanceFields("", ""));
        assertTrue(EmployeeReportDialog.validatePerformanceFields("0", "100"));
        assertTrue(EmployeeReportDialog.validatePerformanceFields("50", "50"));
        assertTrue(EmployeeReportDialog.validatePerformanceFields(null, "80"));
        assertTrue(EmployeeReportDialog.validatePerformanceFields("10.5", "90.3"));

        // niepoprawne przypadki
        assertFalse(EmployeeReportDialog.validatePerformanceFields("abc", "90"));
        assertFalse(EmployeeReportDialog.validatePerformanceFields("10", "xyz"));
        assertFalse(EmployeeReportDialog.validatePerformanceFields("-1", "50"));
        assertFalse(EmployeeReportDialog.validatePerformanceFields("0", "150"));
        assertFalse(EmployeeReportDialog.validatePerformanceFields("80", "70")); // min > max

        System.out.println("EmployeeReportDialogTest.testValidatePerformanceFields wykonano poprawnie");
    }
}
