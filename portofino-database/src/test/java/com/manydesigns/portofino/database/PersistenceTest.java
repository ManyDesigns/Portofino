package com.manydesigns.portofino.database;

import com.manydesigns.elements.ElementsProperties;
import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.fields.DateField;
import com.manydesigns.elements.fields.search.Criteria;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.servlet.MutableHttpServletRequest;
import com.manydesigns.portofino.cache.CacheResetListenerRegistry;
import com.manydesigns.portofino.code.JavaCodeBase;
import com.manydesigns.portofino.config.ConfigurationSource;
import com.manydesigns.portofino.database.model.*;
import com.manydesigns.portofino.database.model.platforms.DatabasePlatformsRegistry;
import com.manydesigns.portofino.database.platforms.H2DatabasePlatform;
import com.manydesigns.portofino.model.Annotation;
import com.manydesigns.portofino.model.AnnotationProperty;
import com.manydesigns.portofino.model.service.ModelService;
import com.manydesigns.portofino.modules.DatabaseModule;
import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.persistence.QueryUtils;
import com.manydesigns.portofino.persistence.hibernate.DatabaseAccessor;
import com.manydesigns.portofino.persistence.hibernate.Events;
import com.manydesigns.portofino.reflection.TableAccessor;
import io.reactivex.disposables.Disposable;
import jakarta.persistence.criteria.CriteriaQuery;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.h2.tools.RunScript;
import org.hibernate.Session;
import org.hibernate.UnknownEntityTypeException;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostLoadEvent;
import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreLoadEvent;
import org.hibernate.jdbc.Work;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.testng.Assert.*;
import static org.testng.AssertJUnit.assertEquals;

@Test
public class PersistenceTest {

    DatabaseModule databaseModule;
    Persistence persistence;
    ModelService modelService;

    @BeforeClass
    public void setupElements() {
        ElementsThreadLocals.setupDefaultElementsContext();
    }

    /*@AfterClass this seems to break CI
    public void teardownElements() {
        ElementsThreadLocals.destroy();
    }*/

    @BeforeMethod
    public void setup() throws Exception {
        FileObject appDir = VFS.getManager().resolveFile("res:com/manydesigns/portofino/database/model");
        setup(appDir);
    }

    protected void setup(FileObject appDir) throws Exception {
        Configuration configuration = new PropertiesConfiguration();
        final DatabasePlatformsRegistry databasePlatformsRegistry = new DatabasePlatformsRegistry(configuration);
        databasePlatformsRegistry.addDatabasePlatform(new H2DatabasePlatform());
        databaseModule = new DatabaseModule() {
            @Override
            public void destroy() {
                if(subscription != null) {
                    subscription.dispose();
                    subscription = null;
                }
            }
        };
        databaseModule.applicationDirectory = appDir;
        databaseModule.configuration = new ConfigurationSource(configuration, null);
        modelService = new ModelService(appDir, new ConfigurationSource(configuration, null), new JavaCodeBase(appDir));
        modelService.loadModel();
        modelService.getModel().getIssues().forEach(
                i -> System.err.println(i.message + " " + i.path + "@" + i.line+ ":" + i.column));
        assertEquals("There are issues with the model", 0, modelService.getModel().getIssues().size());
        persistence = databaseModule.getPersistence(
                modelService, databasePlatformsRegistry, new CacheResetListenerRegistry());
        configure(persistence);
        persistence.start();
        setupJPetStore();
        setupHibernateTest();
        persistence.initModel();
    }

    protected void configure(Persistence persistence) {
        persistence.setConvertLegacyModel(true);
    }

