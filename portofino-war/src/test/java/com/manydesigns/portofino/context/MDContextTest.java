package com.manydesigns.portofino.context;

import com.manydesigns.portofino.context.hibernate.MDContextHibernateImpl;
import com.manydesigns.portofino.model.*;
import junit.framework.TestCase;

/**
 * File created on Jul 4, 2010 at 8:45:44 PM
 * Copyright Paolo Predonzani (paolo.predonzani@gmail.com)
 * All rights reserved
 */
public class MDContextTest extends TestCase {

    MDContext context;
    DataModel dataModel;

    public void setUp() {
        context = new MDContextHibernateImpl();
        context.loadConnectionsAsResource("portofino-connections.xml");
        context.loadXmlModelAsResource(
                "databases/jpetstore/postgresql/jpetstore-postgres.xml");
        dataModel = context.getDataModel();
    }


    public void testFindDatabaseByName() {
        String qualifiedName = "jpetstore";
        Database database = dataModel.findDatabaseByName(qualifiedName);
        assertNotNull(database);
        assertEquals(qualifiedName, database.getDatabaseName());

        String dummyName = "foo";
        database = dataModel.findDatabaseByName(dummyName);
        assertNull(database);
    }

    public void testFindSchemaByQualifiedName() {
        String qualifiedName = "jpetstore.public";
        Schema schema = dataModel.findSchemaByQualifiedName(qualifiedName);
        assertNotNull(schema);
        assertEquals(qualifiedName, schema.getQualifiedName());

        String dummyName = "jpetstore.foo";
        schema = dataModel.findSchemaByQualifiedName(dummyName);
        assertNull(schema);
    }

    public void testFindTableByQualifiedName() {
        String qualifiedName = "jpetstore.public.product";
        Table table = dataModel.findTableByQualifiedName(qualifiedName);
        assertNotNull(table);
        assertEquals(qualifiedName, table.getQualifiedName());

        String dummyName = "jpetstore.public.foo";
        table = dataModel.findTableByQualifiedName(dummyName);
        assertNull(table);
    }

    public void testFindColumnByQualifiedName() {
        String qualifiedName = "jpetstore.public.product.category";
        Column column = dataModel.findColumnByQualifiedName(qualifiedName);
        assertNotNull(column);
        assertEquals(qualifiedName, column.getQualifiedName());

        String dummyName = "jpetstore.public.product.foo";
        column = dataModel.findColumnByQualifiedName(dummyName);
        assertNull(column);
    }
}
