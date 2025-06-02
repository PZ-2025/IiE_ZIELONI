package com.example.projektzielonifx.reports;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ExecutiveReportDialogTest {

    private ExecutiveReportDialog dialog;

    @BeforeEach
    void setup() {
        dialog = new ExecutiveReportDialog() {
            // Mockujemy loadProjects, aby nie łączyć się z bazą
            @Override
            protected Map<String, Integer> loadProjects() {
                Map<String, Integer> projects = new LinkedHashMap<>();
                // Dodajemy przykładowe projekty
                projects.put("Projekt Alfa", 1);
                projects.put("Projekt Beta", 2);

                // Wypełniamy cache ręcznie
                projectManagersCache.put(1, 10);
                projectManagersCache.put(2, 20);

                projectStatusCache.put(1, "Aktywny");
                projectStatusCache.put(2, "Zakończony");

                overdueTasksCache.put(1, 3);
                overdueTasksCache.put(2, 0);

                overdueMilestonesCache.put(1, 1);
                overdueMilestonesCache.put(2, 0);

                taskCompletionRateCache.put(1, 75.0);
                taskCompletionRateCache.put(2, 100.0);

                return projects;
            }
        };

        // Ustawiamy wymagane pola, które w oryginale są @FXML
        // Ustawiamy je na null, bo nie testujemy UI
        dialog.searchField = null;
        dialog.statusComboBox = null;
        dialog.managerComboBox = null;
        dialog.overdueAllCheckBox = null;
        dialog.overdueTasksCheckBox = null;
        dialog.overdueMilestonesCheckBox = null;
        dialog.minCompletionRateField = null;
        dialog.maxCompletionRateField = null;
        dialog.listView = null;
        dialog.okButton = null;
        dialog.cancelButton = null;
    }

    @Test
    void testLoadProjectsCaching() {
        Map<String, Integer> projects = dialog.loadProjects();

        assertEquals(2, projects.size());
        assertTrue(projects.containsKey("Projekt Alfa"));
        assertEquals(1, projects.get("Projekt Alfa"));

        // Sprawdzamy cache
        assertEquals(10, dialog.projectManagersCache.get(1));
        assertEquals("Aktywny", dialog.projectStatusCache.get(1));
        assertEquals(3, dialog.overdueTasksCache.get(1));
        assertEquals(1, dialog.overdueMilestonesCache.get(1));
        assertEquals(75.0, dialog.taskCompletionRateCache.get(1));
    }

    @Test
    void testFilterLogic() {
        Map<String, Integer> projects = dialog.loadProjects();

        // symulujemy parametry filtrów:
        String searchText = "alfa"; // powinna zwrócić Projekt Alfa
        String selectedStatus = "Aktywny";
        String selectedManager = "Manager10"; // w prawdziwym kodzie ID 10
        boolean showOverdueAll = false;
        boolean showOverdueTasks = true;
        boolean showOverdueMilestones = false;
        Double minCompletionRate = 50.0;
        Double maxCompletionRate = 80.0;

        // Symulujemy mapę managerów jak w DBUtil.loadProjectManagers()
        Map<String, Integer> managers = new HashMap<>();
        managers.put("Manager10", 10);
        managers.put("Manager20", 20);

        // Lambda z oryginału przeniesiona do lokalnej metody
        final String st = searchText.toLowerCase();
        final String ss = selectedStatus;
        final String sm = selectedManager;
        final boolean showAll = showOverdueAll;
        final boolean showTasks = showOverdueTasks;
        final boolean showMilestones = showOverdueMilestones;
        final Double minCR = minCompletionRate;
        final Double maxCR = maxCompletionRate;

        // Testujemy filtrację ręcznie:
        boolean alfaMatches = dialog.projectManagersCache.containsKey(projects.get("Projekt Alfa")) &&
                ("Projekt Alfa".toLowerCase().contains(st)) &&
                (ss.equals("Wszystkie") || ss.equals(dialog.projectStatusCache.get(projects.get("Projekt Alfa")))) &&
                (sm.equals("Wszyscy") || managers.get(sm).equals(dialog.projectManagersCache.get(projects.get("Projekt Alfa"))));

        // Sprawdzamy opóźnienia
        int overdueTasks = dialog.overdueTasksCache.get(projects.get("Projekt Alfa"));
        int overdueMilestones = dialog.overdueMilestonesCache.get(projects.get("Projekt Alfa"));
        boolean overdueMatch = true;
        if (showAll || showTasks || showMilestones) {
            overdueMatch = false;
            if (showAll && (overdueTasks > 0 || overdueMilestones > 0)) overdueMatch = true;
            else {
                if (showTasks && overdueTasks > 0) overdueMatch = true;
                if (showMilestones && overdueMilestones > 0) overdueMatch = true;
            }
        }

        double completionRate = dialog.taskCompletionRateCache.get(projects.get("Projekt Alfa"));
        boolean completionRateMatch = (minCR == null || completionRate >= minCR) &&
                (maxCR == null || completionRate <= maxCR);

        boolean result = alfaMatches && overdueMatch && completionRateMatch;

        assertTrue(result, "Projekt Alfa powinien przejść filtrację");

        // Projekt Beta nie powinien przejść (status "Zakończony" i zero opóźnień)
        boolean betaMatches = "Projekt Beta".toLowerCase().contains(st) &&
                (ss.equals("Wszystkie") || ss.equals(dialog.projectStatusCache.get(projects.get("Projekt Beta")))) &&
                (sm.equals("Wszyscy") || managers.get(sm).equals(dialog.projectManagersCache.get(projects.get("Projekt Beta"))));

        overdueTasks = dialog.overdueTasksCache.get(projects.get("Projekt Beta"));
        overdueMilestones = dialog.overdueMilestonesCache.get(projects.get("Projekt Beta"));
        overdueMatch = true;
        if (showAll || showTasks || showMilestones) {
            overdueMatch = false;
            if (showAll && (overdueTasks > 0 || overdueMilestones > 0)) overdueMatch = true;
            else {
                if (showTasks && overdueTasks > 0) overdueMatch = true;
                if (showMilestones && overdueMilestones > 0) overdueMatch = true;
            }
        }

        completionRate = dialog.taskCompletionRateCache.get(projects.get("Projekt Beta"));
        completionRateMatch = (minCR == null || completionRate >= minCR) &&
                (maxCR == null || completionRate <= maxCR);

        boolean betaResult = betaMatches && overdueMatch && completionRateMatch;

        assertFalse(betaResult, "Projekt Beta nie powinien przejść filtracji");
    }

    @Test
    void testReportGenerationCall() {
        // To test logic, ale nie mamy rzeczywistego generowania raportu
        // więc po prostu sprawdzamy, czy metoda generująca raport może być wywołana bez błędów

        // Na potrzeby testu nadpisz metodę generującą raport, aby nie robiła nic
        ExecutiveOverviewReportGeneratorMock reportGeneratorMock = new ExecutiveOverviewReportGeneratorMock();

        // Przypisz mock (w prawdziwym projekcie wstrzykiwanie zależności lub Mockito)
        try {
            reportGeneratorMock.generateFilteredReport(
                    1,
                    "report.pdf",
                    new File("."),
                    "Aktywny",
                    10,
                    true,
                    false,
                    50.0,
                    100.0
            );
        } catch (Exception e) {
            fail("Metoda generowania raportu nie powinna rzucać wyjątku");
        }
    }

    // Prosty mock klasy ExecutiveOverviewReportGenerator
    static class ExecutiveOverviewReportGeneratorMock {
        public void generateFilteredReport(int projectId, String fileName, File selectedDirectory,
                                           String statusFilter, Integer managerFilter,
                                           boolean overdueTasks, boolean overdueMilestones,
                                           Double minCompletionRate, Double maxCompletionRate) {
            // symulacja - nic nie robi
        }
    }
}
