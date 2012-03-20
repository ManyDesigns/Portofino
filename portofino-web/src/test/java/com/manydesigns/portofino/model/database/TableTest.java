/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.manydesigns.portofino.model.database;

import com.manydesigns.portofino.model.Model;
import junit.framework.TestCase;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class TableTest extends TestCase {

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
        table.init(model);

        assertNotNull(table.getActualEntityName());
        assertEquals("_ab_____acus$____", table.getActualEntityName());
        System.out.println(table.getActualEntityName());

        table = new Table();
        table.setSchema(schema);
        table.setTableName("0DPrpt");
        table.init(model);
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
        table.init(model);
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
        table.init(model);
        assertEquals("_0dprpt", table.getActualEntityName());
        System.out.println(table.getActualEntityName());

        db = new Database();
        db.setDatabaseName(".portofino");
        table.setTableName("0DPrpt");
        table.init(model);
        assertEquals("_0dprpt", table.getActualEntityName());
        System.out.println(table.getActualEntityName());

        table.setTableName("XYZéèçò°àùì");
        table.init(model);
        assertEquals("xyzéèçò_àùì", table.getActualEntityName());
        System.out.println(table.getActualEntityName());

        table.setTableName("ĖĔĕĘĘŜŞŝōŎľĿʛʋʊɪɩɨɷ");
        table.init(model);
        assertEquals("ĖĔĕĘĘŜŞŝōŎľĿʛʋʊɪɩɨɷ", table.getActualEntityName());
        System.out.println(table.getActualEntityName());
    }

    public void testActualColumnNames() {
        Model model = new Model();
        Database db = new Database();
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
        table.getColumns().add(column);
        model.getDatabases().add(db);
        model.init();

        assertNotNull(column.getActualPropertyName());
        assertEquals("_ab_____acus$____", column.getActualPropertyName());

        table = new Table();
        table.setSchema(schema);
        table.setTableName("ignore");
        column = new Column();
        column.setTable(table);
        column.setColumnName("0DPrpt");
        table.getColumns().add(column);
        schema.getTables().clear();
        schema.getTables().add(table);
        model.init();
        assertEquals("_0dprpt", column.getActualPropertyName());

        
        column.setColumnName("XYZéèçò°àùì");
        model.init();
        assertEquals("xyzéèçò_àùì", column.getActualPropertyName());

        column.setColumnName("ĖĔĕĘĘŜŞŝōŎľĿʛʋʊɪɩɨɷ");
        model.init();
        assertEquals("ĖĔĕĘĘŜŞŝōŎľĿʛʋʊɪɩɨɷ", column.getActualPropertyName());
    }
}
