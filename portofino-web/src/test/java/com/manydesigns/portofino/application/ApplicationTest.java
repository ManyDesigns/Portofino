package com.manydesigns.portofino.application;

import com.manydesigns.portofino.AbstractPortofinoTest;
import com.manydesigns.portofino.model.database.DatabaseLogic;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.database.*;

/**
 * File created on Jul 4, 2010 at 8:45:44 PM
 * Copyright Paolo Predonzani (paolo.predonzani@gmail.com)
 * All rights reserved
 */
public class ApplicationTest extends AbstractPortofinoTest {


    Model model;

    public void setUp() throws Exception {
        super.setUp();
        model = application.getModel();
    }


    public void testFindDatabaseByName() {
        String qualifiedName = "jpetstore";
        Database database = DatabaseLogic.findDatabaseByName(model, qualifiedName);
        assertNotNull(database);
        assertEquals(qualifiedName, database.getDatabaseName());

        String dummyName = "foo";
        database = DatabaseLogic.findDatabaseByName(model, dummyName);
        assertNull(database);
    }

    public void testFindSchemaByQualifiedName() {
        Schema schema = DatabaseLogic.findSchemaByName(model, "jpetstore", "PUBLIC");
        assertNotNull(schema);
        assertEquals("jpetstore.PUBLIC", schema.getQualifiedName());

        schema = DatabaseLogic.findSchemaByName(model, "jpetstore", "foo");
        assertNull(schema);
    }

    public void testFindTableByQualifiedName() {
        Table table = DatabaseLogic.findTableByName(model, "jpetstore", "PUBLIC", "product");
        assertNotNull(table);
        assertEquals("jpetstore.PUBLIC.product", table.getQualifiedName());

        table = DatabaseLogic.findTableByName(model, "jpetstore", "PUBLIC", "foo");
        assertNull(table);
    }

    public void testFindColumnByQualifiedName() {
        Column column = DatabaseLogic.findColumnByName(model, "jpetstore", "PUBLIC", "product", "category");
        assertNotNull(column);
        assertEquals("jpetstore.PUBLIC.product.category", column.getQualifiedName());

        column = DatabaseLogic.findColumnByName(model, "jpetstore", "PUBLIC", "product", "foo");
        assertNull(column);
    }
}
