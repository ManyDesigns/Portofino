package com.manydesigns.portofino.database;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity(name = "table1")
@Table(name = "TABLE1")
public class Table1 {

    private long id;
    private String text;

    @Id
    @Column(name = "`ID`")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column(name = "testo")
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
