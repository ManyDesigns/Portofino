package com.manydesigns.portofino.database;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.text.QueryStringWithParameters;
import com.manydesigns.portofino.model.InitVisitor;
import com.manydesigns.portofino.model.LinkVisitor;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.database.model.*;
import com.manydesigns.portofino.model.ResetVisitor;
import com.manydesigns.portofino.persistence.QueryUtils;
import com.manydesigns.portofino.persistence.TableCriteria;
import com.manydesigns.portofino.reflection.TableAccessor;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;

@Test
public class QueryUtilsTest {

    @BeforeClass
    public void setupElements() {
        ElementsThreadLocals.setupDefaultElementsContext();
    }

    public void testMergeQueryWithAlias() throws NoSuchFieldException {
        Model model = new Model();

        Database database = new Database();
        database.setDatabaseName("db");
        List<Database> databases = new ArrayList<>();
        databases.add(database);

        Schema schema = new Schema(database);
        schema.setSchemaName("schema");
        database.getSchemas().add(schema);

        Table table = new Table(schema);
        table.setTableName("test_table");
        schema.getTables().add(table);

        Column column = new Column(table);
        column.setColumnName("column1");
        column.setColumnType("varchar");
        column.setLength(10);
        column.setScale(0);
        table.getColumns().add(column);

        PrimaryKey primaryKey = new PrimaryKey(table);
        PrimaryKeyColumn pkColumn = new PrimaryKeyColumn(primaryKey);
        primaryKey.getPrimaryKeyColumns().add(pkColumn);
        pkColumn.setColumnName("column1");
        table.setPrimaryKey(primaryKey);

        initDatabase(database, databases);

        TableAccessor tableAccessor = new TableAccessor(table);

        TableCriteria criteria = new TableCriteria(table);
        criteria.eq(tableAccessor.getProperty("column1"), "123");

        //W/o select
        QueryStringWithParameters queryStringWithParameters =
                QueryUtils.mergeQuery("from test_table t", criteria, null);
        assertEquals("FROM test_table t WHERE t.column1 = :p1", queryStringWithParameters.getQueryString());

        queryStringWithParameters =
                QueryUtils.mergeQuery("from test_table t where t.foo = 1", criteria, null);
        assertEquals("FROM test_table t WHERE (t.foo = 1) AND t.column1 = :p1", queryStringWithParameters.getQueryString());

        queryStringWithParameters =
                QueryUtils.mergeQuery("from test_table t, other where t.foo = other.bar", criteria, null);
        assertEquals("FROM test_table t, other WHERE (t.foo = other.bar) AND t.column1 = :p1", queryStringWithParameters.getQueryString());

        queryStringWithParameters =
                QueryUtils.mergeQuery("from test_table t, other x where t.foo = x.bar", criteria, null);
        assertEquals("FROM test_table t, other x WHERE (t.foo = x.bar) AND t.column1 = :p1", queryStringWithParameters.getQueryString());

        //W/select
        queryStringWithParameters =
                QueryUtils.mergeQuery("select t from test_table t", criteria, null);
        assertEquals("SELECT t FROM test_table t WHERE t.column1 = :p1", queryStringWithParameters.getQueryString());

        queryStringWithParameters =
                QueryUtils.mergeQuery("select t from test_table t where t.foo = 1", criteria, null);
        assertEquals("SELECT t FROM test_table t WHERE (t.foo = 1) AND t.column1 = :p1", queryStringWithParameters.getQueryString());

        queryStringWithParameters =
                QueryUtils.mergeQuery("select t from test_table t, other where t.foo = other.bar", criteria, null);
        assertEquals("SELECT t FROM test_table t, other WHERE (t.foo = other.bar) AND t.column1 = :p1", queryStringWithParameters.getQueryString());

        queryStringWithParameters =
                QueryUtils.mergeQuery("select t from test_table t, other x where t.foo = x.bar", criteria, null);
        assertEquals("SELECT t FROM test_table t, other x WHERE (t.foo = x.bar) AND t.column1 = :p1", queryStringWithParameters.getQueryString());

        //W/multiple select
        queryStringWithParameters =
                QueryUtils.mergeQuery("select t, u from test_table t", criteria, null);
        assertEquals("SELECT t, u FROM test_table t WHERE t.column1 = :p1", queryStringWithParameters.getQueryString());

        queryStringWithParameters =
                QueryUtils.mergeQuery("select t, u from test_table t where t.foo = 1", criteria, null);
        assertEquals("SELECT t, u FROM test_table t WHERE (t.foo = 1) AND t.column1 = :p1", queryStringWithParameters.getQueryString());

        queryStringWithParameters =
                QueryUtils.mergeQuery("select t, u from test_table t, other where t.foo = other.bar", criteria, null);
        assertEquals("SELECT t, u FROM test_table t, other WHERE (t.foo = other.bar) AND t.column1 = :p1", queryStringWithParameters.getQueryString());

        queryStringWithParameters =
                QueryUtils.mergeQuery("select t, u from test_table t, other x where t.foo = x.bar", criteria, null);
        assertEquals("SELECT t, u FROM test_table t, other x WHERE (t.foo = x.bar) AND t.column1 = :p1", queryStringWithParameters.getQueryString());
    }

    private void initDatabase(Database database, List<Database> databases) {
        new ResetVisitor().visit(database);
        new InitVisitor(databases, new PropertiesConfiguration()).visit(database);
        new LinkVisitor(databases, new PropertiesConfiguration()).visit(database);
    }

}