    @AfterMethod
    public void teardown() {
        persistence.getDatabases().forEach(d -> d.getSchemas().forEach(s -> {
            s.getTables().clear();
            s.getModelElement().getEClassifiers().clear();
        }));
        persistence.stop();
        databaseModule.destroy();
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
        Table supplierTable = DatabaseLogic.findTableByName(persistence.getDatabases(), "jpetstore", "PUBLIC", "SUPPLIER");
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

    protected Object makeEntity(String className, Map<String, Object> data) {
        return new HashMap<>(data);
    }

    protected void set(Object entity, String property, Object value) {
        ((Map) entity).put(property, value);
    }

    protected <T> T get(Object entity, String property) {
        return (T) ((Map) entity).get(property);
    }

    public void testReadProdotti() {
        Session session = persistence.getSession("jpetstore");
        CriteriaQuery criteria = QueryUtils.createCriteria(session,"product").query;
        List resultProd = new ArrayList(session.createQuery(criteria).list());

        int sizePrd = resultProd.size();
        assertEquals("prodotti", 16, sizePrd);
    }

    public void testSearchAndReadCategorieProdotti() {
        Session session = persistence.getSession("jpetstore");
        CriteriaQuery criteria = QueryUtils.createCriteria(session,"category").query;
        List resultCat = new ArrayList(session.createQuery(criteria).list());

        int sizeCat = resultCat.size();
        assertEquals("categorie", 5, sizeCat);

        resultCat.forEach(cat -> {
            assertNotNull(get(cat, "name"));
        });

        criteria = QueryUtils.createCriteria(session,"product").query;
        List resultProd = new ArrayList(session.createQuery(criteria).list());

        Table table = DatabaseLogic.findTableByName(
                persistence.getDatabases(), "jpetstore", "PUBLIC", "CATEGORY");
        TableAccessor tableAccessor = new TableAccessor(table);
        Criteria Criteria = new Criteria();
        findCategory(tableAccessor, Criteria);

        int sizePrd = resultProd.size();
        assertEquals("prodotti", 16, sizePrd);
        Object prd0 = resultProd.get(0);
        assertEquals("FI-SW-01", get(prd0, "productid") );
        assertEquals("Angelfish", get(prd0, "name"));
    }

    private Object findCategory(TableAccessor tableAccessor, Criteria criteria) {
        Object category = null;
        try {
            criteria.eq(tableAccessor.getProperty("catid"), "FISH");
            Table table = tableAccessor.getTable();
            Session session = persistence.getSession(table.getDatabaseName());
            List<Object> listObjs = QueryUtils.getObjects(session, table, criteria, null, null);
            assertEquals(1, listObjs.size());
            category = listObjs.get(0);
            String catid = get(category, "catid");
            assertEquals("FISH", catid);
        } catch (NoSuchFieldException e) {
            fail(e.getMessage(), e);
        }
        return category;
    }

    public void testSearchAndUpdateCategorie() {
        Table table = DatabaseLogic.findTableByName(
                persistence.getDatabases(), "jpetstore", "PUBLIC", "CATEGORY");
        assertNotNull(table);
        TableAccessor tableAccessor = new TableAccessor(table);
        Criteria criteria = new Criteria();

        Session session = persistence.getSession("jpetstore");
        CriteriaQuery<Object> criteriaQuery = QueryUtils.createCriteria(session, "category").query;
        List<Object> resultCat = new ArrayList<>(session.createQuery(criteriaQuery).list());

        int sizeCat = resultCat.size();
        assertEquals("categorie", 5, sizeCat);
        Object categoria0 = findCategory(tableAccessor, criteria);
        assertEquals("Fish", get(categoria0, "name"));
        set(categoria0, "name", "Pesciu");
        session.merge("category", categoria0);
        session.getTransaction().commit();
        persistence.closeSessions();

        //Controllo l'aggiornamento e riporto le cose come stavano
        criteria = new Criteria();
        categoria0 =  findCategory(tableAccessor, criteria);
        assertEquals("Pesciu", get(categoria0, "name"));
        set(categoria0, "name", "Fish");
        session = persistence.getSession("jpetstore");
        session.merge("category", categoria0);
        session.getTransaction().commit();
        persistence.closeSessions();
    }

    public void testSaveCategoria() {
        Map<String, Object> worms = new HashMap<>();
        worms.put("catid", "VERMI");
        worms.put("name", "worms");
        worms.put("descn",
                "<image src=\"../images/worms_icon.gif\"><font size=\"5\" color=\"blue\">" +
                        "Worms</font>");

        String databaseName = "jpetstore";
        AtomicReference<PreInsertEvent> preInsert = new AtomicReference<>();
        AtomicReference<PostInsertEvent> postInsert = new AtomicReference<>();
        DatabaseAccessor databaseAccessor = persistence.getDatabaseAccessor(databaseName);
        Events events = databaseAccessor.getEvents();
        Disposable postInsertD = events.postInsert$.subscribe(postInsert::set);
        Disposable preInsertD = events.preInsert$.subscribe(preInsert::set);
        Session session = databaseAccessor.getThreadSession();
        session.persist("category", makeEntity("jpetstore.public.Category", worms));
        session.getTransaction().commit();
        assertNotNull(preInsert.get());
        assertNotNull(postInsert.get());
        assertFalse(postInsertD.isDisposed());
        assertFalse(preInsertD.isDisposed());
        persistence.stop();
        assertTrue(postInsertD.isDisposed());
        assertTrue(preInsertD.isDisposed());
    }

    public void testSaveLineItem() {
        Map<String, Object> lineItemData = new HashMap<>();
        lineItemData.put("orderid", new BigInteger("2"));
        lineItemData.put("linenum", new BigInteger("2"));
        lineItemData.put("itemid", "test");
        lineItemData.put("quantity", new BigInteger("20"));
        lineItemData.put("unitprice", new BigDecimal("10.80"));
        Object lineItem = makeEntity("jpetstore.public.Lineitem", lineItemData);

        Session session = persistence.getSession("jpetstore");
        session.persist("lineitem", lineItem);
        session.getTransaction().commit();

        persistence.closeSessions();

        session = persistence.getSession("jpetstore");
        session.delete("lineitem", lineItem); //Note: there isn't a replacement taking an explicit entity name even if delete is deprecated
        session.getTransaction().commit();
        persistence.closeSessions();
    }

    public void testSaveTestElement() throws Exception {
        Map<String, Object> testItemData = new HashMap<>();
        testItemData.put("testo", "esempio");
        Session session = persistence.getSession("hibernatetest");
        session.persist("table1", makeEntity("hibernatetest.public.Table1", testItemData));
        session.getTransaction().commit();
    }

    public void testDeleteCategoria() {
        Map<String, Object> worms = new HashMap<>();
        worms.put("catid", "VERMI");
        worms.put("name", "worms");
        worms.put("descn",
                "<image src=\"../images/worms_icon.gif\"><font size=\"5\" color=\"blue\">" +
                        "Worms</font>");
        Object wormsEntity = makeEntity("jpetstore.public.Category", worms);

        Session session = persistence.getSession("jpetstore");
        session.persist("category", wormsEntity);
        session.getTransaction().commit();
        session.beginTransaction();
        session.remove(wormsEntity);
        session.getTransaction().commit();
    }

    public void testGetObjByPk(){
        //Test Chiave singola
        HashMap<String, Object> pk = new HashMap<>();
        pk.put("catid", "BIRDS");
        Object bird =  QueryUtils.getObjectByPk
                (persistence, "jpetstore", "category", makeEntity("jpetstore.public.Category", pk));
        assertEquals("Birds", get(bird, "name"));

        //Test Chiave composta
        pk = new HashMap<>();
        pk.put("orderid", new BigInteger("1"));
        pk.put("linenum", new BigInteger("1"));
        Object lineItem = QueryUtils.getObjectByPk
                (persistence, "jpetstore", "lineitem", makeEntity("jpetstore.public.Lineitem", pk));
        assertEquals("EST-1", get(lineItem, "itemid"));
    }

    public void testForeignKeyNavigation() {
        HashMap<String, Object> pk = new HashMap<>();
        pk.put("catid", "BIRDS");
        Object bird = QueryUtils.getObjectByPk
                (persistence, "jpetstore", "category", makeEntity("jpetstore.public.Category", pk));
        assertEquals("Birds", get(bird, "name"));
        assertTrue(get(bird, "fk_product_1") instanceof Collection);
        assertFalse(((Collection) get(bird, "fk_product_1")).isEmpty());
        for(Object o : ((Collection) get(bird, "fk_product_1"))) {
            assertNotNull(o);
            assertEquals(bird, get(o, "fk_product_1"));
        }

        pk = new HashMap<>();
        pk.put("regione", "liguria");
        pk.put("provincia", "genova");
        pk.put("comune", "rapallo");
        Object comune = QueryUtils.getObjectByPk
                (persistence, "hibernatetest", "comune", makeEntity("hibernatetest.public.Comune", pk));

        /* not supported.
        assertTrue(comune.get("domanda_comune_fkey") instanceof Collection);
        assertFalse(((Collection) comune.get("domanda_comune_fkey")).isEmpty());
        for(Object o : ((Collection) comune.get("domanda_comune_fkey"))) {
            assertNotNull(o);
            assertEquals(pk.get("comune"), ((Map) ((Map) o).get("domanda_comune_fkey")).get("comune"));
        }*/

        assertTrue(get(comune, "domanda_regione_fkey") instanceof Collection);
        assertFalse(((Collection) get(comune, "domanda_regione_fkey")).isEmpty());
        for(Object o : ((Collection) get(comune, "domanda_regione_fkey"))) {
            assertNotNull(o);
        }
    }

    public void testGetRelatedObjects(){
        HashMap<String, Object> pk = new HashMap<String, Object>();
        pk.put("catid", "BIRDS");
        Object bird = QueryUtils.getObjectByPk
                (persistence, "jpetstore", "category", makeEntity("jpetstore.public.Category", pk));
        assertEquals("Birds", get(bird, "name"));

        List objs = QueryUtils.getRelatedObjects(persistence, "jpetstore", "category",
                bird, "fk_product_1");
        assertTrue(objs.size() > 0);
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
        order.put("totalprice", new BigDecimal("1.0"));
        order.put("billtofirstname", "x");
        order.put("billtolastname", "x");
        order.put("shiptofirstname", "x");
        order.put("shiptolastname", "x");
        order.put("creditcard", "x");
        order.put("exprdate", "x");
        order.put("cardtype", "x");
        order.put("locale", "x");

        Session session = persistence.getSession("jpetstore");
        session.persist("orders", order);

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
        session.merge("orders", order);
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

    public void testFkComposite() throws Exception {
        Session session = persistence.getSession("hibernatetest");
        List<Object> list2 = session.createQuery(QueryUtils.createCriteria(session, "table2").query).list();
        Object map = list2.get(0);
        List<?> obj = get(map, "table3_t2_id1_fkey");
        assertNotNull(obj);
        assertTrue(obj.size()>0);
        Object obj2 = get(obj.get(0), "table3_t2_id1_fkey");
        assertNotNull(obj2);
    }

    public void tablesWithNoPKAreSkipped() {
        try {
            persistence.getSession("hibernatetest").getReference("test_no_pk", 1);
            fail("test_no_pk should not be mapped");
        } catch (IllegalArgumentException e) {
            //Ok
        }
    }

    public void testAutoIncrementGenerator(){
        Map<String, Object> supplierData = new HashMap<>();
        supplierData.put("status", "99");
        supplierData.put("name", "Giampiero");
        Object supplier = makeEntity("jpetstore.public.Supplier", supplierData);
        Session session = persistence.getSession("jpetstore");
        session.persist("supplier", supplier);
        session.getTransaction().commit();
        Table table = DatabaseLogic.findTableByName(
                persistence.getDatabases(), "jpetstore", "PUBLIC", "SUPPLIER");
        assertNotNull(table);
        TableAccessor tableAccessor = new TableAccessor(table);
        Criteria criteria = new Criteria();
        final BigInteger expectedId = new BigInteger("3");
        try {
            criteria.eq(tableAccessor.getProperty("suppid"), expectedId);
            List<?> listObjs = QueryUtils.getObjects(session, table, criteria, null, null);
            assertEquals(1, listObjs.size());
            Object supp = listObjs.get(0);
            String name = get(supp, "name");
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
        Criteria criteria = new Criteria(table);
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
        Criteria criteria = new Criteria(table);
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
                persistence.getDatabases(), "hibernatetest", "PUBLIC", "TEST_VIEW_1");
        assertNotNull(table);
        assertTrue(table instanceof View);
        View view = (View) table;
        assertFalse(view.isInsertable());
        assertFalse(view.isUpdatable());
        Session session = persistence.getSession("hibernatetest");
        try {
            session.createQuery("from test_view_1", Object.class).list();
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
        List<?> list = session.createQuery("from test_view_1", Object.class).list();
        assertEquals(2, list.size());
    }

    public void testAnnotations() {
        Table table = DatabaseLogic.findTableByName(
                persistence.getDatabases(), "hibernatetest", "PUBLIC", "DOMANDA");
        Session session = persistence.getSession("hibernatetest");
        try {
            session.createNamedQuery("all_questions", Map.class).list();
            fail("Exception expected");
        } catch (Exception e) {}
        assertNotNull(table);
        Annotation nq = table.ensureAnnotation("jakarta.persistence.NamedQuery");
        nq.setProperties(Arrays.asList(
                new AnnotationProperty(nq, "name", "all_questions"),
                new AnnotationProperty(nq, "query", "from domanda")));
        persistence.initModel();
        session = persistence.getSession("hibernatetest");
        List<Object> allQuestions = session.createNamedQuery("all_questions", Object.class).list();
        assertEquals(2, allQuestions.size());
    }

    public void testDateAndTimeAPIMapping() {
        Table table = DatabaseLogic.findTableByName(persistence.getDatabases(), "hibernatetest", "PUBLIC", "DOMANDA");
        assertNotNull(table);
        Column column = DatabaseLogic.findColumnByName(table, "DATA");
        assertNotNull(column);
        assertEquals(java.sql.Date.class, column.getActualJavaType());
        column.setJavaType(LocalDate.class.getName());
        persistence.initModel();
        Object domanda = persistence.getSession("hibernatetest").get("domanda", "0001");
        assertEquals(LocalDate.of(2010, 9, 27), get(domanda,"data"));
    }

    public void testLoadById() {
        String databaseName = "hibernatetest";
        AtomicReference<PreLoadEvent> preLoad = new AtomicReference<>();
        AtomicReference<PostLoadEvent> postLoad = new AtomicReference<>();
        DatabaseAccessor databaseAccessor = persistence.getDatabaseAccessor(databaseName);
        Events events = databaseAccessor.getEvents();
        Disposable postLoadD = events.postLoad$.subscribe(postLoad::set);
        Disposable preLoadD = events.preLoad$.subscribe(preLoad::set);
        Object domanda = databaseAccessor.getThreadSession().get("domanda", "0001");
        assertNotNull(domanda);
        assertNotNull(preLoad.get());
        assertNotNull(postLoad.get());
        assertFalse(postLoadD.isDisposed());
        assertFalse(preLoadD.isDisposed());
        persistence.stop();
        assertTrue(postLoadD.isDisposed());
        assertTrue(preLoadD.isDisposed());
    }

    public void testTableWithSpaces() {
        Session hibernatetest = persistence.getSession("hibernatetest");
        hibernatetest.createQuery("from test_spaces", Object.class).list();
    }

    public void testSaveModel() throws Exception {
        persistence.stop();
        FileObject modelSource = VFS.getManager().resolveFile("res:com/manydesigns/portofino/database/model/legacy");
        FileObject appDir = VFS.getManager().resolveFile("ram:/portofino");
        appDir.createFolder();
        try {
            appDir.copyFrom(modelSource, new AllFileSelector());
            setup(appDir);
            Database hibernatetest = DatabaseLogic.findDatabaseByName(persistence.getDatabases(), "hibernatetest");
            hibernatetest.setDatabaseName("test");
            FileObject file;
            FileObject dbsDir = appDir.resolveFile(ModelService.APP_MODEL_DIRECTORY).resolveFile(Persistence.DATABASES_DOMAIN_NAME);
            file = dbsDir.resolveFile("test");
            persistence.saveModel();
            assertTrue(file.exists());
            //Old directory is deleted
            assertFalse(dbsDir.resolveFile("hibernatetest").exists());
            assertTrue(file.resolveFile("PUBLIC").exists());
        } finally {
            appDir.deleteAll();
        }
    }

    public void testSelectionProviders() throws IOException, ConfigurationException {
        Table table1 = persistence.getTableAccessor("hibernatetest", "table1").getTable();
        DatabaseSelectionProvider sp = new DatabaseSelectionProvider(table1);
        sp.setName("dbsp");
        sp.setHql("from table1");
        Reference ref = new Reference(sp);
        ref.setFromColumn("ID");
        sp.getReferences().add(ref);
        table1.getSelectionProviders().add(sp);
        // TODO save model in memory like the previous test
        //modelService.saveModel();
        //persistence.stop();
        //persistence.start();
        table1 = persistence.getTableAccessor("hibernatetest", "table1").getTable();
        assertEquals(1, table1.getSelectionProviders().size());
        DatabaseSelectionProvider dbsp = (DatabaseSelectionProvider) table1.getSelectionProviders().get(0);
        assertEquals("dbsp", dbsp.getName());
        assertEquals("from table1", dbsp.getHql());
    }

    public void testDisabledDatabasesAreSkipped() {
        assertNotNull(DatabaseLogic.findDatabaseByName(persistence.getDatabases(), "disabled"));
        Error error = null;
        try {
            persistence.getSession("disabled");
        } catch (Error e) {
            error = e;
        }
        if(error == null) {
            fail("Was expecting an exception");
        }
    }

    public void testLiquibaseSQL() throws Exception {
        persistence.syncDataModel("liquibase_sql");
        persistence.initModel();
        TableAccessor usersTable = persistence.getTableAccessor("liquibase_sql", "sql_users");
        assertNotNull(usersTable);
        assertNotNull(usersTable.getProperty("id"));
    }

    public void testLiquibaseXML() throws Exception {
        persistence.syncDataModel("liquibase_xml");
        persistence.initModel();
        TableAccessor usersTable = persistence.getTableAccessor("liquibase_xml", "xml_users");
        assertNotNull(usersTable);
        assertNotNull(usersTable.getProperty("id"));
    }

}
