package com.manydesigns.portofino.base.context;

import com.manydesigns.portofino.base.model.Column;
import com.manydesigns.portofino.base.model.Database;
import com.manydesigns.portofino.base.model.Schema;
import com.manydesigns.portofino.base.model.Table;
import junit.framework.TestCase;

/**
 * File created on Jul 4, 2010 at 8:45:44 PM
 * Copyright Paolo Predonzani (paolo.predonzani@gmail.com)
 * All rights reserved
 */
public class MDContextTest extends TestCase {

    MDContext context;

    public void setUp() {
        context = new MDContext();
        context.loadXmlModelAsResource(
                "databases/jpetstore/postgresql/jpetstore-postgres.xml");
    }


    public void testFindDatabaseByName() {
        String qualifiedName = "jpetstore";
        Database database = null;
        try {
            database = context.findDatabaseByName(qualifiedName);
        } catch (ModelObjectNotFoundException e) {
            e.printStackTrace();
            fail();
        }
        assertEquals(qualifiedName, database.getDatabaseName());

        String dummyName = "foo";
        try {
            context.findDatabaseByName(dummyName);
            fail("Must throw exception.");
        } catch (ModelObjectNotFoundException e) {
            assertEquals(dummyName, e.getMessage());
        }
    }

    public void testFindSchemaByQualifiedName() {
        String qualifiedName = "jpetstore.public";
        Schema schema = null;
        try {
            schema = context.findSchemaByQualifiedName(qualifiedName);
        } catch (ModelObjectNotFoundException e) {
            e.printStackTrace();
            fail();
        }
        assertEquals(qualifiedName, schema.getQualifiedName());

        String dummyName = "jpetstore.foo";
        try {
            context.findSchemaByQualifiedName(dummyName);
            fail("Must throw exception.");
        } catch (ModelObjectNotFoundException e) {
            assertEquals(dummyName, e.getMessage());
        }
    }

    public void testFindTableByQualifiedName() {
        String qualifiedName = "jpetstore.public.product";
        Table table = null;
        try {
            table = context.findTableByQualifiedName(qualifiedName);
        } catch (ModelObjectNotFoundException e) {
            e.printStackTrace();
            fail();
        }
        assertEquals(qualifiedName, table.getQualifiedName());

        String dummyName = "jpetstore.public.foo";
        try {
            context.findTableByQualifiedName(dummyName);
            fail("Must throw exception.");
        } catch (ModelObjectNotFoundException e) {
            assertEquals(dummyName, e.getMessage());
        }
    }

    public void testFindColumnByQualifiedName() {
        String qualifiedName = "jpetstore.public.product.category";
        Column column = null;
        try {
            column = context.findColumnByQualifiedName(qualifiedName);
        } catch (ModelObjectNotFoundException e) {
            e.printStackTrace();
            fail();
        }
        assertEquals(qualifiedName, column.getQualifiedName());

        String dummyName = "jpetstore.public.product.foo";
        try {
            context.findColumnByQualifiedName(dummyName);
            fail("Must throw exception.");
        } catch (ModelObjectNotFoundException e) {
            assertEquals(dummyName, e.getMessage());
        }
    }
}
