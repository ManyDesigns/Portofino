package com.manydesigns.portofino.context;

import com.manydesigns.portofino.context.hibernate.HibernateContextImpl;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.datamodel.Column;
import com.manydesigns.portofino.model.datamodel.Database;
import com.manydesigns.portofino.model.datamodel.Schema;
import com.manydesigns.portofino.model.datamodel.Table;
import junit.framework.TestCase;

/**
 * File created on Jul 4, 2010 at 8:45:44 PM
 * Copyright Paolo Predonzani (paolo.predonzani@gmail.com)
 * All rights reserved
 */
public class ContextTest extends TestCase {

    Context context;
    Model model;

    public void setUp() {
        context = new HibernateContextImpl();
        context.loadConnectionsAsResource("portofino-connections.xml");
        context.loadXmlModelAsResource(
                "databases/jpetstore/postgresql/jpetstore-postgres.xml");
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
        String qualifiedName = "jpetstore.public";
        Schema schema = model.findSchemaByQualifiedName(qualifiedName);
        assertNotNull(schema);
        assertEquals(qualifiedName, schema.getQualifiedName());

        String dummyName = "jpetstore.foo";
        schema = model.findSchemaByQualifiedName(dummyName);
        assertNull(schema);
    }

    public void testFindTableByQualifiedName() {
        String qualifiedName = "jpetstore.public.product";
        Table table = model.findTableByQualifiedName(qualifiedName);
        assertNotNull(table);
        assertEquals(qualifiedName, table.getQualifiedName());

        String dummyName = "jpetstore.public.foo";
        table = model.findTableByQualifiedName(dummyName);
        assertNull(table);
    }

    public void testFindColumnByQualifiedName() {
        String qualifiedName = "jpetstore.public.product.category";
        Column column = model.findColumnByQualifiedName(qualifiedName);
        assertNotNull(column);
        assertEquals(qualifiedName, column.getQualifiedName());

        String dummyName = "jpetstore.public.product.foo";
        column = model.findColumnByQualifiedName(dummyName);
        assertNull(column);
    }
}
