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
        String qualifiedName = "jpetstore.PUBLIC";
        Schema schema = DatabaseLogic.findSchemaByQualifiedName(model, qualifiedName);
        assertNotNull(schema);
        assertEquals(qualifiedName, schema.getQualifiedName());

        String dummyName = "jpetstore.foo";
        schema = DatabaseLogic.findSchemaByQualifiedName(model, dummyName);
        assertNull(schema);
    }

    public void testFindTableByQualifiedName() {
        String qualifiedName = "jpetstore.PUBLIC.product";
        Table table = DatabaseLogic.findTableByQualifiedName(model, qualifiedName);
        assertNotNull(table);
        assertEquals(qualifiedName, table.getQualifiedName());

        String dummyName = "jpetstore.PUBLIC.foo";
        table = DatabaseLogic.findTableByQualifiedName(model, dummyName);
        assertNull(table);
    }

    public void testFindColumnByQualifiedName() {
        String qualifiedName = "jpetstore.PUBLIC.product.category";
        Column column = DatabaseLogic.findColumnByQualifiedName(model, qualifiedName);
        assertNotNull(column);
        assertEquals(qualifiedName, column.getQualifiedName());

        String dummyName = "jpetstore.PUBLIC.product.foo";
        column = DatabaseLogic.findColumnByQualifiedName(model, dummyName);
        assertNull(column);
    }
}
