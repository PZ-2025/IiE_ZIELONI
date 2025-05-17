package com.example.projektzielonifx.models;

import javafx.beans.property.StringProperty;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    public void testConstructorAndGetters() {
        // Arrange
        int id = 1;
        String firstName = "Jan";
        String lastName = "Kowalski";
        String role = "Developer";
        String team = "Alpha";
        String hireDate = "2023-01-15";
        String login = "jank";
        String createdAt = "2023-01-01";

        // Act
        User user = new User(id, firstName, lastName, role, team, hireDate, login, createdAt);

        // Assert
        assertEquals(id, user.getId());
        assertEquals(firstName, user.getFirstName());
        assertEquals(lastName, user.getLastName());
        assertEquals(role, user.getRole());
        assertEquals(team, user.getTeam());
        assertEquals(hireDate, user.getHireDate());
        assertEquals(login, user.getLogin());
        assertEquals(createdAt, user.getCreatedAt());
    }

    @Test
    public void testSetters() {
        // Arrange
        User user = new User(1, "Jan", "Kowalski", "Dev", "Alpha", "2023-01-15", "jank", "2023-01-01");

        // Act
        user.setFirstName("Anna");
        user.setLastName("Nowak");
        user.setRole("Tester");
        user.setTeam("Beta");
        user.setHireDate("2023-02-01");
        user.setLogin("annan");
        user.setCreatedAt("2023-01-20");
        user.setId(2);

        // Assert
        assertEquals(2, user.getId());
        assertEquals("Anna", user.getFirstName());
        assertEquals("Nowak", user.getLastName());
        assertEquals("Tester", user.getRole());
        assertEquals("Beta", user.getTeam());
        assertEquals("2023-02-01", user.getHireDate());
        assertEquals("annan", user.getLogin());
        assertEquals("2023-01-20", user.getCreatedAt());
    }

    @Test
    public void testProperties() {
        User user = new User(3, "Maria", "Wiśniewska", "Manager", "Omega", "2022-12-01", "mariaw", "2022-11-30");

        StringProperty firstNameProperty = user.firstNameProperty();
        firstNameProperty.set("Magda");

        assertEquals("Magda", user.getFirstName());
        assertEquals("Magda", user.firstNameProperty().get());
    }
}
