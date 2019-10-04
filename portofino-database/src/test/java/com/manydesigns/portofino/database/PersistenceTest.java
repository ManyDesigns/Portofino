package com.manydesigns.portofino.database;

import com.manydesigns.elements.ElementsProperties;
import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.fields.DateField;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.servlet.MutableHttpServletRequest;
import com.manydesigns.portofino.database.platforms.H2DatabasePlatform;
import com.manydesigns.portofino.model.Annotation;
import com.manydesigns.portofino.model.Property;
import com.manydesigns.portofino.model.database.*;
import com.manydesigns.portofino.model.database.platforms.DatabasePlatformsRegistry;
import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.persistence.QueryUtils;
import com.manydesigns.portofino.persistence.TableCriteria;
import com.manydesigns.portofino.reflection.TableAccessor;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.vfs2.VFS;
import org.h2.tools.RunScript;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.UnknownEntityTypeException;
import org.hibernate.jdbc.Work;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.persistence.metamodel.EntityType;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static org.testng.Assert.*;
import static org.testng.AssertJUnit.assertEquals;

@Test
public class PersistenceTest {

    Persistence persistence;

    @BeforeClass
    public void setupElements() {
        ElementsThreadLocals.setupDefaultElementsContext();
    }

    @BeforeMethod
    public void setup() throws Exception {
        Configuration configuration = new PropertiesConfiguration();
        DatabasePlatformsRegistry databasePlatformsRegistry = new DatabasePlatformsRegistry(configuration);
        databasePlatformsRegistry.addDatabasePlatform(new H2DatabasePlatform());
        persistence = new Persistence(
                VFS.getManager().resolveFile("res:com/manydesigns/portofino/database/model"), configuration, null, databasePlatformsRegistry);
        persistence.start();
        setupJPetStore();
        setupHibernateTest();
        persistence.initModel();
    }

    @AfterMethod
    public void teardown() {
        persistence.stop();
    }

    protected void setupJPetStore() throws Exception {
        Session session = persistence.getSession("jpetstore");
        session.doWork(new Work() {
            @Override
            public void execute(Connection connection) throws SQLException {
                InputStreamReader reader =
                        new InputStreamReader(
                                getClass().getResourceAsStream("sql/jpetstore-postgres-schema.sql"));
                RunScript.execute(connection, reader);
                reader =
                        new InputStreamReader(
                                getClass().getResourceAsStream("sql/jpetstore-postgres-dataload.sql"));
                RunScript.execute(connection, reader);
            }
        });
        session.getTransaction().commit();
        persistence.syncDataModel("jpetstore");
        //Table ordersTable = DatabaseLogic.findTableByName(persistence.getModel(), "jpetstore", "PUBLIC", "ORDERS");
        //ordersTable.getPrimaryKey().getPrimaryKeyColumns().get(0).setGenerator(new TableGenerator());
        Table supplierTable = DatabaseLogic.findTableByName(persistence.getModel(), "jpetstore", "PUBLIC", "SUPPLIER");
        supplierTable.getPrimaryKey().getPrimaryKeyColumns().get(0).setGenerator(new IncrementGenerator());
        //Table testTable = DatabaseLogic.findTableByName(persistence.getModel(), "jpetstore", "PUBLIC", "TEST");
        //testTable.getPrimaryKey().getPrimaryKeyColumns().get(0).setGenerator(new SequenceGenerator());
    }

    protected void setupHibernateTest() throws Exception {
        Session session = persistence.getSession("hibernatetest");
        session.doWork(new Work() {
            @Override
            public void execute(Connection connection) throws SQLException {
                InputStreamReader reader =
                        new InputStreamReader(
                                getClass().getResourceAsStream("sql/hibernatetest.sql"));
                RunScript.execute(connection, reader);
            }
        });
        session.getTransaction().commit();
        persistence.syncDataModel("hibernatetest");
    }

    public void testReadProdotti() {
        Session session = persistence.getSession("jpetstore");
        Criteria criteria = session.createCriteria("product");
        List resultProd = new ArrayList(criteria.list());

        int sizePrd = resultProd.size();
        assertEquals("prodotti", 16, sizePrd);
    }

