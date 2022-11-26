package com.manydesigns.portofino.database;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
