package com.example.projektzielonifx.newproject;

import com.example.projektzielonifx.models.Project;

import java.util.List;

public class ProjectService {
    private final ProjectManager manager = new ProjectManager();

    public List<Project>   list()          { return manager.getAll();      }
    public Project         one(int id)     { return manager.getById(id);   }
    public int             add(Project p)  { return manager.create(p);     }
    public boolean         save(Project p) { return manager.update(p);     }
    public boolean         remove(int id)  { return manager.delete(id);    }
}