    public void testSearchAndReadCategorieProdotti() {
        Session session = persistence.getSession("jpetstore");
        Criteria criteria = session.createCriteria("category");
        List resultCat = new ArrayList(criteria.list());


        int sizeCat = resultCat.size();
        assertEquals("categorie", 5, sizeCat);


        Map categoria0 = (Map<String, Object>) resultCat.get(0);
        assertEquals("jpetstore.PUBLIC.category", categoria0.get("$type$"));
        assertNotNull(categoria0.get("name"));
        Map categoria1 = (Map<String, Object>)resultCat.get(1);
        assertNotNull(categoria0.get("name"));
        Map categoria2 = (Map<String, Object>)resultCat.get(2);
        assertNotNull(categoria0.get("name"));
        Map categoria3 = (Map<String, Object>)resultCat.get(3);
        assertNotNull(categoria0.get("name"));
        Map categoria4 = (Map<String, Object>)resultCat.get(4);
        assertNotNull(categoria0.get("name"));

        criteria = session.createCriteria("product");
        List resultProd = new ArrayList(criteria.list());

        Table table = DatabaseLogic.findTableByName(
                persistence.getModel(), "jpetstore", "PUBLIC", "CATEGORY");
        TableAccessor tableAccessor = new TableAccessor(table);
        TableCriteria tableCriteria = new TableCriteria(table);
        HashMap<String, String> category= findCategory(tableAccessor, tableCriteria);

        int sizePrd = resultProd.size();
        assertEquals("prodotti", 16, sizePrd);
        Map prd0 = (Map<String, Object>)resultProd.get(0);
        assertEquals("FI-SW-01", prd0.get("productid") );
        assertEquals("Angelfish", prd0.get("name"));
    }

    private HashMap<String, String> findCategory(TableAccessor tableAccessor, TableCriteria criteria) {
        HashMap<String, String> category=null;
        try {
            criteria.eq(tableAccessor.getProperty("catid"), "FISH");
            Session session = persistence.getSession(tableAccessor.getTable().getDatabaseName());
            List<Object> listObjs = QueryUtils.getObjects(session, criteria, null, null);
            assertEquals(1, listObjs.size());
            category = (HashMap<String, String>) listObjs.get(0);
            String catid = category.get("catid");
            assertEquals("FISH", catid);
        } catch (NoSuchFieldException e) {
            fail(e.getMessage(), e);
        }
        return category;
    }

    public void testSearchAndUpdateCategorie() {
        Table table = DatabaseLogic.findTableByName(
                persistence.getModel(), "jpetstore", "PUBLIC", "CATEGORY");
        TableAccessor tableAccessor = new TableAccessor(table);
        TableCriteria tableCriteria = new TableCriteria(table);

        Session session = persistence.getSession("jpetstore");
        Criteria criteria = session.createCriteria("category");
        List resultCat = new ArrayList(criteria.list());

        int sizeCat = resultCat.size();
        assertEquals("categorie", 5, sizeCat);
        Map<String, String> categoria0 =  findCategory(tableAccessor, tableCriteria);
        assertEquals("jpetstore.PUBLIC.category", categoria0.get("$type$"));
        assertEquals("Fish", categoria0.get("name"));
        categoria0.put("name", "Pesciu");
        session.update("category", categoria0);
        session.getTransaction().commit();
        persistence.closeSessions();

        //Controllo l'aggiornamento e riporto le cose come stavano
        tableCriteria = new TableCriteria(table);
        categoria0 =  findCategory(tableAccessor, tableCriteria);
        assertEquals("jpetstore.PUBLIC.category", categoria0.get("$type$"));
        assertEquals("Pesciu", categoria0.get("name"));
        categoria0.put("name", "Fish");
        session = persistence.getSession("jpetstore");
        session.update("category", categoria0);
        session.getTransaction().commit();
        persistence.closeSessions();
    }

    public void testSaveCategoria() {
        Map<String, Object> worms = new HashMap<String, Object>();
        worms.put("$type$", "category");
        worms.put("catid", "VERMI");
        worms.put("name", "worms");
        worms.put("descn",
                "<image src=\"../images/worms_icon.gif\"><font size=\"5\" color=\"blue\">" +
                        "Worms</font>");

        Session session = persistence.getSession("jpetstore");
        session.save("category", worms);
        session.getTransaction().commit();
    }

    public void testSaveLineItem() {
        Map<String, Object> lineItem = new HashMap<String, Object>();
        lineItem.put("$type$", "lineitem");
        lineItem.put("orderid", new BigInteger("2"));
        lineItem.put("linenum", new BigInteger("2"));
        lineItem.put("itemid", "test");
        lineItem.put("quantity", new BigInteger("20"));
        lineItem.put("unitprice", new BigDecimal(10.80));

        Session session = persistence.getSession("jpetstore");
        session.save("lineitem", lineItem);
        session.getTransaction().commit();

        persistence.closeSessions();

        session = persistence.getSession("jpetstore");
        session.delete("lineitem", lineItem);
        session.getTransaction().commit();
        persistence.closeSessions();
    }

