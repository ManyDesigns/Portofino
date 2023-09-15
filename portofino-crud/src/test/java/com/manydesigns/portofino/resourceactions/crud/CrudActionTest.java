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

package com.manydesigns.portofino.resourceactions.crud;

import com.fasterxml.jackson.core.util.JacksonFeature;
import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.annotations.FileBlob;
import com.manydesigns.elements.annotations.Required;
import com.manydesigns.elements.blobs.Blob;
import com.manydesigns.elements.blobs.BlobUtils;
import com.manydesigns.elements.blobs.HierarchicalBlobManager;
import com.manydesigns.elements.fields.AbstractBlobField;
import com.manydesigns.elements.fields.Field;
import com.manydesigns.elements.fields.FileBlobField;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.servlet.MutableHttpServletRequest;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.code.JavaCodeBase;
import com.manydesigns.portofino.config.ConfigurationSource;
import com.manydesigns.portofino.database.model.Column;
import com.manydesigns.portofino.database.model.DatabaseLogic;
import com.manydesigns.portofino.database.model.IncrementGenerator;
import com.manydesigns.portofino.database.model.Table;
import com.manydesigns.portofino.database.model.platforms.DatabasePlatformsRegistry;
import com.manydesigns.portofino.database.platforms.H2DatabasePlatform;
import com.manydesigns.portofino.dispatcher.web.ApplicationRoot;
import com.manydesigns.portofino.dispatcher.web.WebDispatcherInitializer;
import com.manydesigns.portofino.model.Annotation;
import com.manydesigns.portofino.model.service.ModelService;
import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.resourceactions.ActionContext;
import com.manydesigns.portofino.resourceactions.ActionInstance;
import com.manydesigns.portofino.resourceactions.crud.configuration.CrudProperty;
import com.manydesigns.portofino.resourceactions.crud.configuration.database.CrudConfiguration;
import com.manydesigns.portofino.resourceactions.crud.export.CrudExporterRegistry;
import com.manydesigns.portofino.resourceactions.crud.export.JSONExporter;
import com.manydesigns.portofino.rest.messagebodywriters.FormMessageBodyWriter;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.media.sse.EventSource;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.h2.tools.RunScript;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.hibernate.query.Query;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.*;

import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.manydesigns.portofino.resourceactions.crud.AbstractCrudAction.PORTOFINO_API_VERSION_5_2;
import static org.testng.Assert.*;

@SuppressWarnings({"JpaQlInspection"})
@Test
public class CrudActionTest extends JerseyTest {

    Persistence persistence;

    @BeforeClass
    public void setupElementsAndJersey() throws Exception {
        ElementsThreadLocals.setupDefaultElementsContext();
        super.setUp();
    }

    @AfterClass
    public void teardownElements() {
        ElementsThreadLocals.destroy();
    }

    @BeforeMethod
    public void setup() throws Exception {
        FileObject appDir = VFS.getManager().resolveFile("res:com/manydesigns/portofino/resourceactions/crud/app");
        setup(appDir);
    }

    protected void setup(FileObject appDir) throws Exception {
        Configuration configuration = new PropertiesConfiguration();
        DatabasePlatformsRegistry databasePlatformsRegistry = new DatabasePlatformsRegistry(configuration);
        databasePlatformsRegistry.addDatabasePlatform(new H2DatabasePlatform());
        ModelService modelService = new ModelService(
                appDir, new ConfigurationSource(configuration, null), new JavaCodeBase(appDir));
        modelService.loadModel();
        persistence = new Persistence(
                modelService,
                new ConfigurationSource(configuration, null), databasePlatformsRegistry);
        persistence.setConvertLegacyModel(false);
        persistence.start();
        setupJPetStore();
        persistence.initModel();
    }

    @AfterMethod
    public void teardown() {
        persistence.stop();
    }

    CrudAction testCrudAction;

