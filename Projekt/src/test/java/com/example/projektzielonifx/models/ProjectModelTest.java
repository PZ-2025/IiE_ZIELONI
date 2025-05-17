package com.example.projektzielonifx.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ProjectModelTest {

    @Test
    public void testProjectModelConstructorAndGetters() {
        // Arrange
        int userId = 1;
        String firstName = "Anna";
        String lastName = "Nowak";
        String role = "Developer";
        String team = "Green";
        String hireDate = "2023-01-01";
        String login = "anowak";
        String createdAt = "2022-12-15";
        String manager = "Marek Kowalski";
        String teamLeader = "Jan Zielony";
        String projects = "3";
        String milestones = "5";
        String tasks = "12";
        String totalTasks = "20";
        String todo = "5";
        String inProgress = "4";
        String done = "10";
        String canceled = "1";

        // Act
        ProjectModel model = new ProjectModel(
                userId, firstName, lastName, role, team, hireDate, login, createdAt,
                manager, teamLeader, projects, milestones, tasks, totalTasks,
                todo, inProgress, done, canceled
        );

        // Assert
        assertEquals(userId, model.getUser_id());
        assertEquals(firstName, model.getFirst_name());
        assertEquals(lastName, model.getLast_name());
        assertEquals(role, model.getRole());
        assertEquals(team, model.getTeam());
        assertEquals(hireDate, model.getHire_date());
        assertEquals(login, model.getLogin());
        assertEquals(createdAt, model.getUser_created_at());
        assertEquals(manager, model.getManager_name());
        assertEquals(teamLeader, model.getTeam_leader_name());
        assertEquals(projects, model.getProjects_assigned());
        assertEquals(milestones, model.getMilestone_assigned());
        assertEquals(tasks, model.getTasks_assigned());
        assertEquals(totalTasks, model.getTotal_tasks());
        assertEquals(todo, model.getTodo());
        assertEquals(inProgress, model.getIn_progress());
        assertEquals(done, model.getDone());
        assertEquals(canceled, model.getCanceled());
    }
}