    public void testSaveTestElement() throws Exception {
        Map<String, Object> testItem = new HashMap<String, Object>();
        testItem.put("$type$", "table1");
        testItem.put("testo", "esempio");
        //salvo
        Session session = persistence.getSession("hibernatetest");
        session.save("table1", testItem);
        session.getTransaction().commit();
    }

    public void testDeleteCategoria() {
        Map<String, Object> worms = new HashMap<String, Object>();
        worms.put("$type$", "category");
        worms.put("catid", "VERMI");
        worms.put("name", "worms");
        worms.put("descn",
                "<image src=\"../images/worms_icon.gif\"><font size=\"5\" color=\"blue\">" +
                        "Worms</font>");

        Session session = persistence.getSession("jpetstore");
        session.save("category", worms);
        session.getTransaction().commit();
        session.beginTransaction();
        session.delete("category", worms);
        session.getTransaction().commit();
    }

    public void testGetObjByPk(){
        //Test Chiave singola
        HashMap<String, Object> pk = new HashMap<String, Object>();
        pk.put("catid", "BIRDS");
        Object bird =  QueryUtils.getObjectByPk
                (persistence, "jpetstore", "category", pk);
        assertEquals("Birds", ((Map) bird).get("name"));

        //Test Chiave composta
        pk = new HashMap<String, Object>();
        pk.put("orderid", new BigInteger("1"));
        pk.put("linenum", new BigInteger("1"));
        Map lineItem = (Map) QueryUtils.getObjectByPk
                (persistence, "jpetstore", "lineitem", pk);
        assertEquals("EST-1", lineItem.get("itemid"));
    }

    public void testForeignKeyNavigation() {
        HashMap<String, Object> pk = new HashMap<String, Object>();
        pk.put("catid", "BIRDS");
        Map bird = (Map) QueryUtils.getObjectByPk
                (persistence, "jpetstore", "category", pk);
        assertEquals("Birds", bird.get("name"));
        assertTrue(bird.get("fk_product_1") instanceof Collection);
        assertFalse(((Collection) bird.get("fk_product_1")).isEmpty());
        for(Object o : ((Collection) bird.get("fk_product_1"))) {
            assertNotNull(o);
            assertEquals(bird, ((Map) o).get("fk_product_1"));
        }

        pk = new HashMap<>();
        pk.put("regione", "liguria");
        pk.put("provincia", "genova");
        pk.put("comune", "rapallo");
        Map comune = (Map) QueryUtils.getObjectByPk
                (persistence, "hibernatetest", "comune", pk);

        /* not supported.
        assertTrue(comune.get("domanda_comune_fkey") instanceof Collection);
        assertFalse(((Collection) comune.get("domanda_comune_fkey")).isEmpty());
        for(Object o : ((Collection) comune.get("domanda_comune_fkey"))) {
            assertNotNull(o);
            assertEquals(pk.get("comune"), ((Map) ((Map) o).get("domanda_comune_fkey")).get("comune"));
        }*/

        assertTrue(comune.get("domanda_regione_fkey") instanceof Collection);
        assertFalse(((Collection) comune.get("domanda_regione_fkey")).isEmpty());
        for(Object o : ((Collection) comune.get("domanda_regione_fkey"))) {
            assertNotNull(o);
            //assertEquals(pk.get("comune"), ((Map) ((Map) o).get("domanda_comune_fkey")).get("comune"));
        }
    }

    public void testGetRelatedObjects(){
        HashMap<String, Object> pk = new HashMap<String, Object>();
        pk.put("catid", "BIRDS");
        Object bird = QueryUtils.getObjectByPk
                (persistence, "jpetstore", "category", pk);
        assertEquals("Birds", ((Map) bird).get("name"));

        List objs = QueryUtils.getRelatedObjects(persistence, "jpetstore", "category",
                bird, "fk_product_1");
        assertTrue(objs.size()>0);
    }

    @Test(enabled = false) //Disable because it fails on the CI server where the timezone is different
    public void testDateWithDTSSwitch() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Rome"));
        Date problematicDate = new Date(73, 5, 3); //1973-06-03 DTS switch at midnight in tz Europe/Rome

