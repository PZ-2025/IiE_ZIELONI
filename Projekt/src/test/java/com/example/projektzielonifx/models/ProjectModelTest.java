package com.example.projektzielonifx.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy jednostkowe dla klasy {@link ProjectModel}.
 * Sprawdzają poprawność działania getterów w klasie modelu.
 */
class ProjectModelTest {

    /**
     * Testuje, czy wszystkie gettery klasy {@link ProjectModel} zwracają
     * wartości przekazane w konstruktorze.
     */
    @Test
    void testProjectModelGetters() {
        ProjectModel model = new ProjectModel(
                1,
                "Jan",
                "Kowalski",
                "Developer",
                "Zieloni",
                "2023-01-01",
                "jan.kowalski",
                "2023-01-02",
                "Anna Manager",
                "Piotr TeamLeader",
                "Project A",
                "Milestone 1",
                "Task 1",
                "10",
                "2",
                "5",
                "3",
                "0"
        );

        assertEquals(1, model.getUser_id());
        assertEquals("Jan", model.getFirst_name());
        assertEquals("Kowalski", model.getLast_name());
        assertEquals("Developer", model.getRole());
        assertEquals("Zieloni", model.getTeam());
        assertEquals("2023-01-01", model.getHire_date());
        assertEquals("jan.kowalski", model.getLogin());
        assertEquals("2023-01-02", model.getUser_created_at());
        assertEquals("Anna Manager", model.getManager_name());
        assertEquals("Piotr TeamLeader", model.getTeam_leader_name());
        assertEquals("Project A", model.getProjects_assigned());
        assertEquals("Milestone 1", model.getMilestone_assigned());
        assertEquals("Task 1", model.getTasks_assigned());
        assertEquals("10", model.getTotal_tasks());
        assertEquals("2", model.getTodo());
        assertEquals("5", model.getIn_progress());
        assertEquals("3", model.getDone());
        assertEquals("0", model.getCanceled());

        System.out.println("Test 'testProjectModelGetters' przebiegł prawidłowo.");
    }
}