    @Override
    protected ResourceConfig configure() {
        try {
            setup();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        testCrudAction = makeTestCrudAction();
        ActionInstance actionInstance = new ActionInstance(null, null, CrudAction.class);
        ActionContext actionContext = new ActionContext();
        MutableHttpServletRequest req = new MutableHttpServletRequest();
        actionContext.setRequest(req);
        actionContext.setServletContext(req.getServletContext());
        actionContext.setActionPath("/test");
        setUpCrudAction(testCrudAction, actionInstance, actionContext);
        return new ResourceConfig(ApplicationRoot.class)
                .register(JacksonFeature.class)
                .register(FormMessageBodyWriter.class)
                .register(testCrudAction);
    }

    @Override
    protected void configureClient(ClientConfig config) {
        super.configureClient(config);
        config.property(ClientProperties.CONNECT_TIMEOUT, 15000);
        config.property(ClientProperties.READ_TIMEOUT, 0);
        config.property(ClientProperties.ASYNC_THREADPOOL_SIZE, 8);
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() {
        return new GrizzlyWebTestContainerFactory();
    }

    @Override
    protected DeploymentContext configureDeployment() {
        try (FileObject appRoot = VFS.getManager().resolveFile(
                "res:com/manydesigns/portofino/resourceactions/crud/app"
        )) {
            return ServletDeploymentContext.forServlet(new ServletContainer(configure())).
                    contextParam("portofino.application.directory", appRoot.getURL().toString()).
                    addListener(WebDispatcherInitializer.class).
                    build();
        } catch (FileSystemException e) {
            throw new RuntimeException(e);
        }
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
        Table supplierTable = DatabaseLogic.findTableByName(
                persistence.getDatabases(), "jpetstore", "PUBLIC", "SUPPLIER");
        supplierTable.getPrimaryKey().getPrimaryKeyColumns().get(0).setGenerator(new IncrementGenerator());
        //Table testTable = DatabaseLogic.findTableByName(persistence.getModel(), "jpetstore", "PUBLIC", "TEST");
        //testTable.getPrimaryKey().getPrimaryKeyColumns().get(0).setGenerator(new SequenceGenerator());
    }

    public void testSearch() throws Exception {
        MutableHttpServletRequest req = new MutableHttpServletRequest();
        ElementsThreadLocals.setMultipart(req);
        req.getServletContext().setInitParameter("portofino.api.root", "http://fake");
        persistence.initModel();
        CrudAction crudAction = makeTestCrudAction();

        ActionInstance actionInstance = new ActionInstance(null, null, CrudAction.class);
        actionInstance.getParameters().add("1");
        ActionContext actionContext = new ActionContext();
        actionContext.setRequest(req);
        actionContext.setActionPath("");
        actionContext.setServletContext(req.getServletContext());
        setUpCrudAction(crudAction, actionInstance, actionContext);

        crudAction.executeSearch();
        assertEquals(16, crudAction.getTotalSearchRecords());
        assertEquals(16, crudAction.getTableForm().getRows().length);

        //Category is not searchable, so this is ignored
        crudAction.searchString = "search_category=none";
        crudAction.executeSearch();
        assertEquals(16, crudAction.getTotalSearchRecords());
        assertEquals(16, crudAction.getTableForm().getRows().length);
        crudAction.collection.getCriteria().clear();

        crudAction.searchString = "search_productid=none";
        crudAction.executeSearch();
        assertEquals(0, crudAction.getTotalSearchRecords());
        assertEquals(0, crudAction.getTableForm().getRows().length);
        crudAction.collection.getCriteria().clear();

        crudAction.searchString = "search_productid=FI-SW-01";
        crudAction.executeSearch();
        assertEquals(1, crudAction.getTotalSearchRecords());
        assertEquals(1, crudAction.getTableForm().getRows().length);
        crudAction.collection.getCriteria().clear();

        crudAction.searchString = "search_productid=FI-SW,search_productid_mode=STARTS";
        crudAction.executeSearch();
        assertEquals(2, crudAction.getTotalSearchRecords());
        assertEquals(2, crudAction.getTableForm().getRows().length);
        crudAction.collection.getCriteria().clear();
    }

    public void testBlobs() throws Exception {
        MutableHttpServletRequest req = new MutableHttpServletRequest();
        ElementsThreadLocals.setMultipart(req);
        req.getServletContext().setInitParameter("portofino.api.root", "http://fake");
        req.makeMultipart();

        Column column = DatabaseLogic.findColumnByName(
                persistence.getDatabases(), "jpetstore", "PUBLIC", "PRODUCT", "DESCN");
        Annotation ann = new Annotation(column, FileBlob.class.getName());
        column.getAnnotations().add(ann);

        persistence.initModel();
        CrudAction crudAction = makeTestCrudAction();

        String metaFilenamePattern = "blob-{0}.properties";
        String dataFilenamePattern = "blob-{0}.data";
        crudAction.blobManager = new HierarchicalBlobManager(new File(System.getProperty("java.io.tmpdir")), metaFilenamePattern, dataFilenamePattern);

        req.setParameter("productid", "1");
        Query<Map> query = persistence.getSession("jpetstore").createQuery("from category", Map.class);
        Map category = query.list().get(0);
        req.setParameter("category", (String) category.get("catid"));

        ActionInstance actionInstance = new ActionInstance(null, null, CrudAction.class);
        actionInstance.getParameters().add("1");
        ActionContext actionContext = new ActionContext();
        actionContext.setRequest(req);
        actionContext.setActionPath("");
        actionContext.setServletContext(req.getServletContext());
        setUpCrudAction(crudAction, actionInstance, actionContext);
        crudAction.setupForm(Mode.CREATE);

        Field descnField = crudAction.getForm().findFieldByPropertyName("descn");
        assertNotNull(descnField);
        assertTrue(descnField instanceof FileBlobField);

        File tmpFile = File.createTempFile("blob", "blob");
        DiskFileItem fileItem =
                new DiskFileItem(
                        "descn", "application/octet-stream", false,
                        tmpFile.getName(), 0, tmpFile.getParentFile()) {
                    @Override
                    public void delete() {
                        //Do nothing as we want to reuse this
                    }
                };
        OutputStream os = fileItem.getOutputStream();
        IOUtils.write("some test data", os, req.getCharacterEncoding());
        req.addFileItem("descn", fileItem);
        req.setParameter("descn_operation", AbstractBlobField.UPLOAD_MODIFY);
        crudAction.httpPostMultipart();

        assertFalse(crudAction.form.validate());
        AbstractBlobField blobField = (AbstractBlobField) crudAction.form.findFieldByPropertyName("descn");
        assertNotNull(blobField.getValue());
        assertEquals(tmpFile.getName(), blobField.getValue().getFilename());
        assertEquals(fileItem.getSize(), blobField.getValue().getSize());
        try {
            crudAction.getBlobManager().loadMetadata(new Blob(blobField.getValue().getCode()));
            fail("The blob was saved despite validation failing");
        } catch (Exception e) {}
        crudAction.object = null;

        req.setParameter(blobField.getCodeInputName(), blobField.getValue().getCode());
        req.setParameter("name", "name");
        req.setParameter("productid", "1");
        req.setParameter("category", "BIRDS");
        crudAction.httpPostMultipart();
        assertTrue(crudAction.form.validate());
        blobField = (FileBlobField) crudAction.form.findFieldByPropertyName("descn");
        assertNotNull(blobField.getValue());
        crudAction.blobManager.loadMetadata(blobField.getValue()); //This is necessary because the crud might reload the form
        assertEquals(tmpFile.getName(), blobField.getValue().getFilename());
        assertEquals(fileItem.getSize(), blobField.getValue().getSize());
        try {
            crudAction.blobManager.loadMetadata(new Blob(blobField.getValue().getCode()));
        } catch (IOException e) {
            e.printStackTrace();
            fail("The blob was not saved");
        }

        crudAction.httpPutMultipart();
        assertTrue(crudAction.form.validate());
        blobField = (FileBlobField) crudAction.form.findFieldByPropertyName("descn");
        assertNotNull(blobField.getValue());
        crudAction.blobManager.loadMetadata(blobField.getValue()); //This is necessary because the crud might reload the form
        assertEquals(tmpFile.getName(), blobField.getValue().getFilename());
        String oldBlobCode = blobField.getValue().getCode();
        assertEquals(fileItem.getSize(), blobField.getValue().getSize());

        req.setParameter("descn_operation", FileBlobField.UPLOAD_MODIFY);
        req.setFileItem("descn", fileItem);
        crudAction.httpPutMultipart();
        assertTrue(crudAction.form.validate());
        blobField = (FileBlobField) crudAction.form.findFieldByPropertyName("descn");
        assertNotNull(blobField.getValue());
        crudAction.blobManager.loadMetadata(blobField.getValue()); //This is necessary because the crud might reload the form
        assertEquals(tmpFile.getName(), blobField.getValue().getFilename());
        String newBlobCode = blobField.getValue().getCode();

        assertNotEquals(oldBlobCode, newBlobCode);
        crudAction.blobManager.loadMetadata(new Blob(newBlobCode));
        try {
            crudAction.blobManager.loadMetadata(new Blob(oldBlobCode));
            fail("The blob " + oldBlobCode + " should have been deleted");
        } catch (IOException e) {
            //Ok
        }

        Session session = persistence.getSession("jpetstore");
        session.flush();
        Object id = ((Map) crudAction.object).get("productid");
        int qres = session.
                createNativeQuery("update product set descn = 'illegal' where productid = :id", Object.class).
                setParameter("id", id).
                executeUpdate();
        assertEquals(1, qres);
        session.flush();
        session.getTransaction().commit();
        session.clear();
        session.beginTransaction();
        //Force loading the object from the DB
        crudAction.getParameters().add(id.toString());
        crudAction.parametersAcquired();
        crudAction.setupForm(Mode.VIEW);
        crudAction.form.readFromObject(crudAction.object);
        BlobUtils.loadBlobs(crudAction.form, crudAction.getBlobManager(), false);
        blobField = (FileBlobField) crudAction.form.findFieldByPropertyName("descn");
        assertNotNull(blobField.getValue());
        assertNotNull(blobField.getBlobError());
        assertNull(blobField.getValue().getFilename());

        qres = session.
                createNativeQuery("update product set descn = :blobCode where productid = :id", Object.class).
                setParameter("id", id).
                setParameter("blobCode", newBlobCode).
                executeUpdate();
        assertEquals(1, qres);
        session.flush();
        session.getTransaction().commit();
        session.clear();
        session.beginTransaction();
        crudAction.parametersAcquired(); //Force reload
        crudAction.httpDelete(Collections.emptyList(), PORTOFINO_API_VERSION_5_2.toString());
        try {
            crudAction.blobManager.loadMetadata(new Blob(newBlobCode));
            fail("The blob " + newBlobCode + " should have been deleted");
        } catch (IOException e) {
            //Ok
        }
    }

    private void setUpCrudAction(CrudAction crudAction, ActionInstance actionInstance, ActionContext actionContext) {
        CrudConfiguration configuration = crudAction.getCrudConfiguration();
        configuration.persistence = persistence;
        configuration.setSubscriptionSupport("fine");
        configuration.init();
        actionInstance.setConfiguration(configuration);
        crudAction.persistence = persistence;
        crudAction.portofinoConfiguration = new ConfigurationSource(null, null);
        crudAction.setContext(actionContext);
        crudAction.setActionInstance(actionInstance);
        crudAction.configured();
        crudAction.init();
    }

    @NotNull
    private CrudAction makeTestCrudAction() {
        CrudAction crudAction = new TestCrudAction();
        CrudConfiguration configuration = new CrudConfiguration();
        configuration.setDatabase("jpetstore");
        configuration.setQuery("from product");

        CrudExporterRegistry registry = new CrudExporterRegistry();
        registry.register(new JSONExporter());
        crudAction.crudExporterRegistry = registry;

        CrudProperty property = new CrudProperty();
        property.setName("productid");
        property.setEnabled(true);
        property.setInsertable(true);
        property.setUpdatable(true);
        property.setInSummary(true);
        property.setSearchable(true);
        configuration.getProperties().add(property);

        property = new CrudProperty();
        property.setName("category");
        property.setEnabled(true);
        property.setInsertable(true);
        property.setUpdatable(true);
        property.setInSummary(true);
        configuration.getProperties().add(property);

        property = new CrudProperty();
        property.setName("descn");
        property.setEnabled(true);
        property.setInsertable(true);
        property.setUpdatable(true);
        property.setInSummary(true);
        configuration.getProperties().add(property);

        property = new CrudProperty();
        property.setName("name");
        property.setEnabled(true);
        property.setInsertable(true);
        property.setUpdatable(true);
        property.setInSummary(true);
        Annotation ann = new Annotation(property, Required.class.getName());
        ann.setPropertyValue("value", "true");
        property.getAnnotations().add(ann);
        configuration.getProperties().add(property);

        crudAction.setCrudConfiguration(configuration);
        return crudAction;
    }

    @Test
    public void subscription() throws InterruptedException {
        EventSource es = EventSource.target(target("/test/subscribe")).build();
        List<InboundEvent> events = new ArrayList<>();
        es.register(events::add);
        es.open();
        try (Response response = target("/test").request().post(Entity.entity(
                "{\"productid\":\"new_prod_1\", \"name\":\"New Product 1\", \"category\":\"FISH\"}",
                MediaType.APPLICATION_JSON_TYPE))) {
            assertEquals(response.getStatus(), 201);
        }
        Map result = target("/test/new_prod_1").request().get(Map.class);
        assertNotNull(result);
        assertEquals(((Map) result.get("productid")).get("value"), "new_prod_1");
        Thread.sleep(1000);
        assertEquals(1, events.size());
        assertEquals("new_prod_1", events.get(0).readData());
    }

}

@Path("/test")
class TestCrudAction extends CrudAction {
    public void commitTransaction() {
        super.commitTransaction();
        collection.getSession().beginTransaction();
    }

    @NotNull
    @Override
    protected ClassAccessor filterAccordingToPermissions(ClassAccessor classAccessor) {
        return classAccessor; //Let's ignore Shiro
    }

    @Override
    protected String getUrlEncoding() {
        return PortofinoProperties.URL_ENCODING_DEFAULT;
    }

    @Override
    public Object loadChild(String pathSegment) {
        return null;
    }
}
