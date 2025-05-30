package com.example.projektzielonifx.models;

import java.time.LocalDate;

public class Project {

    private int id;
    private int managerId;
    private String name;
    private int progress;           // 0-100
    private String status;          // planowany | wTrakcie | zakonczony | anulowany
    private LocalDate startDate;
    private LocalDate endDate;      // null → wciąż trwa

    /* ===== KONSTRUKTOR PEŁNY ===== */
    public Project(int id,
                   int managerId,
                   String name,
                   int progress,
                   String status,
                   LocalDate startDate,
                   LocalDate endDate) {

        this.id = id;
        this.managerId = managerId;
        this.name = name;
        this.progress = progress;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /* ===== GETTERS / SETTERS ===== */
    // --- ID ---------------------------------------------------------
    public int getId()                 { return id; }
    public void setId(int id)          { this.id = id; }

    // --- MANAGER ----------------------------------------------------
    public int getManagerId()          { return managerId; }
    public void setManagerId(int id)   { this.managerId = id; }

    // --- NAME -------------------------------------------------------
    public String getName()            { return name; }
    public void setName(String name)   { this.name = name; }

    // --- PROGRESS ---------------------------------------------------
    public int getProgress()           { return progress; }
    public void setProgress(int p)     { this.progress = p; }

    // --- STATUS -----------------------------------------------------
    public String getStatus()          { return status; }
    public void setStatus(String s)    { this.status = s; }

    // --- DATES ------------------------------------------------------
    public LocalDate getStartDate()            { return startDate; }
    public void      setStartDate(LocalDate d) { this.startDate = d; }

    public LocalDate getEndDate()              { return endDate; }
    public void      setEndDate(LocalDate d)   { this.endDate = d; }

    /* ===== DEBUG / LOG ===== */
    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", managerId=" + managerId +
                ", name='" + name + '\'' +
                ", progress=" + progress +
                ", status='" + status + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}
