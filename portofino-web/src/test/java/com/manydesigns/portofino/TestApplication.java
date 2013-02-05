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

package com.manydesigns.portofino;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.text.OgnlTextFormat;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.database.platforms.DatabasePlatformsManager;
import com.manydesigns.portofino.dispatcher.*;
import com.manydesigns.portofino.i18n.ResourceBundleManager;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.database.ConnectionProvider;
import com.manydesigns.portofino.pageactions.PageActionLogic;
import com.manydesigns.portofino.pageactions.safemode.SafeModeAction;
import com.manydesigns.portofino.pages.ChildPage;
import com.manydesigns.portofino.pages.Layout;
import com.manydesigns.portofino.pages.Page;
import com.manydesigns.portofino.reflection.TableAccessor;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;
import org.hibernate.Session;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class TestApplication implements Application {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    protected final String appId;
    protected final File rootDir;
    protected final File pagesDir;

    protected final BaseConfiguration portofinoProperties;

    protected final List<File> resourcesToDelete = new ArrayList<File>();

    public TestApplication(String appId) throws Exception {
        this.appId = appId;
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        if(tmpDir.isDirectory()) {
            rootDir = new File(tmpDir, "portofino-test-app-" + appId);
            ensureDirectoryExists(rootDir);
            rememberToDelete(rootDir);

            pagesDir = new File(rootDir, "pages");
            ensureDirectoryExists(pagesDir);
            rememberToDelete(pagesDir);

            Page rootPage = new Page();
            File rootPageFile = DispatcherLogic.savePage(pagesDir, rootPage);
            rememberToDelete(rootPageFile);
        } else {
            throw new Error("Not a directory: " + tmpDir.getAbsolutePath());
        }

        portofinoProperties = new BaseConfiguration();
        portofinoProperties.addProperty(PortofinoProperties.FALLBACK_ACTION_CLASS, SafeModeAction.class.getName());
    }

    private void rememberToDelete(File rootPageFile) {
        resourcesToDelete.add(0, rootPageFile);
    }

    private void ensureDirectoryExists(File dir) {
        if(!dir.mkdir() && !dir.isDirectory()) {
            throw new Error("Not a directory: " + dir.getAbsolutePath());
        }
    }

    public File addPage(String path, String name, Class<? extends PageAction> actionClass) throws Exception {
        String scriptTemplate = PageActionLogic.getScriptTemplate(actionClass);
        if(scriptTemplate == null) {
            throw new IllegalArgumentException(actionClass + " doesn't have a script template");
        }

        if(ElementsThreadLocals.getOgnlContext() == null) {
            ElementsThreadLocals.setupDefaultElementsContext();
        }

        String script = OgnlTextFormat.format(scriptTemplate, this);

        Page rootPage;
        File rootDir;
        File childrenDir;
        Layout rootLayout;
        if(path == null || path.isEmpty() || path.equals("/")) {
            rootDir = childrenDir = pagesDir;
            rootPage = DispatcherLogic.getPage(rootDir);
            rootLayout = rootPage.getLayout();
        } else {
            Dispatcher dispatcher = new Dispatcher(this);
            Dispatch dispatch = dispatcher.getDispatch("", path);
            if(dispatch == null) {
                throw new Error("Invalid path: " + path);
            }
            PageInstance pageInstance = dispatch.getLastPageInstance();
            rootPage = pageInstance.getPage();
            rootLayout = pageInstance.getLayout();
            rootDir = pageInstance.getDirectory();
            childrenDir = pageInstance.getChildrenDirectory();
            ensureDirectoryExists(childrenDir);
            rememberToDelete(childrenDir);
        }
        File child = new File(childrenDir, name);
        ensureDirectoryExists(child);
        rememberToDelete(child);

        Page page = new Page();
        File pageFile = DispatcherLogic.savePage(child, page);
        rememberToDelete(pageFile);

        File scriptFile = new File(child, "action.groovy");
        FileWriter fileWriter = new FileWriter(scriptFile);
        IOUtils.write(script, fileWriter);
        IOUtils.closeQuietly(fileWriter);
        rememberToDelete(scriptFile);

        ChildPage childPage = new ChildPage();
        childPage.setName(name);
        rootLayout.getChildPages().add(childPage);
        DispatcherLogic.savePage(rootDir, rootPage);

        return pageFile;
    }

    public String getAppId() {
        return appId;
    }

    public String getName() {
        return appId;
    }

    public File getAppDir() {
        return rootDir;
    }

    public File getAppBlobsDir() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public File getPagesDir() {
        return pagesDir;
    }

    public File getAppDbsDir() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public File getAppModelFile() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public File getAppScriptsDir() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public File getAppStorageDir() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public File getAppWebDir() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void loadXmlModel() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void saveXmlModel() throws IOException, JAXBException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public ConnectionProvider getConnectionProvider(String databaseName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public BaseConfiguration getPortofinoProperties() {
        return portofinoProperties;
    }

    public DatabasePlatformsManager getDatabasePlatformsManager() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Model getModel() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void syncDataModel(String databaseName) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Session getSession(String databaseName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void closeSession(String databaseName) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void closeSessions() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public TableAccessor getTableAccessor(String database, String entityName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ResourceBundle getBundle(Locale locale) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ResourceBundleManager getResourceBundleManager() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Configuration getAppConfiguration() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void initModel() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void shutdown() {
        for(File f : resourcesToDelete) {
            f.delete();
        }
    }
}
