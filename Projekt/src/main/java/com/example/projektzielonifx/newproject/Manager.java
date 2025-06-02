package com.example.projektzielonifx.newproject;

public class Manager {
    protected final int id;
    protected final String fullName;   // Imię + nazwisko

    public Manager(int id, String fullName) {
        this.id = id;
        this.fullName = fullName;
    }

    public int getId()           { return id; }
    public String getFullName()  { return fullName; }

    @Override public String toString() { return fullName; }
}
