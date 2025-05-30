package com.example.projektzielonifx.newproject;

public class Manager {
    private final int id;
    private final String fullName;   // Imię + nazwisko

    public Manager(int id, String fullName) {
        this.id = id;
        this.fullName = fullName;
    }

    public int getId()           { return id; }
    public String getFullName()  { return fullName; }

    @Override public String toString() { return fullName; }
}
