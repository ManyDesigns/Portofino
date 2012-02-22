package com.manydesigns.portofino.pageactions.jsgantt.model;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class Task {

    int id;
    String name;
    DateTime start;
    DateTime end;
    String color;
    String link;
    int mile;
    String resource ;
    int comp;
    int group;
    Task parent;
    int open;
    final List<Integer> depend = new ArrayList<Integer>();

    public Task() {
    }

    public Task(int id, String name, DateTime start, DateTime end, String color,
                String link, int mile, String resource, int comp, int group,
                int open) {
        this.color = color;
        this.comp = comp;
        this.end = end;
        this.group = group;
        this.id = id;
        this.link = link;
        this.mile = mile;
        this.name = name;
        this.open = open;
        this.resource = resource;
        this.start = start;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getComp() {
        return comp;
    }

    public void setComp(int comp) {
        this.comp = comp;
    }

    public List<Integer> getDepend() {
        return depend;
    }


    public DateTime getEnd() {
        return end;
    }

    public void setEnd(DateTime end) {
        this.end = end;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public int getMile() {
        return mile;
    }

    public void setMile(int mile) {
        this.mile = mile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOpen() {
        return open;
    }

    public void setOpen(int open) {
        this.open = open;
    }

    public Task getParent() {
        return parent;
    }

    public void setParent(Task parent) {
        this.parent = parent;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public DateTime getStart() {
        return start;
    }

    public void setStart(DateTime start) {
        this.start = start;
    }
}
