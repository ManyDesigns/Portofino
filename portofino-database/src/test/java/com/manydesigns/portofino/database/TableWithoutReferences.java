package com.manydesigns.portofino.database;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity(name = "table_without_references")
@Table(name = "TABLE_WITHOUT_REFERENCES")
public class TableWithoutReferences {

    private long id;
    private String contents;

    @Id
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column(name = "text")
    public String getContents() {
        return contents;
    }

    public void setContents(String text) {
        this.contents = text;
    }
}
