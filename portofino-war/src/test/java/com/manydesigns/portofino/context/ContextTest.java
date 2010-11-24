package com.manydesigns.portofino.context;

import com.manydesigns.portofino.AbstractPortofinoTest;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.datamodel.Column;
import com.manydesigns.portofino.model.datamodel.Database;
import com.manydesigns.portofino.model.datamodel.Schema;
import com.manydesigns.portofino.model.datamodel.Table;

/**
 * File created on Jul 4, 2010 at 8:45:44 PM
 * Copyright Paolo Predonzani (paolo.predonzani@gmail.com)
 * All rights reserved
 */
public class ContextTest extends AbstractPortofinoTest {


    Model model;

    public void setUp() throws Exception {
        super.setUp();
        model = context.getModel();
    }


    public void testFindDatabaseByName() {
        String qualifiedName = "jpetstore";
        Database database = model.findDatabaseByName(qualifiedName);
        assertNotNull(database);
        assertEquals(qualifiedName, database.getDatabaseName());

        String dummyName = "foo";
        database = model.findDatabaseByName(dummyName);
        assertNull(database);
    }

    public void testFindSchemaByQualifiedName() {
        String qualifiedName = "jpetstore.PUBLIC";
        Schema schema = model.findSchemaByQualifiedName(qualifiedName);
        assertNotNull(schema);
        assertEquals(qualifiedName, schema.getQualifiedName());

        String dummyName = "jpetstore.foo";
        schema = model.findSchemaByQualifiedName(dummyName);
        assertNull(schema);
    }

    public void testFindTableByQualifiedName() {
        String qualifiedName = "jpetstore.PUBLIC.product";
        Table table = model.findTableByQualifiedName(qualifiedName);
        assertNotNull(table);
        assertEquals(qualifiedName, table.getQualifiedName());

        String dummyName = "jpetstore.PUBLIC.foo";
        table = model.findTableByQualifiedName(dummyName);
        assertNull(table);
    }

    public void testFindColumnByQualifiedName() {
        String qualifiedName = "jpetstore.PUBLIC.product.category";
        Column column = model.findColumnByQualifiedName(qualifiedName);
        assertNotNull(column);
        assertEquals(qualifiedName, column.getQualifiedName());

        String dummyName = "jpetstore.PUBLIC.product.foo";
        column = model.findColumnByQualifiedName(dummyName);
        assertNull(column);
    }
}
