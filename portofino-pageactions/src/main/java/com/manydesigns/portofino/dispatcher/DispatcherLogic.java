/*
 * Copyright (C) 2005-2015 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.dispatcher;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.elements.util.ElementsFileUtils;
import com.manydesigns.portofino.actions.safemode.SafeModeAction;
import com.manydesigns.portofino.di.Injections;
import com.manydesigns.portofino.pageactions.PageActionLogic;
import com.manydesigns.portofino.pages.ChildPage;
import com.manydesigns.portofino.pages.Page;
import com.manydesigns.portofino.scripting.ScriptingUtil;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.*;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * A collection of methods that operate on {@link Dispatch} instances and related objects.
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class DispatcherLogic {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    public static final Logger logger = LoggerFactory.getLogger(DispatcherLogic.class);
    public static final String INVALID_PAGE_INSTANCE = "validDispatchPathLength";

    public static SelectionProvider createPagesSelectionProvider
            (File baseDir, File... excludes) {
        return createPagesSelectionProvider(baseDir, false, false, excludes);
    }

    public static SelectionProvider createPagesSelectionProvider
            (File baseDir, boolean includeRoot, boolean includeDetailChildren,
             File... excludes) {
        DefaultSelectionProvider selectionProvider = new DefaultSelectionProvider("pages");
        if (includeRoot) {
            Page rootPage;
            try {
                rootPage = getPage(baseDir);
            } catch (Exception e) {
                throw new RuntimeException("Couldn't load root page", e);
            }
            selectionProvider.appendRow("/", rootPage.getTitle() + " (top level)", true);
        }
        appendChildrenToPagesSelectionProvider
                (baseDir, baseDir, null, selectionProvider, includeDetailChildren, excludes);
        return selectionProvider;
    }

    protected static void appendChildrenToPagesSelectionProvider
            (File baseDir, File parentDir, String breadcrumb,
             DefaultSelectionProvider selectionProvider, boolean includeDetailChildren, File... excludes) {
        FileFilter filter = new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        };
        for (File dir : parentDir.listFiles(filter)) {
            try {
                appendToPagesSelectionProvider
                        (baseDir, dir, breadcrumb, selectionProvider, includeDetailChildren, excludes);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private static void appendToPagesSelectionProvider
            (File baseDir, File file, String breadcrumb,
             DefaultSelectionProvider selectionProvider, boolean includeDetailChildren, File... excludes) {
        if (ArrayUtils.contains(excludes, file)) {
            return;
        }
        if (PageInstance.DETAIL.equals(file.getName())) {
            if (includeDetailChildren) {
                breadcrumb += " (detail)"; //TODO I18n
                selectionProvider.appendRow
                        ("/" + ElementsFileUtils.getRelativePath(baseDir, file), breadcrumb, true);
                appendChildrenToPagesSelectionProvider
                        (baseDir, file, breadcrumb, selectionProvider, includeDetailChildren, excludes);
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
                    (baseDir, file, breadcrumb, selectionProvider, includeDetailChildren, excludes);
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

    /**
     * Persists a page to the file system.
     * @param pageInstance the live PageInstance containing the Page to save. 
     * @return the file where the page was saved.
     * @throws Exception in case the save fails.
     */
    public static File savePage(PageInstance pageInstance) throws Exception {
        return savePage(pageInstance.getDirectory(), pageInstance.getPage());
    }

    /**
     * Persists a page to the file system.
     * @param directory the directory where to save the page.xml file.
     * @param page the page to save.
     * @return the file where the page was saved.
     * @throws Exception in case the save fails.
     */
    public static File savePage(File directory, Page page) throws Exception {
        File pageFile = getPageFile(directory);
        Marshaller marshaller = pagesJaxbContext.createMarshaller();
        marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(page, pageFile);
        pageCache.invalidate(pageFile);
        return pageFile;
    }

    //Cache configuration properties
    public static final String PAGE_CACHE_SIZE = "page.cache.size";
    public static final String PAGE_CACHE_CHECK_FREQUENCY = "page.cache.check.frequency";
    public static final String CONFIGURATION_CACHE_SIZE = "configuration.cache.size";
    public static final String CONFIGURATION_CACHE_CHECK_FREQUENCY = "configuration.cache.check.frequency";

    public static void init(Configuration portofinoConfiguration) {
        int maxSize, refreshCheckFrequency;
        maxSize = portofinoConfiguration.getInt(PAGE_CACHE_SIZE, 1000);
        refreshCheckFrequency =
                portofinoConfiguration.getInt(PAGE_CACHE_CHECK_FREQUENCY, 5);
        initPageCache(maxSize, refreshCheckFrequency);
        maxSize = portofinoConfiguration.getInt(CONFIGURATION_CACHE_SIZE, 1000);
        refreshCheckFrequency =
                portofinoConfiguration.getInt(CONFIGURATION_CACHE_CHECK_FREQUENCY, 5);
        initConfigurationCache(maxSize, refreshCheckFrequency);
    }

    protected static class FileCacheEntry<T> {
        public final T object;
        public final long lastModified;
        public final boolean error;

        public FileCacheEntry(T object, long lastModified, boolean error) {
            this.object = object;
            this.lastModified = lastModified;
            this.error = error;
        }
    }

    protected static class ConfigurationCacheEntry extends FileCacheEntry<Object> {
        public final Class<?> configurationClass;

        public ConfigurationCacheEntry(
                Object configuration, Class<?> configurationClass, long lastModified,
                boolean error) {
            super(configuration, lastModified, error);
            this.configurationClass = configurationClass;
        }
    }

    //NB il reload delle cache è _asincrono_ rispetto alla get, è quindi possibile che una get ritorni
    //un valore vecchio anche nel caso in cui sia appena stato rilevato un errore nel reload (es. ho scritto
    //caratteri invalidi all'inizio dell'xml).

    protected static LoadingCache<File, FileCacheEntry<Page>> pageCache;

    public static void initPageCache(int maxSize, int refreshCheckFrequency) {
        pageCache =
                CacheBuilder.newBuilder()
                        .maximumSize(maxSize)
                        .refreshAfterWrite(refreshCheckFrequency, TimeUnit.SECONDS)
                        .build(new CacheLoader<File, FileCacheEntry<Page>>() {

                            @Override
                            public FileCacheEntry<Page> load(File key) throws Exception {
                                return new FileCacheEntry<Page>(loadPage(key), key.lastModified(), false);
                            }

                            @Override
                            public ListenableFuture<FileCacheEntry<Page>> reload(
                                    final File key, FileCacheEntry<Page> oldValue)
                                    throws Exception {
                                if(!key.exists()) {
                                    //Se la pagina non esiste più, registro questo fatto nella cache;
                                    //a questo livello non è un errore, sarà il metodo getPage() a gestire
                                    //la entry problematica.
                                    return Futures.immediateFuture(
                                            new FileCacheEntry<Page>(null, 0, true));
                                } else if (key.lastModified() > oldValue.lastModified) {
                                    /*return ListenableFutureTask.create(new Callable<PageCacheEntry>() {
                                        public PageCacheEntry call() throws Exception {
                                            return doLoad(key);
                                        }
                                    });*/
                                    //TODO async?
                                    try {
                                        Page page = loadPage(key);
                                        return Futures.immediateFuture(
                                                new FileCacheEntry<Page>(page, key.lastModified(), false));
                                    } catch (Throwable t) {
                                        logger.error(
                                                "Could not reload cached page from " + key.getAbsolutePath() +
                                                ", removing from cache", t);
                                        return Futures.immediateFuture(
                                                new FileCacheEntry<Page>(null, key.lastModified(), true));
                                    }
                                } else {
                                    return Futures.immediateFuture(oldValue);
                                }
                            }

                        });
    }

    protected static LoadingCache<File, ConfigurationCacheEntry> configurationCache;

    public static void initConfigurationCache(int maxSize, int refreshCheckFrequency) {
        configurationCache =
                CacheBuilder.newBuilder()
                        .maximumSize(maxSize)
                        .refreshAfterWrite(refreshCheckFrequency, TimeUnit.SECONDS)
                        .build(new CacheLoader<File, ConfigurationCacheEntry>() {

                            @Override
                            public ConfigurationCacheEntry load(File key) throws Exception {
                                throw new UnsupportedOperationException();
                            }

                            @Override
                            public ListenableFuture<ConfigurationCacheEntry> reload(
                                    final File key, ConfigurationCacheEntry oldValue)
                                    throws Exception {
                                if(!key.exists()) {
                                    //Se la conf. non esiste più, la marco come errata;
                                    //il metodo getConfiguration provvederà a rimuoverla (contrariamente
                                    //alla page, non è possibile lasciare l'oggetto in stato errato nella
                                    //cache perché in generale potrebbero mancare le informazioni per ricaricarlo
                                    //correttamente... TODO da verificare meglio!!!)
                                    return Futures.immediateFuture(
                                            new ConfigurationCacheEntry(null, null, 0, true));
                                } else if (key.lastModified() > oldValue.lastModified) {
                                    //TODO se oldValue.error non dovrei ricaricare (informazioni incomplete) - ?
                                    //TODO async?
                                    try {
                                        Object newConf = loadConfiguration(
                                                key, oldValue.configurationClass);
                                        return Futures.immediateFuture(
                                                new ConfigurationCacheEntry(
                                                        newConf, newConf.getClass(), key.lastModified(),
                                                        false));
                                    } catch (Throwable t) {
                                        logger.error(
                                                "Could not reload cached configuration from " + key.getAbsolutePath() +
                                                ", removing from cache", t);
                                        return Futures.immediateFuture(
                                            new ConfigurationCacheEntry(null, null, 0, true));
                                    }
                                } else {
                                    return Futures.immediateFuture(oldValue);
                                }
                            }

                        });
    }

    public static void clearConfigurationCache() {
        configurationCache.invalidateAll();
    }

    /**
     * Clears the cache from entries whose class matches exactly with the one passed as a parameter.
     * @param configurationClass the class of the entries to remove.
     */
    public static void clearConfigurationCache(Class configurationClass) {
        Set<Map.Entry<File,ConfigurationCacheEntry>> entries = configurationCache.asMap().entrySet();
        List<File> keysToInvalidate = new ArrayList<File>();
        for(Map.Entry<File,ConfigurationCacheEntry> entry : entries) {
            if(entry.getValue().configurationClass == configurationClass) {
                keysToInvalidate.add(entry.getKey());
            }
        }
        configurationCache.invalidateAll(keysToInvalidate);
    }

    protected static File getPageFile(File directory) {
        return new File(directory, "page.xml");
    }

    public static Page loadPage(File key) throws Exception {
        FileInputStream fileInputStream = new FileInputStream(key);
        try {
            Page page = loadPage(fileInputStream);
            page.init();
            return page;
        } finally {
            IOUtils.closeQuietly(fileInputStream);
        }
    }

    public static Page loadPage(InputStream inputStream) throws JAXBException {
        Unmarshaller unmarshaller = pagesJaxbContext.createUnmarshaller();
        return (Page) unmarshaller.unmarshal(inputStream);
    }

    public static Page getPage(File directory) throws PageNotActiveException {
        File pageFile = getPageFile(directory);
        try {
            FileCacheEntry<Page> entry = pageCache.get(pageFile);
            if(!entry.error) {
                return entry.object;
            } else {
                throw new PageNotActiveException(pageFile.getAbsolutePath());
            }
        } catch (ExecutionException e) {
            throw new PageNotActiveException(pageFile.getAbsolutePath(), e);
        }
    }

    public static File saveConfiguration(File directory, Object configuration) throws Exception {
        String configurationPackage = configuration.getClass().getPackage().getName();
        JAXBContext jaxbContext = JAXBContext.newInstance(configurationPackage);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        File configurationFile = new File(directory, "configuration.xml");
        marshaller.marshal(configuration, configurationFile);
        configurationCache.invalidate(configurationFile);
        return configurationFile;
    }

    public static <T> T getConfiguration(File configurationFile, Class<? extends T> configurationClass)
            throws Exception {
        if (configurationClass == null) {
            return null;
        }
        ConfigurationCacheEntry entry = configurationCache.getIfPresent(configurationFile);
        if(entry == null || !configurationClass.isInstance(entry.object) || entry.error) {
            if(entry != null && entry.error) {
                logger.warn("Cached configuration for {} is in error state, forcing a reload",
                            configurationFile.getAbsolutePath());
            } else if(entry != null && !configurationClass.isInstance(entry.object)) {
                logger.warn("Cached configuration for {} is an instance of the wrong class, forcing a reload",
                            configurationFile.getAbsolutePath());
            }
            T configuration = loadConfiguration(configurationFile, configurationClass);
            entry = new ConfigurationCacheEntry(
                    configuration, configurationClass, configurationFile.lastModified(), false);
            configurationCache.put(configurationFile, entry);
        }
        return (T) entry.object;
    }

    public static <T> T loadConfiguration(
            File configurationFile, Class<? extends T> configurationClass) throws Exception {
        if (configurationClass == null) {
            return null;
        }
        InputStream inputStream = new FileInputStream(configurationFile);
        try {
            return loadConfiguration(inputStream, configurationClass);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    public static <T> T loadConfiguration
            (InputStream inputStream, Class<? extends T> configurationClass)
            throws Exception {
        if (configurationClass == null) {
            return null;
        }
        Object configuration;
        String configurationPackage = configurationClass.getPackage().getName();
        JAXBContext jaxbContext = JAXBContext.newInstance(configurationPackage);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        configuration = unmarshaller.unmarshal(new StreamSource(inputStream), configurationClass);
        if(configuration instanceof JAXBElement) {
            configuration = ((JAXBElement) configuration).getValue();
        }
        if (!configurationClass.isInstance(configuration)) {
            logger.error("Invalid configuration: expected " + configurationClass + ", got " + configuration);
            return null;
        }
        Injections.inject(
                configuration, ElementsThreadLocals.getServletContext(), ElementsThreadLocals.getHttpServletRequest());
        if(configuration instanceof PageActionConfiguration) {
            ((PageActionConfiguration) configuration).init();
        }
        return (T) configuration;
    }

    public static Class<? extends PageAction> getActionClass(Configuration configuration, File directory) {
        return getActionClass(configuration, directory, true);
    }

    public static Class<? extends PageAction> getActionClass
            (Configuration configuration, File directory, boolean fallback) {
        File scriptFile = ScriptingUtil.getGroovyScriptFile(directory, "action");
        Class<? extends PageAction> actionClass;
        try {
            actionClass = (Class<? extends PageAction>) ScriptingUtil.getGroovyClass(scriptFile);
        } catch (Exception e) {
            logger.error("Couldn't load action class for " + directory + ", returning safe-mode action", e);
            return fallback ? SafeModeAction.class : null;
        }
        if (isValidActionClass(actionClass)) {
            return actionClass;
        } else {
            logger.error("Invalid action class for " + directory + ": " + actionClass);
            return fallback ? SafeModeAction.class : null;
        }
    }

    public static boolean isValidActionClass(Class<?> actionClass) {
        if (actionClass == null) {
            return false;
        }
        if (!PageAction.class.isAssignableFrom(actionClass)) {
            logger.error("Action " + actionClass + " must implement " + PageAction.class);
            return false;
        }
        return true;
    }

    @Deprecated
    public static PageAction ensureActionBean(PageInstance page) throws IllegalAccessException, InstantiationException {
        PageAction action = page.getActionBean();
        assert action != null;
        if(action == null) {
            action = page.getActionClass().newInstance();
            page.setActionBean(action);
        }
        return action;
    }

    public static void configurePageAction(PageAction pageAction)
            throws JAXBException, IOException {
        PageInstance pageInstance = pageAction.getPageInstance();
        if(pageInstance.getConfiguration() != null) {
            logger.debug("Page instance {} is already configured");
            return;
        }
        File configurationFile = new File(pageInstance.getDirectory(), "configuration.xml");
        Class<?> configurationClass = PageActionLogic.getConfigurationClass(pageAction.getClass());
        try {
            Object configuration =
                    getConfiguration(configurationFile, configurationClass);
            pageInstance.setConfiguration(configuration);
        } catch (Throwable t) {
            logger.error("Couldn't load configuration from " + configurationFile.getAbsolutePath(), t);
        }
    }

    public static PageAction getSubpage(
            Configuration configuration, PageInstance parentPageInstance, String pathFragment)
            throws PageNotActiveException {
        File currentDirectory = parentPageInstance.getChildrenDirectory();
        File childDirectory = new File(currentDirectory, pathFragment);
        if(childDirectory.isDirectory() && !PageInstance.DETAIL.equals(childDirectory.getName())) {
            ChildPage childPage = null;
            for(ChildPage candidate : parentPageInstance.getLayout().getChildPages()) {
                if(candidate.getName().equals(childDirectory.getName())) {
                    childPage = candidate;
                    break;
                }
            }
            if(childPage == null) {
                throw new PageNotActiveException(childDirectory.getAbsolutePath());
            }

            Page page = DispatcherLogic.getPage(childDirectory);
            Class<? extends PageAction> actionClass =
                    DispatcherLogic.getActionClass(configuration, childDirectory);
            try {
                PageAction pageAction = actionClass.newInstance();
                PageInstance pageInstance =
                    new PageInstance(parentPageInstance, childDirectory, page, actionClass);
                pageInstance.setActionBean(pageAction);
                pageAction.setPageInstance(pageInstance);
                configurePageAction(pageAction);
                return pageAction;
            } catch (Exception e) {
                throw new PageNotActiveException(e);
            }
        } else {
            return null;
        }
    }

}
