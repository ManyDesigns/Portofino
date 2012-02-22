package com.manydesigns.portofino.pageactions.jsgantt.model;



import java.util.ArrayList;
import java.util.List;

public class ProjectModel {
    final List<Task> tasks;

    public ProjectModel() {
        tasks = new ArrayList<Task>();
    }

    public List<Task> getTasks() {
        return tasks;
    }
}
