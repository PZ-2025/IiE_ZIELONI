package com.example.projektzielonifx.models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class  User {
    protected final IntegerProperty id;
    protected final StringProperty first_name;
    protected final StringProperty last_name;
    protected final StringProperty role;
    protected final StringProperty team;
    protected final StringProperty hire_date;
    protected final StringProperty login;

    protected final StringProperty created_at;
    protected final SimpleStringProperty passwordHash;

    public User(int id, String firstName, String lastName, String role, String team, String hireDate, String login, String passwordHash, String createdAt) {
     this.id = new SimpleIntegerProperty(id);
     this.first_name = new SimpleStringProperty(firstName);
     this.last_name = new SimpleStringProperty(lastName);
     this.role = new SimpleStringProperty(role);
     this.team = new SimpleStringProperty(team);
     this.hire_date = new SimpleStringProperty(hireDate);
     this.login = new SimpleStringProperty(login);
     this.passwordHash = new SimpleStringProperty(passwordHash);
     this.created_at = new SimpleStringProperty(createdAt);
    }


    public IntegerProperty idProperty() {return id;}
    public StringProperty firstNameProperty() {return first_name;}
    public StringProperty lastNameProperty() {return last_name;}
    public StringProperty roleProperty() {return role;}
    public StringProperty teamProperty() {return team;}
    public StringProperty hireDateProperty() {return hire_date;}
    public StringProperty loginProperty() {return login;}
    public StringProperty createdAtProperty() {return created_at;}
    public int getId() {return id.get();}
    public void setId(int id) {this.id.set(id);}
    public String getFirstName() {return first_name.get();}
    public void setFirstName(String firstName) {this.first_name.set(firstName);}
    public String getLastName() {return last_name.get();}
    public void setLastName(String lastName) {this.last_name.set(lastName);}
    public String getRole() {return role.get();}
    public void setRole(String role) {this.role.set(role);}
    public String getTeam() {return team.get();}
    public void setTeam(String team) {this.team.set(team);}
    public String getHireDate() {return hire_date.get();}

    public String getPassword() {return passwordHash.get();}
    public void setPasswordHash(String passwordHash) {this.passwordHash.set(passwordHash);}

    public void setHireDate(String hireDate) {this.hire_date.set(hireDate);}
    public String getLogin() {return login.get();}
    public void setLogin(String login) {this.login.set(login);}
    public String getCreatedAt() {return created_at.get();}
    public void setCreatedAt(String createdAt) {this.created_at.set(createdAt);}
}