        Map<String, Object> order = new HashMap<>();
        order.put("orderid", new BigInteger("1"));
        order.put("userid", "x");
        order.put("orderdate", problematicDate);
        order.put("shipaddr1", "x");
        order.put("shipaddr2", "x");
        order.put("shipcity", "x");
        order.put("shipstate", "x");
        order.put("shipzip", "x");
        order.put("shipcountry", "x");
        order.put("billaddr1", "x");
        order.put("billaddr2", "x");
        order.put("billcity", "x");
        order.put("billstate", "x");
        order.put("billzip", "x");
        order.put("billcountry", "x");
        order.put("courier", "x");
        order.put("totalprice", new BigDecimal(1.0));
        order.put("billtofirstname", "x");
        order.put("billtolastname", "x");
        order.put("shiptofirstname", "x");
        order.put("shiptolastname", "x");
        order.put("creditcard", "x");
        order.put("exprdate", "x");
        order.put("cardtype", "x");
        order.put("locale", "x");

        Session session = persistence.getSession("jpetstore");
        session.save("orders", order);

        FormBuilder fb = new FormBuilder(persistence.getTableAccessor("jpetstore", "orders"));
        Form form = fb.configFields("orderdate").build();
        DateField dateField = (DateField) form.findFieldByPropertyName("orderdate");
        dateField.readFromObject(order);
        assertEquals("dd-MM-yyyy", ElementsProperties.getConfiguration().getString("fields.date.format"));
        String strDate = "03-06-1973";
        assertEquals(strDate, dateField.getStringValue());

