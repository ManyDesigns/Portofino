package com.manydesigns.portofino.application;

import com.manydesigns.portofino.AbstractPortofinoTest;
import com.manydesigns.portofino.logic.DataModelLogic;
import com.manydesigns.portofino.model.datamodel.Model;
import com.manydesigns.portofino.model.datamodel.*;

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
        Database database = DataModelLogic.findDatabaseByName(model, qualifiedName);
        assertNotNull(database);
        assertEquals(qualifiedName, database.getDatabaseName());

        String dummyName = "foo";
        database = DataModelLogic.findDatabaseByName(model, dummyName);
        assertNull(database);
    }

    public void testFindSchemaByQualifiedName() {
        String qualifiedName = "jpetstore.PUBLIC";
        Schema schema = DataModelLogic.findSchemaByQualifiedName(model, qualifiedName);
        assertNotNull(schema);
        assertEquals(qualifiedName, schema.getQualifiedName());

        String dummyName = "jpetstore.foo";
        schema = DataModelLogic.findSchemaByQualifiedName(model, dummyName);
        assertNull(schema);
    }

    public void testFindTableByQualifiedName() {
        String qualifiedName = "jpetstore.PUBLIC.product";
        Table table = DataModelLogic.findTableByQualifiedName(model, qualifiedName);
        assertNotNull(table);
        assertEquals(qualifiedName, table.getQualifiedName());

        String dummyName = "jpetstore.PUBLIC.foo";
        table = DataModelLogic.findTableByQualifiedName(model, dummyName);
        assertNull(table);
    }

    public void testFindColumnByQualifiedName() {
        String qualifiedName = "jpetstore.PUBLIC.product.category";
        Column column = DataModelLogic.findColumnByQualifiedName(model, qualifiedName);
        assertNotNull(column);
        assertEquals(qualifiedName, column.getQualifiedName());

        String dummyName = "jpetstore.PUBLIC.product.foo";
        column = DataModelLogic.findColumnByQualifiedName(model, dummyName);
        assertNull(column);
    }
}
