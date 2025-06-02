package com.example.projektzielonifx.newproject;


import java.util.List;

public class ManagerService {
    protected final ManagerManager dao = new ManagerManager();
    public List<Manager> list() { return dao.getAll(); }
}