        MutableHttpServletRequest request = new MutableHttpServletRequest();
        request.setParameter("date", strDate);
        dateField.readFromRequest(request);
        assertTrue(dateField.validate());
        dateField.writeToObject(order);
        session.update("orders", order);
        session.flush();
        session.clear(); //Forget about order
        order = (Map) session.get("orders", new BigInteger("1"));
        Date date = new Date(((Date) order.get("orderdate")).getTime()); //Convert to java.util.Date from java.sql.Date
        assertEquals(73, date.getYear());
        assertEquals(5, date.getMonth());
        assertEquals(3, date.getDate());
        assertEquals(1, date.getHours());
        assertEquals(0, date.getMinutes());
        assertEquals(0, date.getSeconds());
        dateField.readFromObject(order);
        assertEquals(strDate, dateField.getStringValue());
    }

    public void testFkComposite(){
        Session session = persistence.getSession("hibernatetest");
        List<Object> list2 = session.createCriteria("table2").list();
        HashMap map = (HashMap)list2.get(0);
        List obj =  (List) map.get("table3_t2_id1_fkey");
        assertNotNull(obj);
        assertTrue(obj.size()>0);
        Map obj2 = (Map) ((Map)obj.get(0)).get("table3_t2_id1_fkey");
        assertNotNull(obj2);
        assertEquals(5, obj2.keySet().size());
    }

    public void tablesWithNoPKAreSkipped() {
        try {
            persistence.getSession("hibernatetest").load("test_no_pk", 1);
            fail("test_no_pk should not be mapped");
        } catch (UnknownEntityTypeException e) {
            //Ok
        }
    }

    public void testAutoIncrementGenerator(){
        Map<String, Object> supplier = new HashMap<>();
        supplier.put("status", "99");
        supplier.put("name", "Giampiero");
        Session session = persistence.getSession("jpetstore");
        session.save("supplier", supplier);
        session.getTransaction().commit();
        Table table = DatabaseLogic.findTableByName(
                persistence.getModel(), "jpetstore", "PUBLIC", "SUPPLIER");
        assertNotNull(table);
        TableAccessor tableAccessor = new TableAccessor(table);
        TableCriteria criteria = new TableCriteria(table);
        final BigInteger expectedId = new BigInteger("3");
        try {
            criteria.eq(tableAccessor.getProperty("suppid"), expectedId);
            List listObjs = QueryUtils.getObjects(session, criteria, null, null);
            assertEquals(1, listObjs.size());
            Map<String,String> supp = (Map<String, String>) listObjs.get(0);
            String name = supp.get("name");
            assertEquals("Giampiero", name);
        } catch (NoSuchFieldException e) {
            fail(e.getMessage(), e);
        }
    }

    /*public void testSequenceGenerator(){
        Map<String, Object> supplier = new HashMap<>();
        final String testEntity = "test";
        supplier.put("$type$", testEntity);
        Session session = persistence.getSession("hibernatetest");
        session.save(testEntity, supplier);
        session.getTransaction().commit();
        Table table = DatabaseLogic.findTableByName(
                persistence.getModel(), "hibernatetest", "PUBLIC", "test");
        TableAccessor tableAccessor = new TableAccessor(table);
        TableCriteria criteria = new TableCriteria(table);
        final long expectedId = 1;
        try {
            criteria.eq(tableAccessor.getProperty("id"), expectedId);
            List listObjs = QueryUtils.getObjects(session, criteria, null, null);
            assertEquals(1, listObjs.size());
        } catch (NoSuchFieldException e) {
            fail(e.getMessage(), e);
        }
    }

    public void testTableGenerator(){
        Map<String, Object> order = new HashMap<>();
        final int expectedId = 1000;
        String ordersEntity = "orders";
        order.put("$type$", ordersEntity);
        order.put("userid", "99");
        order.put("orderdate", new Date());
        order.put("shipaddr1", "99");
        order.put("shipaddr2", "99");
        order.put("shipcity", "99");
        order.put("shipstate", "99");
        order.put("shipzip", "99");
        order.put("billaddr1", "99");
        order.put("billaddr2", "99");
        order.put("billcity", "99");
        order.put("billstate", "99");
        order.put("billzip", "99");
        order.put("billcountry", "99");
        order.put("courier", "99");
        order.put("totalprice", new BigDecimal(99L));
        order.put("billtofirstname", "99");
        order.put("billtolastname", "99");
        order.put("shiptofirstname", "99");
        order.put("shiptolastname", "99");
        order.put("shipcountry", "99");
        order.put("creditcard", "99");
        order.put("exprdate", "99");
        order.put("cardtype", "99");
        order.put("locale", "99");
        Session session = persistence.getSession("jpetstore");
        session.save(ordersEntity, order);
        QueryUtils.commit(persistence, "jpetstore");
        Table table = DatabaseLogic.findTableByName(
                persistence.getModel(), "jpetstore", "PUBLIC", "orders");
        TableAccessor tableAccessor = new TableAccessor(table);
        TableCriteria criteria = new TableCriteria(table);
        try {
            criteria.eq(tableAccessor.getProperty("orderid"), expectedId);
            List listObjs = QueryUtils.getObjects(session, criteria, null, null);
            assertEquals(1, listObjs.size());
            Map<String,String> supp = (Map<String, String>) listObjs.get(0);
            String name = supp.get("userid");
            assertEquals("99", name);
        } catch (NoSuchFieldException e) {
            fail("orderid property not found", e);
        }
    }*/

    public void testViews() {
        Table table = DatabaseLogic.findTableByName(
                persistence.getModel(), "hibernatetest", "PUBLIC", "TEST_VIEW_1");
        assertNotNull(table);
        assertTrue(table instanceof View);
        View view = (View) table;
        assertFalse(view.isInsertable());
        assertFalse(view.isUpdatable());
        Session session = persistence.getSession("hibernatetest");
        try {
            session.createQuery("from test_view_1").list();
            fail("View should not be mapped by default because it has no pk.");
        } catch (IllegalArgumentException e) {
            //OK
        }
        persistence.closeSessions();

        PrimaryKey pk = new PrimaryKey(view);
        view.setPrimaryKey(pk);
        PrimaryKeyColumn id = new PrimaryKeyColumn(pk);
        id.setColumnName("ID");
        pk.getPrimaryKeyColumns().add(id);
        persistence.initModel();
        session = persistence.getSession("hibernatetest");
        List list = session.createQuery("from test_view_1").list();
        assertEquals(2, list.size());
    }

    public void testAnnotations() {
        Session session = persistence.getSession("hibernatetest");
        try {
            session.createNamedQuery("all_questions", Map.class).list();
            fail("Exception expected");
        } catch (Exception e) {}
        Table table = DatabaseLogic.findTableByName(persistence.getModel(), "hibernatetest", "PUBLIC", "DOMANDA");
        assertNotNull(table);
        Annotation nq = new Annotation(table, "javax.persistence.NamedQuery");
        nq.setProperties(Arrays.asList(new Property("name", "all_questions"), new Property("query", "from domanda")));
        table.getAnnotations().add(nq);
        persistence.initModel();
        session = persistence.getSession("hibernatetest");
        session.createNamedQuery("all_questions", Map.class).list();
    }
}
