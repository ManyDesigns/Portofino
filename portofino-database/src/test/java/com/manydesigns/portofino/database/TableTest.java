/*
 * Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.manydesigns.portofino.database;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.database.*;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.Assert.assertNotNull;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@Test
public class TableTest {

    @BeforeMethod
    public void setup() {
        ElementsThreadLocals.setupDefaultElementsContext();
    }

    @AfterMethod
    public void teardown() {
        ElementsThreadLocals.removeElementsContext();
    }

    public void testActualEntityNames(){
        Model model = new Model();
        Database db = new Database();
        db.setDatabaseName("portofino");
        Schema schema = new Schema();
        schema.setDatabase(db);
        schema.setSchemaName("meta");
        Table table = new Table();
        table.setSchema(schema);
        table.setTableName(" ab!!!..acus$%/()");
        model.getDatabases().add(db);
        table.init(model, null);

        assertNotNull(table.getActualEntityName());
        assertEquals("_ab_____acus$____", table.getActualEntityName());
        System.out.println(table.getActualEntityName());

        table = new Table();
        table.setSchema(schema);
        table.setTableName("0DPrpt");
        table.init(model, null);
        assertEquals("_0dprpt", table.getActualEntityName());
        System.out.println(table.getActualEntityName());


        db = new Database();
        db.setDatabaseName("1portofino");
        schema = new Schema();
        schema.setDatabase(db);
        schema.setSchemaName("meta");
        table = new Table();
        table.setSchema(schema);
        table.setTableName("0DPrpt");
        table.init(model, null);
        assertEquals("_0dprpt", table.getActualEntityName());
        System.out.println(table.getActualEntityName());

        db = new Database();
        db.setDatabaseName("$1portofino");
        schema = new Schema();
        schema.setDatabase(db);
        schema.setSchemaName("meta");
        table = new Table();
        table.setSchema(schema);
        table.setTableName("0DPrpt");
        table.init(model, null);
        assertEquals("_0dprpt", table.getActualEntityName());
        System.out.println(table.getActualEntityName());

        db = new Database();
        db.setDatabaseName(".portofino");
        table.setTableName("0DPrpt");
        table.init(model, null);
        assertEquals("_0dprpt", table.getActualEntityName());
        System.out.println(table.getActualEntityName());

        table.setEntityName(null);
        table.setTableName("XYZéèçò°àùì");
        table.init(model, null);
        assertEquals("xyzéèçò_àùì", table.getActualEntityName());
        System.out.println(table.getActualEntityName());

        table.setEntityName(null);
        table.setTableName("ĖĔĕĘĘŜŞŝōŎľĿʛʋʊɪɩɨɷ");
        table.init(model, null);
        assertEquals("ĖĔĕĘĘŜŞŝōŎľĿʛʋʊɪɩɨɷ", table.getActualEntityName());
        System.out.println(table.getActualEntityName());
    }

    public void testActualColumnNames() {
        Model model = new Model();
        Database db = new Database();
        db.setConnectionProvider(new JdbcConnectionProvider());
        db.setDatabaseName("portofino");
        Schema schema = new Schema();
        schema.setDatabase(db);
        db.getSchemas().add(schema);
        schema.setSchemaName("meta");
        Table table = new Table();
        table.setSchema(schema);
        table.setTableName("ignore");
        schema.getTables().add(table);
        Column column = new Column();
        column.setTable(table);
        column.setColumnName(" ab!!!..acus$%/()");
        column.setColumnType("varchar");
        column.setLength(0);
        column.setScale(0);
        table.getColumns().add(column);
        model.getDatabases().add(db);
        model.init(new PropertiesConfiguration());

        assertNotNull(column.getActualPropertyName());
        assertEquals("_ab_____acus$____", column.getActualPropertyName());

        table = new Table();
        table.setSchema(schema);
        table.setTableName("ignore");
        column = new Column();
        column.setTable(table);
        column.setColumnName("0DPrpt");
        column.setColumnType("varchar");
        column.setLength(0);
        column.setScale(0);
        table.getColumns().add(column);
        schema.getTables().clear();
        schema.getTables().add(table);
        model.init(null);
        assertEquals("_0dprpt", column.getActualPropertyName());

        
        column.setColumnName("XYZéèçò°àùì");
        model.init(null);
        assertEquals("xyzéèçò_àùì", column.getActualPropertyName());

        column.setColumnName("ĖĔĕĘĘŜŞŝōŎľĿʛʋʊɪɩɨɷ");
        model.init(null);
        assertEquals("ĖĔĕĘĘŜŞŝōŎľĿʛʋʊɪɩɨɷ", column.getActualPropertyName());
    }

    public void testDuplicatePropertyNames() {
        Model model = new Model();
        Database db = new Database();
        db.setDatabaseName("portofino");
        db.setConnectionProvider(new JdbcConnectionProvider());
        Schema schema = new Schema();
        schema.setDatabase(db);
        db.getSchemas().add(schema);
        schema.setSchemaName("meta");

        Table table = new Table();
        table.setSchema(schema);
        table.setTableName("ignore");
        schema.getTables().add(table);

        Column column = new Column();
        column.setTable(table);
        column.setColumnName("dup");
        column.setColumnType("varchar");
        column.setLength(0);
        column.setScale(0);
        table.getColumns().add(column);

        Column column2 = new Column();
        column2.setTable(table);
        column2.setColumnName("dup");
        column2.setColumnType("number");
        column2.setLength(0);
        column2.setScale(0);
        table.getColumns().add(column2);

        model.getDatabases().add(db);
        model.init(new PropertiesConfiguration());

        assertFalse(StringUtils.equals(column.getActualPropertyName(), column2.getActualPropertyName()));

        Table table2 = new Table();
        table2.setSchema(schema);
        table2.setTableName("ignore2");
        schema.getTables().add(table2);

        Column column3 = new Column();
        column3.setTable(table2);
        column3.setColumnName("dup");
        column3.setColumnType("date");
        column3.setLength(0);
        column3.setScale(0);
        table2.getColumns().add(column3);

        ForeignKey fk = new ForeignKey(table);
        fk.setToSchema("meta");
        fk.setToTableName("ignore2");
        fk.setToDatabase("portofino");
        fk.setName("dup");
        Reference ref = new Reference(fk);
        ref.setFromColumn("dup");
        ref.setToColumn("dup");
        fk.getReferences().add(ref);
        table.getForeignKeys().add(fk);

        model.init(null);

        assertFalse(StringUtils.equals(column.getActualPropertyName(), column2.getActualPropertyName()));
        assertFalse(StringUtils.equals(column.getActualPropertyName(), fk.getActualOnePropertyName()));
        assertFalse(StringUtils.equals(column3.getActualPropertyName(), fk.getActualManyPropertyName()));

        fk.setName("dup_2");
        model.init(null);

        assertFalse(StringUtils.equals(column.getActualPropertyName(), column2.getActualPropertyName()));
        assertFalse(StringUtils.equals(column.getActualPropertyName(), fk.getActualOnePropertyName()));
        assertFalse(StringUtils.equals(column3.getActualPropertyName(), fk.getActualManyPropertyName()));
    }
}
