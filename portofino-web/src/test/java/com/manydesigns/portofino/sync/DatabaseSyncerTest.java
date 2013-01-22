/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * There are special exceptions to the terms and conditions of the GPL
 * as it is applied to this software. View the full text of the
 * exception in file OPEN-SOURCE-LICENSE.txt in the directory of this
 * software distribution.
 *
 * This program is distributed WITHOUT ANY WARRANTY; and without the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see http://www.gnu.org/licenses/gpl.txt
 * or write to:
 * Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307  USA
 *
 */

package com.manydesigns.portofino.sync;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.manydesigns.portofino.AbstractPortofinoTest;
import com.manydesigns.portofino.model.Annotation;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.database.ConnectionProvider;
import com.manydesigns.portofino.model.database.*;
import liquibase.database.structure.ForeignKeyConstraintType;

import java.util.Arrays;
import java.util.List;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class DatabaseSyncerTest extends AbstractPortofinoTest {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public void setUp() throws Exception {
        super.setUp();
        ((Logger) DatabaseSyncer.logger).setLevel(Level.DEBUG);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSyncDatabase() throws Exception {
        String databaseName = "jpetstore";
        ConnectionProvider connectionProvider = application.getConnectionProvider(databaseName);
        DatabaseSyncer dbSyncer = new DatabaseSyncer(connectionProvider);
        Model sourceModel = new Model();
        Database targetDatabase = dbSyncer.syncDatabase(sourceModel);
        assertNotNull(targetDatabase);
        assertEquals(databaseName, targetDatabase.getDatabaseName());

        List<Schema> schemas = targetDatabase.getSchemas();
        assertEquals(1, schemas.size());

        Schema schema = schemas.get(0);
        assertEquals(targetDatabase, schema.getDatabase());
        assertEquals("PUBLIC", schema.getSchemaName());

        List<Table> tables = schema.getTables();
        assertEquals(13, tables.size());

        Table table = tables.get(0);
        assertEquals("SEQUENCE", table.getTableName());
        assertEquals(schema, table.getSchema());
        assertTrue(table.getAnnotations().isEmpty());
        assertNull(table.getJavaClass());
        assertNull(table.getEntityName());

        List<Column> columns = table.getColumns();
        assertEquals(2, columns.size());

        Column column = columns.get(0);
        assertEquals(table, column.getTable());
        assertNull(column.getJavaType());
        assertTrue(column.getAnnotations().isEmpty());
        assertNull(column.getPropertyName());
        assertEquals("NAME", column.getColumnName());
        assertEquals("VARCHAR", column.getColumnType());
        assertFalse(column.isNullable());
        assertFalse(column.isAutoincrement());
        assertEquals(30, (int) column.getLength());
        assertEquals(0, (int) column.getScale());

        column = columns.get(1);
        assertEquals("NEXTID", column.getColumnName());
        assertEquals("INT", column.getColumnType());
        assertFalse(column.isNullable());
        assertFalse(column.isAutoincrement());
        assertEquals(10, (int) column.getLength());
        assertEquals(0, (int) column.getScale());


        table = tables.get(3);
        assertEquals("PRODUCT", table.getTableName());
        List<ForeignKey> foreignKeys = table.getForeignKeys();
        assertEquals(1, foreignKeys.size());
        ForeignKey foreignKey = foreignKeys.get(0);
        assertEquals(table, foreignKey.getFromTable());
        assertEquals("FK_PRODUCT_1", foreignKey.getName());
        assertEquals("CATEGORY", foreignKey.getToTableName());
        assertEquals(schema.getSchemaName(), foreignKey.getToSchema());
        assertEquals(databaseName, foreignKey.getToDatabase());
        assertEquals(ForeignKeyConstraintType.importedKeyRestrict.name(),
                foreignKey.getOnUpdate());
        assertEquals(ForeignKeyConstraintType.importedKeyRestrict.name(),
                foreignKey.getOnDelete());
        List<Reference> references = foreignKey.getReferences();
        assertEquals(1, references.size());
        Reference reference = references.get(0);
        assertEquals(foreignKey, reference.getOwner());
        assertEquals("CATEGORY", reference.getFromColumn());
        assertEquals("CATID", reference.getToColumn());


        table = tables.get(1);
        assertEquals("SUPPLIER", table.getTableName());
        assertEquals(schema, table.getSchema());

        PrimaryKey primaryKey = table.getPrimaryKey();
        assertNotNull(primaryKey);
        assertEquals(table, primaryKey.getTable());
        assertEquals("PK_SUPPLIER", primaryKey.getPrimaryKeyName());
        List<PrimaryKeyColumn> pkColumns = primaryKey.getPrimaryKeyColumns();
        assertEquals(1, pkColumns.size());
        PrimaryKeyColumn pkColumn = pkColumns.get(0);
        assertEquals(primaryKey, pkColumn.getPrimaryKey());
        assertEquals("SUPPID", pkColumn.getColumnName());
        assertNull(pkColumn.getGenerator());

        columns = table.getColumns();
        assertEquals(9, columns.size());

        column = columns.get(1);
        assertEquals("NAME", column.getColumnName());
        assertEquals("VARCHAR", column.getColumnType());
        assertTrue(column.isNullable());
        assertFalse(column.isAutoincrement());
        assertEquals(80, (int) column.getLength());
        assertEquals(0, (int) column.getScale());


        table = tables.get(2);
        assertEquals("ITEM", table.getTableName());
        assertEquals(schema, table.getSchema());

        columns = table.getColumns();
        assertEquals(11, columns.size());

        column = columns.get(2);
        assertEquals("LISTPRICE", column.getColumnName());
        assertEquals("DECIMAL", column.getColumnType());
        assertTrue(column.isNullable());
        assertFalse(column.isAutoincrement());
        assertEquals(10, (int) column.getLength());
        assertEquals(2, (int) column.getScale());


        //Test modifica e sync successivo
        sourceModel.getDatabases().clear();
        sourceModel.getDatabases().add(targetDatabase);
        Database sourceDatabase = targetDatabase;
        schema = sourceDatabase.getSchemas().get(0);

        // prendiamo SEQUENCE per tabella e colonne
        table = schema.getTables().get(0);
        table.setJavaClass("com.foo.Bar");
        table.setEntityName("entity.name");
        PrimaryKey wrongPrimaryKey = new PrimaryKey();
        wrongPrimaryKey.setPrimaryKeyName("wrongName");
        table.setPrimaryKey(wrongPrimaryKey);

        Annotation annotation = new Annotation(table, "pippo");
        table.getAnnotations().add(annotation);
        annotation.getValues().add("pluto");

        column = table.getColumns().get(0);
        column.setJavaType("x.y.Z");
        column.setPropertyName("some.property");

        annotation = new Annotation(column, "cip");
        column.getAnnotations().add(annotation);
        annotation.getValues().add("ciop");

        // prendiamo SUPPLIER per la primary key
        table = schema.getTables().get(1);
        primaryKey = table.getPrimaryKey();
        pkColumn = primaryKey.getPrimaryKeyColumns().get(0);
        assertNull(pkColumn.getGenerator());
        Generator generator = new IncrementGenerator(pkColumn);
        pkColumn.setGenerator(generator);

        // prendiamo PRODUCT per la foreign key
        table = schema.getTables().get(3);
        foreignKey = table.getForeignKeys().get(0);
        foreignKey.setManyPropertyName("many");
        foreignKey.setOnePropertyName("one");

        List<ModelSelectionProvider> selectionProviders =
                table.getSelectionProviders();
        assertTrue(selectionProviders.isEmpty());
        ModelSelectionProvider selectionProvider =
                new DatabaseSelectionProvider(table);
        selectionProviders.add(selectionProvider);
        reference = new Reference(selectionProvider);
        selectionProvider.getReferences().add(reference);
        reference.setFromColumn("from_selection");
        reference.setToColumn("to_selection");

        //Sync
        targetDatabase = dbSyncer.syncDatabase(sourceModel);

        schema = targetDatabase.getSchemas().get(0);

        // riprendiamo SEQUENCE
        table = schema.getTables().get(0);
        primaryKey = table.getPrimaryKey();
        assertNotSame(wrongPrimaryKey, primaryKey);
        assertFalse("wrongName".equals(primaryKey.getPrimaryKeyName()));
        assertEquals("com.foo.Bar", table.getJavaClass());
        assertEquals("entity.name", table.getEntityName());
        List<Annotation> annotations = table.getAnnotations();
        assertEquals(1, annotations.size());
        annotation = annotations.get(0);
        assertEquals(table, annotation.getParent());
        assertEquals("pippo", annotation.getType());
        assertEquals(Arrays.asList(new String[] {"pluto"}), annotation.getValues());

        column = table.getColumns().get(0);
        assertEquals("x.y.Z", column.getJavaType());
        assertEquals("some.property", column.getPropertyName());
        annotations = column.getAnnotations();
        assertEquals(1, annotations.size());
        annotation = annotations.get(0);
        assertEquals(column, annotation.getParent());
        assertEquals("cip", annotation.getType());
        assertEquals(Arrays.asList(new String[]{"ciop"}), annotation.getValues());

        // riprendiamo SUPPLIER
        table = schema.getTables().get(1);
        primaryKey = table.getPrimaryKey();
        pkColumn = primaryKey.getPrimaryKeyColumns().get(0);
        generator = pkColumn.getGenerator();
        assertNotNull(generator);
        assertTrue(generator instanceof IncrementGenerator);

        // riprendiamo PRODUCT per la foreign key
        table = schema.getTables().get(3);
        foreignKey = table.getForeignKeys().get(0);
        assertEquals("many", foreignKey.getManyPropertyName());
        assertEquals("one", foreignKey.getOnePropertyName());

        selectionProviders = table.getSelectionProviders();
        assertEquals(1, selectionProviders.size());
        selectionProvider = selectionProviders.get(0);
        assertEquals(table, selectionProvider.getFromTable());
        assertTrue(selectionProvider instanceof DatabaseSelectionProvider);
        DatabaseSelectionProvider dsp = (DatabaseSelectionProvider) selectionProvider;
        assertEquals(1, dsp.getReferences().size());
        reference = dsp.getReferences().get(0);
        assertEquals("from_selection", reference.getFromColumn());
        assertEquals("to_selection", reference.getToColumn());
    }
}
