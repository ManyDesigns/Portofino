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

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.annotations.FileBlob;
import com.manydesigns.elements.annotations.Required;
import com.manydesigns.elements.blobs.Blob;
import com.manydesigns.elements.blobs.HierarchicalBlobManager;
import com.manydesigns.elements.fields.AbstractBlobField;
import com.manydesigns.elements.fields.Field;
import com.manydesigns.elements.fields.FileBlobField;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.servlet.MutableHttpServletRequest;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.actions.ActionDescriptor;
import com.manydesigns.portofino.database.platforms.H2DatabasePlatform;
import com.manydesigns.portofino.model.Annotation;
import com.manydesigns.portofino.model.Property;
import com.manydesigns.portofino.model.database.Column;
import com.manydesigns.portofino.model.database.DatabaseLogic;
import com.manydesigns.portofino.model.database.IncrementGenerator;
import com.manydesigns.portofino.model.database.Table;
import com.manydesigns.portofino.model.database.platforms.DatabasePlatformsRegistry;
import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.resourceactions.ActionContext;
import com.manydesigns.portofino.resourceactions.ActionInstance;
import com.manydesigns.portofino.resourceactions.crud.configuration.CrudProperty;
import com.manydesigns.portofino.resourceactions.crud.configuration.database.CrudConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.h2.tools.RunScript;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.*;

import javax.ws.rs.core.Application;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

import static org.testng.Assert.*;

@SuppressWarnings({"JpaQlInspection"})
@Test
public class CrudActionTest extends JerseyTest {

    Persistence persistence;

    @BeforeClass
    public void setupElements() {
        ElementsThreadLocals.setupDefaultElementsContext();
    }

    @AfterClass
    public void teardownElements() {
        ElementsThreadLocals.destroy();
    }

    @BeforeMethod
    public void setup() throws Exception {
        FileObject appDir = VFS.getManager().resolveFile("res:com/manydesigns/portofino/resourceactions/crud/model");
        setup(appDir);
    }

    protected void setup(FileObject appDir) throws Exception {
        Configuration configuration = new PropertiesConfiguration();
        DatabasePlatformsRegistry databasePlatformsRegistry = new DatabasePlatformsRegistry(configuration);
        databasePlatformsRegistry.addDatabasePlatform(new H2DatabasePlatform());
        persistence = new Persistence(appDir, configuration, null, databasePlatformsRegistry, null);
        persistence.start();
        setupJPetStore();
        persistence.initModel();
    }

    @AfterMethod
    public void teardown() {
        persistence.stop();
    }

    @Override
    protected Application configure() {
        return new ResourceConfig();
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

    public void testBlobs() throws Exception {
        MutableHttpServletRequest req = new MutableHttpServletRequest();
        ElementsThreadLocals.setMultipart(req);
        req.getServletContext().setInitParameter("portofino.api.root", "http://fake");
        req.makeMultipart();

        Column column = DatabaseLogic.findColumnByName(persistence.getModel(), "jpetstore", "PUBLIC", "PRODUCT", "DESCN");
        Annotation ann = new Annotation(column, FileBlob.class.getName());
        column.getAnnotations().add(ann);

        persistence.initModel();
        CrudAction crudAction = new CrudAction() {
            public void commitTransaction() {
                super.commitTransaction();
                session.beginTransaction();
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
        };
        CrudConfiguration configuration = new CrudConfiguration();
        configuration.setDatabase("jpetstore");
        configuration.setQuery("from product");
        String metaFilenamePattern = "blob-{0}.properties";
        String dataFilenamePattern = "blob-{0}.data";
        crudAction.blobManager = new HierarchicalBlobManager(new File(System.getProperty("java.io.tmpdir")), metaFilenamePattern, dataFilenamePattern);

        CrudProperty property = new CrudProperty();
        property.setName("productid");
        property.setEnabled(true);
        property.setInsertable(true);
        property.setUpdatable(true);
        configuration.getProperties().add(property);

        property = new CrudProperty();
        property.setName("category");
        property.setEnabled(true);
        property.setInsertable(true);
        property.setUpdatable(true);
        configuration.getProperties().add(property);

        property = new CrudProperty();
        property.setName("descn");
        property.setEnabled(true);
        property.setInsertable(true);
        property.setUpdatable(true);
        configuration.getProperties().add(property);

        property = new CrudProperty();
        property.setName("name");
        property.setEnabled(true);
        property.setInsertable(true);
        property.setUpdatable(true);
        ann = new Annotation(column, Required.class.getName());
        ann.getProperties().add(new Property("value", "true"));
        property.getAnnotations().add(ann);
        configuration.getProperties().add(property);

        configuration.persistence = persistence;
        configuration.init();

        ActionInstance actionInstance = new ActionInstance(null, null, new ActionDescriptor(), CrudAction.class);
        actionInstance.setConfiguration(configuration);
        actionInstance.getParameters().add("1");
        ActionContext actionContext = new ActionContext();
        actionContext.setRequest(req);
        actionContext.setActionPath("");
        actionContext.setServletContext(req.getServletContext());

        req.setParameter("productid", "1");
        Map category = (Map) persistence.getSession("jpetstore").createQuery("from category").list().get(0);
        req.setParameter("category", (String) category.get("catid"));
        crudAction.persistence = persistence;
        crudAction.setContext(actionContext);
        crudAction.setActionInstance(actionInstance);
        crudAction.init();
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
                createSQLQuery("update product set descn = 'illegal' where productid = :id").
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
        crudAction.jsonReadData();
        blobField = (FileBlobField) crudAction.form.findFieldByPropertyName("descn");
        assertNotNull(blobField.getValue());
        assertNotNull(blobField.getBlobError());
        assertNull(blobField.getValue().getFilename());

        qres = session.
                createSQLQuery("update product set descn = :blobCode where productid = :id").
                setParameter("id", id).
                setParameter("blobCode", newBlobCode).
                executeUpdate();
        assertEquals(1, qres);
        session.flush();
        session.getTransaction().commit();
        session.clear();
        session.beginTransaction();
        crudAction.parametersAcquired(); //Force reload
        crudAction.httpDelete(Collections.emptyList());
        try {
            crudAction.blobManager.loadMetadata(new Blob(newBlobCode));
            fail("The blob " + newBlobCode + " should have been deleted");
        } catch (IOException e) {
            //Ok
        }
    }

}
