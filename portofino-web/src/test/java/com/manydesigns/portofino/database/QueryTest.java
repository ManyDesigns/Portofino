/*
* Copyright (C) 2005-2011 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.database;

import com.manydesigns.elements.fields.search.Criteria;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.text.QueryStringWithParameters;
import com.manydesigns.portofino.application.QueryUtils;
import com.manydesigns.portofino.model.database.Column;
import com.manydesigns.portofino.model.database.Table;
import com.manydesigns.portofino.reflection.ColumnAccessor;
import junit.framework.TestCase;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class QueryTest extends TestCase {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    public static class FakeTable extends Table {
        @Override
        public String getActualEntityName() {
            return "test";
        }
    }

    public static class FakeColumn extends Column {

        @Override
        public String getActualPropertyName() {
            return "test";
        }

        @Override
        public Integer getLength() {
            return 42;
        }

        @Override
        public Integer getScale() {
            return 24;
        }
    }

    public void testMergeQuery() {
        TableCriteria criteria = new TableCriteria(new FakeTable());

        QueryStringWithParameters query = QueryUtils.mergeQuery("from test", criteria, null);
        assertEquals(0, query.getParamaters().length);
        assertEquals("FROM test", query.getQueryString());

        query = QueryUtils.mergeQuery("select foo, bar from test", criteria, null);
        assertEquals(0, query.getParamaters().length);
        assertEquals("SELECT foo, bar FROM test", query.getQueryString());

        query = QueryUtils.mergeQuery("from test where a = b", criteria, null);
        assertEquals(0, query.getParamaters().length);
        assertEquals("FROM test WHERE a = b", query.getQueryString());

        query = QueryUtils.mergeQuery("select foo, bar from test where a = b", criteria, null);
        assertEquals(0, query.getParamaters().length);
        assertEquals("SELECT foo, bar FROM test WHERE a = b", query.getQueryString());

        PropertyAccessor propertyAccessor = new ColumnAccessor(new FakeColumn(), false, false, null);
        criteria.eq(propertyAccessor, 42);

        query = QueryUtils.mergeQuery("from test", criteria, null);
        assertEquals(1, query.getParamaters().length);
        assertEquals(42, query.getParamaters()[0]);
        assertEquals("FROM test WHERE test = ?", query.getQueryString());

        query = QueryUtils.mergeQuery("select foo, bar from test", criteria, null);
        assertEquals(1, query.getParamaters().length);
        assertEquals(42, query.getParamaters()[0]);
        assertEquals("SELECT foo, bar FROM test WHERE test = ?", query.getQueryString());

        query = QueryUtils.mergeQuery("from test where a = b", criteria, null);
        assertEquals(1, query.getParamaters().length);
        assertEquals(42, query.getParamaters()[0]);
        assertEquals("FROM test WHERE a = b AND test = ?", query.getQueryString());

        query = QueryUtils.mergeQuery("select foo, bar from test where a = b", criteria, null);
        assertEquals(1, query.getParamaters().length);
        assertEquals(42, query.getParamaters()[0]);
        assertEquals("SELECT foo, bar FROM test WHERE a = b AND test = ?", query.getQueryString());

        criteria.orderBy(propertyAccessor, Criteria.OrderBy.ASC);

        query = QueryUtils.mergeQuery("from test", criteria, null);
        assertEquals(1, query.getParamaters().length);
        assertEquals(42, query.getParamaters()[0]);
        assertEquals("FROM test WHERE test = ? ORDER BY test", query.getQueryString());

        query = QueryUtils.mergeQuery("select foo, bar from test", criteria, null);
        assertEquals(1, query.getParamaters().length);
        assertEquals(42, query.getParamaters()[0]);
        assertEquals("SELECT foo, bar FROM test WHERE test = ? ORDER BY test", query.getQueryString());

        query = QueryUtils.mergeQuery("from test where a = b", criteria, null);
        assertEquals(1, query.getParamaters().length);
        assertEquals(42, query.getParamaters()[0]);
        assertEquals("FROM test WHERE a = b AND test = ? ORDER BY test", query.getQueryString());

        query = QueryUtils.mergeQuery("select foo, bar from test where a = b", criteria, null);
        assertEquals(1, query.getParamaters().length);
        assertEquals(42, query.getParamaters()[0]);
        assertEquals("SELECT foo, bar FROM test WHERE a = b AND test = ? ORDER BY test", query.getQueryString());

        criteria.orderBy(propertyAccessor, Criteria.OrderBy.DESC);

        query = QueryUtils.mergeQuery("from test", criteria, null);
        assertEquals(1, query.getParamaters().length);
        assertEquals(42, query.getParamaters()[0]);
        assertEquals("FROM test WHERE test = ? ORDER BY test DESC", query.getQueryString());

        query = QueryUtils.mergeQuery("select foo, bar from test", criteria, null);
        assertEquals(1, query.getParamaters().length);
        assertEquals(42, query.getParamaters()[0]);
        assertEquals("SELECT foo, bar FROM test WHERE test = ? ORDER BY test DESC", query.getQueryString());

        query = QueryUtils.mergeQuery("from test where a = b", criteria, null);
        assertEquals(1, query.getParamaters().length);
        assertEquals(42, query.getParamaters()[0]);
        assertEquals("FROM test WHERE a = b AND test = ? ORDER BY test DESC", query.getQueryString());

        query = QueryUtils.mergeQuery("select foo, bar from test where a = b", criteria, null);
        assertEquals(1, query.getParamaters().length);
        assertEquals(42, query.getParamaters()[0]);
        assertEquals("SELECT foo, bar FROM test WHERE a = b AND test = ? ORDER BY test DESC", query.getQueryString());
    }

}
