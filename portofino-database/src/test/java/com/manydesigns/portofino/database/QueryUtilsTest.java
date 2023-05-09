package com.manydesigns.portofino.database;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.fields.search.Criteria;
import com.manydesigns.elements.text.QueryStringWithParameters;
import com.manydesigns.portofino.database.model.platforms.GenericDatabasePlatform;
import com.manydesigns.portofino.model.InitVisitor;
import com.manydesigns.portofino.model.LinkVisitor;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.database.model.*;
import com.manydesigns.portofino.model.ResetVisitor;
import com.manydesigns.portofino.persistence.QueryUtils;
import com.manydesigns.portofino.persistence.hibernate.Events;
import com.manydesigns.portofino.persistence.hibernate.SessionFactoryAndCodeBase;
import com.manydesigns.portofino.persistence.hibernate.SessionFactoryBuilder;
import com.manydesigns.portofino.reflection.TableAccessor;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.hibernate.Session;
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
        Database database = new Database();
        JdbcConnectionProvider connectionProvider = new JdbcConnectionProvider() {{
            databasePlatform = new GenericDatabasePlatform();
            actualUrl = url = "jdbc:h2:mem:qutest";
        }};
        database.setConnectionProvider(connectionProvider);
        database.setDatabaseName("db");
        List<Database> databases = new ArrayList<>();
        databases.add(database);

        Schema schema = new Schema(database);
        schema.setSchemaName("schema");
        database.addSchema(schema);

        Table table = new Table(schema);
        table.setTableName("test_table");
        schema.getTables().add(table);

        Column column = new Column(table);
        column.setColumnName("column1");
        column.setColumnType("varchar");
        column.setJavaType("java.lang.String");
        column.setLength(10);
        column.setScale(0);
        table.getColumns().add(column);

        column = new Column(table);
        column.setColumnName("foo");
        column.setColumnType("varchar");
        column.setJavaType("java.lang.String");
        column.setLength(10);
        column.setScale(0);
        table.getColumns().add(column);

        PrimaryKey primaryKey = new PrimaryKey(table);
        PrimaryKeyColumn pkColumn = new PrimaryKeyColumn(primaryKey);
        primaryKey.getPrimaryKeyColumns().add(pkColumn);
        pkColumn.setColumnName("column1");
        table.setPrimaryKey(primaryKey);

        Table other = new Table(schema);
        other.setTableName("other");
        schema.getTables().add(other);

        column = new Column(other);
        column.setColumnName("id");
        column.setColumnType("varchar");
        column.setJavaType("java.lang.String");
        column.setLength(10);
        column.setScale(0);
        other.getColumns().add(column);

        column = new Column(other);
        column.setColumnName("bar");
        column.setColumnType("varchar");
        column.setJavaType("java.lang.String");
        column.setLength(10);
        column.setScale(0);
        other.getColumns().add(column);

        primaryKey = new PrimaryKey(other);
        pkColumn = new PrimaryKeyColumn(primaryKey);
        primaryKey.getPrimaryKeyColumns().add(pkColumn);
        pkColumn.setColumnName("id");
        other.setPrimaryKey(primaryKey);

        initDatabase(database, databases);

        SessionFactoryBuilder builder =
                new SessionFactoryBuilder(database, new PropertiesConfiguration(), new Events(), null);
        SessionFactoryAndCodeBase sessionFactoryAndCodeBase = builder.buildSessionFactory();
        Session session = sessionFactoryAndCodeBase.sessionFactory.openSession();

        TableAccessor tableAccessor = new TableAccessor(table);

        Criteria criteria = new Criteria();
        criteria.eq(tableAccessor.getProperty("column1"), "123");

        //W/o select
        QueryStringWithParameters queryStringWithParameters =
                QueryUtils.mergeQuery(session, "from test_table t", table, criteria, null, null);
        assertEquals(
                "select t from db.schema.TestTable t where t.column1 = :p1",
                queryStringWithParameters.getQueryString());

        queryStringWithParameters =
                QueryUtils.mergeQuery(session, "from test_table t where t.foo = '1'",
                        table, criteria, null, null);
        assertEquals(
                "select t from db.schema.TestTable t where t.foo = '1' and t.column1 = :p1",
                queryStringWithParameters.getQueryString());

        queryStringWithParameters =
                QueryUtils.mergeQuery(session, "from test_table t, other o where t.foo = o.bar",
                        table, criteria, null, null);
        assertEquals("select t, o from db.schema.TestTable t, db.schema.Other o where t.foo = o.bar and t.column1 = :p1",
                queryStringWithParameters.getQueryString());

        queryStringWithParameters =
                QueryUtils.mergeQuery(session, "from test_table t, other x where t.foo = x.bar",
                        table, criteria, null, null);
        assertEquals(
                "select t, x from db.schema.TestTable t, db.schema.Other x where t.foo = x.bar and t.column1 = :p1",
                queryStringWithParameters.getQueryString());

        //W/select
        queryStringWithParameters =
                QueryUtils.mergeQuery(session, "select t from test_table t",
                        table, criteria, null, null);
        assertEquals(
                "select t from db.schema.TestTable t where t.column1 = :p1",
                queryStringWithParameters.getQueryString());

        queryStringWithParameters =
                QueryUtils.mergeQuery(session, "select t from test_table t where t.foo = '1'",
                        table, criteria, null, null);
        assertEquals("select t from db.schema.TestTable t where t.foo = '1' and t.column1 = :p1",
                queryStringWithParameters.getQueryString());

        // TODO auto aliasing?
        queryStringWithParameters =
                QueryUtils.mergeQuery(session, "select t from test_table t, other o where t.foo = o.bar",
                        table, criteria, null, null);
        assertEquals("select t from db.schema.TestTable t, db.schema.Other o where t.foo = o.bar and t.column1 = :p1",
                queryStringWithParameters.getQueryString());

        queryStringWithParameters =
                QueryUtils.mergeQuery(session, "select t from test_table t, other x where t.foo = x.bar",
                        table, criteria, null, null);
        assertEquals(
                "select t from db.schema.TestTable t, db.schema.Other x where t.foo = x.bar and t.column1 = :p1",
                queryStringWithParameters.getQueryString());

        //W/multiple select
        queryStringWithParameters =
                QueryUtils.mergeQuery(session, "select t, u from test_table t, other u",
                        table, criteria, null, null);
        assertEquals("select t, u from db.schema.TestTable t, db.schema.Other u where t.column1 = :p1",
                queryStringWithParameters.getQueryString());

        queryStringWithParameters =
                QueryUtils.mergeQuery(session, "select t, u from test_table t, other u where t.foo = '1'",
                        table, criteria, null, null);
        assertEquals("select t, u from db.schema.TestTable t, db.schema.Other u where t.foo = '1' and t.column1 = :p1",
                queryStringWithParameters.getQueryString());

        queryStringWithParameters =
                QueryUtils.mergeQuery(session, "select t, u from test_table t, other u where t.foo = u.bar",
                        table, criteria, null, null);
        assertEquals("select t, u from db.schema.TestTable t, db.schema.Other u where t.foo = u.bar and t.column1 = :p1",
                queryStringWithParameters.getQueryString());

        queryStringWithParameters =
                QueryUtils.mergeQuery(session, "select t, x from test_table t, other x where t.foo = x.bar",
                        table, criteria, null, null);
        assertEquals("select t, x from db.schema.TestTable t, db.schema.Other x where t.foo = x.bar and t.column1 = :p1",
                queryStringWithParameters.getQueryString());
    }

    private void initDatabase(Database database, List<Database> databases) {
        new ResetVisitor().visit(database);
        new InitVisitor(databases, new PropertiesConfiguration()).visit(database);
        new LinkVisitor(databases, new PropertiesConfiguration()).visit(database);
    }

}
