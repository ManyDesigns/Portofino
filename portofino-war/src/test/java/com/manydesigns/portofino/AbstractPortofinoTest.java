/*
 * Copyright (C) 2005-2010 ManyDesigns srl.  All rights reserved.
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
package com.manydesigns.portofino;

import com.manydesigns.elements.AbstractElementsTest;
import com.manydesigns.elements.util.InstanceBuilder;
import com.manydesigns.elements.util.ReflectionUtil;
import com.manydesigns.portofino.connections.ConnectionProvider;
import com.manydesigns.portofino.context.Context;
import com.manydesigns.portofino.context.hibernate.HibernateContextImpl;
import com.manydesigns.portofino.model.Model;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.h2.tools.RunScript;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.Connection;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public abstract class AbstractPortofinoTest extends AbstractElementsTest {

    //Connessioni e context
    public Connection connPetStore;
    public Connection connPortofino;
    public Connection connDBTest;
    public Context context = null;
    public Model model;
    public String storeDir;

    public static final String PORTOFINO_CONNECTIONS_RESOURCE =
            "portofino-connections.xml";
    public static final String PORTOFINO_MODEL_RESOURCE =
            "portofino-model.xml";

    //Script per h2 3 file di configurazione
    public static final String PETSTORE_DB_SCHEMA =
        "database/jpetstore-postgres-schema.sql";
    public static final String PETSTORE_DB_DATA =
            "database/jpetstore-postgres-dataload.sql";
    public static final String PORTOFINO4_DB =
            "database/portofino4.sql";
    public static final String TEST_DB =
            "database/hibernatetest.sql";
    private static final String PORTOFINO_PROPERTIES_RESOURCE = "portofino_test.properties";

    //--------------------------------------------------------------------------
    // Setup e teardown
    //--------------------------------------------------------------------------

    @Override
    public void setUp() throws Exception {
        super.setUp();
        PortofinoProperties.loadProperties(getPortofinoPropertiesResource());

        // crea store dir se non c'è
        storeDir = FilenameUtils.normalize(portofinoProperties.getProperty(
                PortofinoProperties.PORTOFINO_STOREDIR_PROPERTY));
        File file = new File(storeDir);
        if (!file.exists() && !file.mkdirs()) {
            throw new Error("Errore in creazione di: " + storeDir);
        }

        copyResource(getPortofinoConnectionsResource(),
                PORTOFINO_CONNECTIONS_RESOURCE);
        copyResource(getPortofinoModelResource(),
                PORTOFINO_MODEL_RESOURCE);

        createContext();

        ClassLoader cl = AbstractPortofinoTest.class.getClassLoader();
        connPortofino = context.getConnectionProvider("portofino").acquireConnection();
        connPetStore = context.getConnectionProvider("jpetstore").acquireConnection();
        connDBTest = context.getConnectionProvider("hibernatetest").acquireConnection();

        RunScript.execute(connPortofino,
                new InputStreamReader(
                        cl.getResourceAsStream(PORTOFINO4_DB)));
        RunScript.execute(connPetStore,
                new InputStreamReader(
                        cl.getResourceAsStream(PETSTORE_DB_SCHEMA)));
        RunScript.execute(connPetStore,
                new InputStreamReader(
                        cl.getResourceAsStream(PETSTORE_DB_DATA)));
        RunScript.execute(connDBTest,
                new InputStreamReader(
                        cl.getResourceAsStream(TEST_DB)));

        setupAdditionalDatabases("portofino", "jpetstore", "hibernatetest");
    }

    private void setupAdditionalDatabases(String... excludes) throws Exception {
        ClassLoader cl = getClass().getClassLoader();
        for(ConnectionProvider cp : context.getConnectionProviders()) {
            if(!ArrayUtils.contains(excludes, cp.getDatabaseName())) {
                Connection conn = cp.acquireConnection();
                String schemaResource = getResource("-schema.sql", null);
                if(schemaResource != null) {
                    RunScript.execute(conn, new InputStreamReader(cl.getResourceAsStream(schemaResource)));
                }
                String dataResource = getResource("-dataload.sql", null);
                if(dataResource != null) {
                    RunScript.execute(conn, new InputStreamReader(cl.getResourceAsStream(dataResource)));
                }
            }
        }
    }

    @Override
    public void tearDown() throws Exception {
        context.stopFileManager();
        super.tearDown();
    }

    //--------------------------------------------------------------------------
    // Parametrizzazione del test
    //--------------------------------------------------------------------------

    public String getPortofinoPropertiesResource() {
        return PORTOFINO_PROPERTIES_RESOURCE;
    }

    public String getPortofinoConnectionsResource() {
        return getResource("-connections.xml", PORTOFINO_CONNECTIONS_RESOURCE);
    }

    public String getPortofinoModelResource() {
        return getResource("-model.xml", PORTOFINO_MODEL_RESOURCE);
    }

    public String getResource(String suffix, @Nullable String defaultResource) {
        String className = getClass().getName();
        String resourceName = className.replace('.', '/') + suffix;
        ClassLoader cl = getClass().getClassLoader();
        InputStream is = cl.getResourceAsStream(resourceName);
        if (is == null) {
            return defaultResource;
        } else {
            return resourceName;
        }
    }


    //--------------------------------------------------------------------------
    // utilità
    //--------------------------------------------------------------------------

    protected void copyResource(String resourceName, String fileName) throws IOException {
        InputStream is =
                ReflectionUtil.getResourceAsStream(resourceName);
        File tempFile = new File(storeDir+"/"+fileName);
        Writer writer = new FileWriter(tempFile);
        IOUtils.copy(is, writer);
        IOUtils.closeQuietly(writer);
    }



    protected void createContext() {
        Logger logger = LoggerFactory.getLogger(AbstractPortofinoTest.class);
        logger.info("Creating Context and " +
                "registering on servlet context...");
        // create and register the container first, without exceptions

        try {
            // ElementsThreadLocals è già stato impostato da AbstractElementsTest

            String managerClassName =
                    portofinoProperties.getProperty(
                            PortofinoProperties.CONTEXT_CLASS_PROPERTY);
            InstanceBuilder<Context> builder =
                    new InstanceBuilder<Context>(
                            Context.class,
                            HibernateContextImpl.class,
                            logger);
            context = builder.createInstance(managerClassName);

            String storeDir = FilenameUtils.normalize(portofinoProperties.getProperty(
                PortofinoProperties.PORTOFINO_STOREDIR_PROPERTY));
            String workDir = FilenameUtils.normalize(portofinoProperties.getProperty(
                PortofinoProperties.PORTOFINO_WORKDIR_PROPERTY));

            String connectionsFileName =
                    portofinoProperties.getProperty(
                            PortofinoProperties.CONNECTION_FILE_PROPERTY);
            String modelLocation =
                    portofinoProperties.getProperty(
                            PortofinoProperties.MODEL_LOCATION_PROPERTY);

            //String rootDirPath = ServletContext.getRealPath("/");

            File modelFile = new File(storeDir+"/"+modelLocation);

            logger.info("Storing directory:" + storeDir);
            logger.info("Working directory:" + workDir);
            context.createFileManager(storeDir, workDir);
            context.startFileManager();
            context.loadConnections(connectionsFileName);
            context.loadXmlModel(modelFile);
            model = context.getModel();


        } catch (Throwable e) {
            logger.error(ExceptionUtils.getRootCauseMessage(e), e);
        }
    }

}
