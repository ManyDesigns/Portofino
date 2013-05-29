/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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
            "Copyright (c) 2005-2013, ManyDesigns srl";

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
        assertEquals(0, query.getParameters().length);
        assertEquals("FROM test", query.getQueryString());

        query = QueryUtils.mergeQuery("select foo, bar from test", criteria, null);
        assertEquals(0, query.getParameters().length);
        assertEquals("SELECT foo, bar FROM test", query.getQueryString());

        query = QueryUtils.mergeQuery("from test where a = b", criteria, null);
        assertEquals(0, query.getParameters().length);
        assertEquals("FROM test WHERE a = b", query.getQueryString());

        query = QueryUtils.mergeQuery("select foo, bar from test where a = b", criteria, null);
        assertEquals(0, query.getParameters().length);
        assertEquals("SELECT foo, bar FROM test WHERE a = b", query.getQueryString());

        PropertyAccessor propertyAccessor = new ColumnAccessor(new FakeColumn(), false, false, null);
        criteria.eq(propertyAccessor, 42);

        query = QueryUtils.mergeQuery("from test", criteria, null);
        assertEquals(1, query.getParameters().length);
        assertEquals(42, query.getParameters()[0]);
        assertEquals("FROM test WHERE test = ?", query.getQueryString());

        query = QueryUtils.mergeQuery("select foo, bar from test", criteria, null);
        assertEquals(1, query.getParameters().length);
        assertEquals(42, query.getParameters()[0]);
        assertEquals("SELECT foo, bar FROM test WHERE test = ?", query.getQueryString());

        query = QueryUtils.mergeQuery("from test where a = b", criteria, null);
        assertEquals(1, query.getParameters().length);
        assertEquals(42, query.getParameters()[0]);
        assertEquals("FROM test WHERE a = b AND test = ?", query.getQueryString());

        query = QueryUtils.mergeQuery("select foo, bar from test where a = b", criteria, null);
        assertEquals(1, query.getParameters().length);
        assertEquals(42, query.getParameters()[0]);
        assertEquals("SELECT foo, bar FROM test WHERE a = b AND test = ?", query.getQueryString());

        criteria.orderBy(propertyAccessor, Criteria.OrderBy.ASC);

        query = QueryUtils.mergeQuery("from test", criteria, null);
        assertEquals(1, query.getParameters().length);
        assertEquals(42, query.getParameters()[0]);
        assertEquals("FROM test WHERE test = ? ORDER BY test", query.getQueryString());

        query = QueryUtils.mergeQuery("select foo, bar from test", criteria, null);
        assertEquals(1, query.getParameters().length);
        assertEquals(42, query.getParameters()[0]);
        assertEquals("SELECT foo, bar FROM test WHERE test = ? ORDER BY test", query.getQueryString());

        query = QueryUtils.mergeQuery("from test where a = b", criteria, null);
        assertEquals(1, query.getParameters().length);
        assertEquals(42, query.getParameters()[0]);
        assertEquals("FROM test WHERE a = b AND test = ? ORDER BY test", query.getQueryString());

        query = QueryUtils.mergeQuery("select foo, bar from test where a = b", criteria, null);
        assertEquals(1, query.getParameters().length);
        assertEquals(42, query.getParameters()[0]);
        assertEquals("SELECT foo, bar FROM test WHERE a = b AND test = ? ORDER BY test", query.getQueryString());

        criteria.orderBy(propertyAccessor, Criteria.OrderBy.DESC);

        query = QueryUtils.mergeQuery("from test", criteria, null);
        assertEquals(1, query.getParameters().length);
        assertEquals(42, query.getParameters()[0]);
        assertEquals("FROM test WHERE test = ? ORDER BY test DESC", query.getQueryString());

        query = QueryUtils.mergeQuery("select foo, bar from test", criteria, null);
        assertEquals(1, query.getParameters().length);
        assertEquals(42, query.getParameters()[0]);
        assertEquals("SELECT foo, bar FROM test WHERE test = ? ORDER BY test DESC", query.getQueryString());

        query = QueryUtils.mergeQuery("from test where a = b", criteria, null);
        assertEquals(1, query.getParameters().length);
        assertEquals(42, query.getParameters()[0]);
        assertEquals("FROM test WHERE a = b AND test = ? ORDER BY test DESC", query.getQueryString());

        query = QueryUtils.mergeQuery("select foo, bar from test where a = b", criteria, null);
        assertEquals(1, query.getParameters().length);
        assertEquals(42, query.getParameters()[0]);
        assertEquals("SELECT foo, bar FROM test WHERE a = b AND test = ? ORDER BY test DESC", query.getQueryString());
    }

}
