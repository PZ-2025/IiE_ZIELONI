package com.example.projektzielonifx.models;

public class ProjectModel {
    private int user_id;
    private String first_name;
    private String last_name;
    private String role;
    private String team;
    private String hire_date;
    private String login;
    private String user_created_at;
    private String manager_name;
    private String team_leader_name;
    private String projects_assigned;
    private String milestone_assigned;
    private String tasks_assigned;
    private String total_tasks;
    private String todo;
    private String in_progress;
    private String done;
    private String canceled;

    public ProjectModel(int user_id, String first_name, String last_name, String role, String team, String hire_date, String login, String user_created_at, String manager_name, String team_leader_name, String projects_assigned, String milestone_assigned, String tasks_assigned, String total_tasks, String todo, String in_progress, String done, String canceled) {
        this.user_id = user_id;
        this.first_name = first_name;
        this.last_name = last_name;
        this.role = role;
        this.team = team;
        this.hire_date = hire_date;
        this.login = login;
        this.user_created_at = user_created_at;
        this.manager_name = manager_name;
        this.team_leader_name = team_leader_name;
        this.projects_assigned = projects_assigned;
        this.milestone_assigned = milestone_assigned;
        this.tasks_assigned = tasks_assigned;
        this.total_tasks = total_tasks;
        this.todo = todo;
        this.in_progress = in_progress;
        this.done = done;
        this.canceled = canceled;
    }

    public int getUser_id() {
        return user_id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public String getRole() {
        return role;
    }

    public String getTeam() {
        return team;
    }

    public String getHire_date() {
        return hire_date;
    }

    public String getLogin() {
        return login;
    }

    public String getUser_created_at() {
        return user_created_at;
    }

    public String getManager_name() {
        return manager_name;
    }

    public String getTeam_leader_name() {
        return team_leader_name;
    }

    public String getProjects_assigned() {
        return projects_assigned;
    }

    public String getMilestone_assigned() {
        return milestone_assigned;
    }

    public String getTasks_assigned() {
        return tasks_assigned;
    }

    public String getTotal_tasks() {
        return total_tasks;
    }

    public String getTodo() {
        return todo;
    }

    public String getIn_progress() {
        return in_progress;
    }

    public String getDone() {
        return done;
    }

    public String getCanceled() {
        return canceled;
    }
}

