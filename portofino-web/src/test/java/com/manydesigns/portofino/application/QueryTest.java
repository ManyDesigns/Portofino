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

package com.manydesigns.portofino.application;

import com.manydesigns.elements.text.QueryStringWithParameters;
import com.manydesigns.portofino.database.TableCriteria;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.database.*;
import com.manydesigns.portofino.reflection.TableAccessor;
import junit.framework.TestCase;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class QueryTest extends TestCase {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public void testMergeQueryWithAlias() throws NoSuchFieldException {
        Model model = new Model();

        Database database = new Database();
        database.setDatabaseName("db");

        Schema schema = new Schema(database);
        schema.setSchemaName("schema");
        database.getSchemas().add(schema);

        Table table = new Table(schema);
        table.setTableName("test_table");
        schema.getTables().add(table);

        Column column = new Column(table);
        column.setColumnName("column1");
        column.setLength(10);
        column.setScale(0);
        table.getColumns().add(column);

        PrimaryKey primaryKey = new PrimaryKey(table);
        PrimaryKeyColumn pkColumn = new PrimaryKeyColumn(primaryKey);
        primaryKey.getPrimaryKeyColumns().add(pkColumn);
        pkColumn.setColumnName("column1");
        table.setPrimaryKey(primaryKey);

        model.init(database);

        TableAccessor tableAccessor = new TableAccessor(table);

        TableCriteria criteria = new TableCriteria(table);
        criteria.eq(tableAccessor.getProperty("column1"), "123");

        //W/o select
        QueryStringWithParameters queryStringWithParameters =
                QueryUtils.mergeQuery("from test_table t", criteria, null);
        assertEquals("FROM test_table AS t WHERE t.column1 = ?", queryStringWithParameters.getQueryString());

        queryStringWithParameters =
                QueryUtils.mergeQuery("from test_table t where t.foo = 1", criteria, null);
        assertEquals("FROM test_table AS t WHERE (t.foo = 1) AND t.column1 = ?", queryStringWithParameters.getQueryString());

        queryStringWithParameters =
                QueryUtils.mergeQuery("from test_table t, other where t.foo = other.bar", criteria, null);
        assertEquals("FROM test_table AS t, other WHERE (t.foo = other.bar) AND t.column1 = ?", queryStringWithParameters.getQueryString());

        queryStringWithParameters =
                QueryUtils.mergeQuery("from test_table t, other x where t.foo = x.bar", criteria, null);
        assertEquals("FROM test_table AS t, other AS x WHERE (t.foo = x.bar) AND t.column1 = ?", queryStringWithParameters.getQueryString());

        //W/select
        queryStringWithParameters =
                QueryUtils.mergeQuery("select t from test_table t", criteria, null);
        assertEquals("SELECT t FROM test_table AS t WHERE t.column1 = ?", queryStringWithParameters.getQueryString());

        queryStringWithParameters =
                QueryUtils.mergeQuery("select t from test_table t where t.foo = 1", criteria, null);
        assertEquals("SELECT t FROM test_table AS t WHERE (t.foo = 1) AND t.column1 = ?", queryStringWithParameters.getQueryString());

        queryStringWithParameters =
                QueryUtils.mergeQuery("select t from test_table t, other where t.foo = other.bar", criteria, null);
        assertEquals("SELECT t FROM test_table AS t, other WHERE (t.foo = other.bar) AND t.column1 = ?", queryStringWithParameters.getQueryString());

        queryStringWithParameters =
                QueryUtils.mergeQuery("select t from test_table t, other x where t.foo = x.bar", criteria, null);
        assertEquals("SELECT t FROM test_table AS t, other AS x WHERE (t.foo = x.bar) AND t.column1 = ?", queryStringWithParameters.getQueryString());

        //W/multiple select
        queryStringWithParameters =
                QueryUtils.mergeQuery("select t, u from test_table t", criteria, null);
        assertEquals("SELECT t, u FROM test_table AS t WHERE t.column1 = ?", queryStringWithParameters.getQueryString());

        queryStringWithParameters =
                QueryUtils.mergeQuery("select t, u from test_table t where t.foo = 1", criteria, null);
        assertEquals("SELECT t, u FROM test_table AS t WHERE (t.foo = 1) AND t.column1 = ?", queryStringWithParameters.getQueryString());

        queryStringWithParameters =
                QueryUtils.mergeQuery("select t, u from test_table t, other where t.foo = other.bar", criteria, null);
        assertEquals("SELECT t, u FROM test_table AS t, other WHERE (t.foo = other.bar) AND t.column1 = ?", queryStringWithParameters.getQueryString());

        queryStringWithParameters =
                QueryUtils.mergeQuery("select t, u from test_table t, other x where t.foo = x.bar", criteria, null);
        assertEquals("SELECT t, u FROM test_table AS t, other AS x WHERE (t.foo = x.bar) AND t.column1 = ?", queryStringWithParameters.getQueryString());
    }

}
