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

package com.manydesigns.portofino.dispatcher;

import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.elements.util.ElementsFileUtils;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.pageactions.safemode.SafeModeAction;
import com.manydesigns.portofino.pages.Page;
import com.manydesigns.portofino.scripting.ScriptingUtil;
import groovy.lang.GroovyClassLoader;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class DispatcherLogic {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    private static final Logger logger = LoggerFactory.getLogger(DispatcherLogic.class);

    public static SelectionProvider createPagesSelectionProvider
            (Application application, File baseDir, File... excludes) {
        return createPagesSelectionProvider(application, baseDir, false, false, excludes);
    }

    public static SelectionProvider createPagesSelectionProvider
            (Application application, File baseDir, boolean includeRoot, boolean includeDetailChildren,
             File... excludes) {
        DefaultSelectionProvider selectionProvider = new DefaultSelectionProvider("pages");
        if(includeRoot) {
            Page rootPage;
            try {
                rootPage = getPage(baseDir);
            } catch (Exception e) {
                throw new RuntimeException("Couldn't load root page", e);
            }
            selectionProvider.appendRow("/", rootPage.getTitle() + " (top level)", true);
        }
        appendChildrenToPagesSelectionProvider
                (application, baseDir, baseDir, null, selectionProvider, includeDetailChildren, excludes);
        return selectionProvider;
    }

    protected static void appendChildrenToPagesSelectionProvider
            (Application application, File baseDir, File parentDir, String breadcrumb,
             DefaultSelectionProvider selectionProvider, boolean includeDetailChildren, File... excludes) {
        FileFilter filter = new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        };
        for (File dir : parentDir.listFiles(filter)) {
            appendToPagesSelectionProvider
                    (application, baseDir, dir, breadcrumb, selectionProvider, includeDetailChildren, excludes);
        }
    }

    private static void appendToPagesSelectionProvider
            (Application application, File baseDir, File file, String breadcrumb,
             DefaultSelectionProvider selectionProvider, boolean includeDetailChildren, File... excludes) {
        if(ArrayUtils.contains(excludes, file)) {
            return;
        }
        if(PageInstance.DETAIL.equals(file.getName())) {
            if(includeDetailChildren) {
                breadcrumb += " (detail)"; //TODO I18n
                selectionProvider.appendRow
                    ("/" + ElementsFileUtils.getRelativePath(baseDir, file), breadcrumb, true);
                appendChildrenToPagesSelectionProvider
                        (application, baseDir, file, breadcrumb, selectionProvider, includeDetailChildren, excludes);
            }
        } else {
            Page page;
            try {
                page = getPage(file);
            } catch (Exception e) {
                throw new RuntimeException("Couldn't load page", e);
            }
            if (breadcrumb == null) {
                breadcrumb = page.getTitle();
            } else {
                breadcrumb = String.format("%s > %s", breadcrumb, page.getTitle());
            }
            selectionProvider.appendRow
                    ("/" + ElementsFileUtils.getRelativePath(baseDir, file), breadcrumb, true);
            appendChildrenToPagesSelectionProvider
                    (application, baseDir, file, breadcrumb, selectionProvider, includeDetailChildren, excludes);
        }
    }

    protected static final JAXBContext pagesJaxbContext;

    static {
        try {
            pagesJaxbContext = JAXBContext.newInstance(Page.class.getPackage().getName());
        } catch (JAXBException e) {
            throw new Error("Can't instantiate pages jaxb context", e);
        }
    }

    public static File savePage(PageInstance pageInstance) throws Exception {
        return savePage(pageInstance.getDirectory(), pageInstance.getPage());
    }

    public static File savePage(File directory, Page page) throws Exception {
        File pageFile = new File(directory, "page.xml");
        Marshaller marshaller = pagesJaxbContext.createMarshaller();
        marshaller.marshal(page, pageFile);
        return pageFile;
    }

    public static Page loadPage(File directory) throws FileNotFoundException, JAXBException {
        File pageFile = new File(directory, "page.xml");
        FileReader reader = new FileReader(pageFile);
        try {
            return loadPage(reader);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    public static Page loadPage(Reader reader) throws JAXBException {
        Unmarshaller unmarshaller = pagesJaxbContext.createUnmarshaller();
        return (Page) unmarshaller.unmarshal(reader);
    }

    public static Page getPage(File directory) throws Exception {
        Page page = loadPage(directory);
        page.init();
        return page;
    }

    public static File saveConfiguration(File directory, Object configuration) throws Exception {
        String configurationPackage = configuration.getClass().getPackage().getName();
        JAXBContext jaxbContext = JAXBContext.newInstance(configurationPackage);
        Marshaller marshaller = jaxbContext.createMarshaller();
        File configurationFile = new File(directory, "configuration.xml");
        marshaller.marshal(configuration, configurationFile);
        return configurationFile;
    }

    public static <T> T loadConfiguration(File directory, Class<? extends T> configurationClass) throws Exception {
        if(configurationClass == null) {
            return null;
        }
        File configurationFile = new File(directory, "configuration.xml");
        FileReader reader = new FileReader(configurationFile);
        try {
            return loadConfiguration(reader, configurationClass);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    public static <T> T loadConfiguration(Reader reader, Class<? extends T> configurationClass) throws Exception {
        if(configurationClass == null) {
            return null;
        }
        Object configuration;
        String configurationPackage = configurationClass.getPackage().getName();
        JAXBContext jaxbContext = JAXBContext.newInstance(configurationPackage);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        configuration = unmarshaller.unmarshal(reader);
        if(!configurationClass.isInstance(configuration)) {
            logger.error("Invalid configuration: expected " + configurationClass + ", got " + configuration);
            return null;
        }
        return (T) configuration;
    }

    //TODO!!!
    private static final ConcurrentMap<File, Class<? extends PageAction>> actionClassCache =
            new ConcurrentHashMap<File, Class<? extends PageAction>>();

    public static Class<? extends PageAction> getActionClass(File directory) {
        Class<? extends PageAction> actionClass = actionClassCache.get(directory);
        if(actionClass != null) {
            return actionClass;
        } else {
            try {
                actionClass = (Class<? extends PageAction>) ScriptingUtil.getGroovyClass(directory, "action");
            } catch (Exception e) {
                logger.error("Couldn't load action class for " + directory + ", returning safe-mode action", e);
                return SafeModeAction.class;
            }
            if(isValidActionClass(actionClass)) {
                actionClassCache.put(directory, actionClass);
                return actionClass;
            } else {
                logger.error("Invalid action class for " + directory + ": " + actionClass);
                return SafeModeAction.class;
            }
        }
    }

    public static Class<? extends PageAction> setActionClass(File directory, String source) throws IOException {
        File groovyScriptFile =
                ScriptingUtil.getGroovyScriptFile(directory, "action");
        GroovyClassLoader loader = new GroovyClassLoader();
        Class<? extends PageAction> scriptClass =
                loader.parseClass(source, groovyScriptFile.getAbsolutePath());
        if(!isValidActionClass(scriptClass)) {
            return null;
        }
        FileWriter fw = new FileWriter(groovyScriptFile);
        try {
            fw.write(source);
            actionClassCache.put(directory, scriptClass);
        } finally {
            IOUtils.closeQuietly(fw);
        }
        return scriptClass;
    }

    public static boolean isValidActionClass(Class<?> actionClass) {
        if(actionClass == null) {
            return false;
        }
        if(!PageAction.class.isAssignableFrom(actionClass)) {
            logger.error("Action " + actionClass + " must implement " + PageAction.class);
            return false;
        }
        return true;
    }
}
