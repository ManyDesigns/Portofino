/*
 * Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.actions;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.portofino.resourceactions.*;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.xml.bind.*;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * A collection of methods that operate on pages and related objects.
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ActionLogic {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    public static final Logger logger = LoggerFactory.getLogger(ActionLogic.class);

    protected static final JAXBContext JAXB_CONTEXT;

    static {
        try {
            JAXB_CONTEXT = JAXBContext.newInstance(ActionDescriptor.class.getPackage().getName());
        } catch (JAXBException e) {
            throw new Error("Can't instantiate pages jaxb context", e);
        }
    }

    /**
     * Persists an actionDescriptor to the file system.
     * @param actionInstance the live ActionInstance containing the ActionDescriptor to save.
     * @return the file where the actionDescriptor was saved.
     * @throws Exception in case the save fails.
     */
    public static FileObject saveActionDescriptor(ActionInstance actionInstance) throws Exception {
        return saveActionDescriptor(actionInstance.getDirectory(), actionInstance.getActionDescriptor());
    }

    /**
     * Persists an actionDescriptor to the file system.
     * @param directory the directory where to save the action.xml file.
     * @param actionDescriptor the actionDescriptor to save.
     * @return the file where the actionDescriptor was saved.
     * @throws Exception in case the save fails.
     */
    public static FileObject saveActionDescriptor(FileObject directory, ActionDescriptor actionDescriptor) throws Exception {
        FileObject file = getActionDescriptorFile(directory);
        Marshaller marshaller = JAXB_CONTEXT.createMarshaller();
        marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        if(!file.exists()) {
            file.createFile();
        }
        try(OutputStream outputStream = file.getContent().getOutputStream()) {
            marshaller.marshal(actionDescriptor, outputStream);
            actionDescriptorCache.invalidate(file);
        }
        return file;
    }

    //Cache configuration properties
    public static final String ACTION_DESCRIPTOR_CACHE_SIZE = "actionDescriptor.cache.size";
    public static final String ACTION_DESCRIPTOR_CACHE_CHECK_FREQUENCY = "actionDescriptor.cache.check.frequency";

    public static void init(Configuration portofinoConfiguration) {
        int maxSize, refreshCheckFrequency;
        maxSize = portofinoConfiguration.getInt(ACTION_DESCRIPTOR_CACHE_SIZE, 1000);
        refreshCheckFrequency =
                portofinoConfiguration.getInt(ACTION_DESCRIPTOR_CACHE_CHECK_FREQUENCY, 5);
        initActionDescriptorCache(maxSize, refreshCheckFrequency);
    }

    public static void mount(FileObject actionDirectory, String segment, String path) throws Exception {
        ActionDescriptor descriptor = getActionDescriptor(actionDirectory);
        Optional<AdditionalChild> existing =
                descriptor.getAdditionalChildren().stream().filter(c -> c.getSegment().equals(segment)).findFirst();
        if(existing.isPresent()) {
            String existingPath = existing.get().getPath();
            if(!path.equals(existingPath)) {
                throw new IllegalArgumentException("Another path is already mounted at " + segment + ": " + existingPath);
            }
        } else {
            AdditionalChild child = new AdditionalChild();
            child.setSegment(segment);
            child.setPath(path);
            descriptor.getAdditionalChildren().add(child);
            saveActionDescriptor(actionDirectory, descriptor);
        }
    }

    public static void mount(FileObject actionDirectory, String segment, Class<?> actionClass) throws Exception {
        mount(actionDirectory, segment, "res:" + actionClass.getName().replace('.', '/') + ".class");
    }

    public static void mountPackage(FileObject actionDirectory, String segment, String packageName) throws Exception {
        mount(actionDirectory, segment, "res:" + packageName.replace('.', '/'));
    }

    public static void mountPackage(FileObject actionDirectory, String segment, Package pkg) throws Exception {
        mountPackage(actionDirectory, segment, pkg.getName());
    }

    public static void unmount(FileObject actionDirectory, String segment) throws Exception {
        ActionDescriptor descriptor = getActionDescriptor(actionDirectory);
        Optional<AdditionalChild> existing =
                descriptor.getAdditionalChildren().stream().filter(c -> c.getSegment().equals(segment)).findFirst();
        if(existing.isPresent()) {
            descriptor.getAdditionalChildren().remove(existing.get());
            saveActionDescriptor(actionDirectory, descriptor);
        }
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

    protected static LoadingCache<FileObject, FileCacheEntry<ActionDescriptor>> actionDescriptorCache;

    public static void initActionDescriptorCache(int maxSize, int refreshCheckFrequency) {
        actionDescriptorCache =
                CacheBuilder.newBuilder()
                        .maximumSize(maxSize)
                        .refreshAfterWrite(refreshCheckFrequency, TimeUnit.SECONDS)
                        .build(new CacheLoader<FileObject, FileCacheEntry<ActionDescriptor>>() {

                            @Override
                            public FileCacheEntry<ActionDescriptor> load(@NotNull FileObject key) throws Exception {
                                return new FileCacheEntry<>(loadActionDescriptor(key), key.getContent().getLastModifiedTime(), false);
                            }

                            @Override
                            public ListenableFuture<FileCacheEntry<ActionDescriptor>> reload(
                                    @NotNull final FileObject key, FileCacheEntry<ActionDescriptor> oldValue)
                                    throws Exception {
                                if(!key.exists()) {
                                    //Se la pagina non esiste più, registro questo fatto nella cache;
                                    //a questo livello non è un errore, sarà il metodo getActionDescriptor() a gestire
                                    //la entry problematica.
                                    return Futures.immediateFuture(
                                            new FileCacheEntry<>(null, 0, true));
                                } else if (key.getContent().getLastModifiedTime() > oldValue.lastModified) {
                                    /*return ListenableFutureTask.create(new Callable<PageCacheEntry>() {
                                        public PageCacheEntry call() throws Exception {
                                            return doLoad(key);
                                        }
                                    });*/
                                    //TODO async?
                                    try {
                                        ActionDescriptor actionDescriptor = loadActionDescriptor(key);
                                        return Futures.immediateFuture(
                                                new FileCacheEntry<>(actionDescriptor, key.getContent().getLastModifiedTime(), false));
                                    } catch (Throwable t) {
                                        logger.error(
                                                "Could not reload cached actionDescriptor from " + key.getName().getPath() +
                                                ", removing from cache", t);
                                        return Futures.immediateFuture(
                                                new FileCacheEntry<>(null, key.getContent().getLastModifiedTime(), true));
                                    }
                                } else {
                                    return Futures.immediateFuture(oldValue);
                                }
                            }

                        });
    }

    protected static FileObject getActionDescriptorFile(FileObject directory) throws FileSystemException {
        return directory.resolveFile("action.xml");
    }

    public static ActionDescriptor loadActionDescriptor(FileObject key) throws Exception {
        try(InputStream fileInputStream = key.getContent().getInputStream()) {
            ActionDescriptor action = loadActionDescriptor(fileInputStream);
            action.init();
            return action;
        }
    }

    public static ActionDescriptor loadActionDescriptor(InputStream inputStream) throws JAXBException {
        Unmarshaller unmarshaller = JAXB_CONTEXT.createUnmarshaller();
        return (ActionDescriptor) unmarshaller.unmarshal(inputStream);
    }

    public static ActionDescriptor getActionDescriptor(FileObject directory) throws ActionNotActiveException {
        FileObject actionDescriptorFile;
        try {
            actionDescriptorFile = getActionDescriptorFile(directory);
            FileCacheEntry<ActionDescriptor> entry = actionDescriptorCache.get(actionDescriptorFile);
            if(!entry.error) {
                return entry.object;
            } else {
                throw new ActionNotActiveException(directory.getName().getPath());
            }
        } catch (Exception e) {
            throw new ActionNotActiveException(directory.getName().getPath(), e);
        }
    }

    public static <T> T getLegacyConfiguration(
            FileObject configurationFile, Class<? extends T> configurationClass
    ) throws Exception {
        if (configurationClass == null) {
            return null;
        }
        return loadLegacyConfiguration(configurationFile, configurationClass);
    }

    public static <T> T loadLegacyConfiguration(
            FileObject configurationFile, Class<? extends T> configurationClass
    ) throws Exception {
        if (configurationClass == null) {
            return null;
        }
        try(InputStream inputStream = configurationFile.getContent().getInputStream()) {
            return loadLegacyConfiguration(inputStream, configurationClass);
        }
    }

    public static <T> T loadLegacyConfiguration(
            InputStream inputStream, Class<? extends T> configurationClass
    ) throws Exception {
        if (configurationClass == null) {
            return null;
        }
        String configurationPackage = configurationClass.getPackage().getName();
        JAXBContext jaxbContext = JAXBContext.newInstance(configurationPackage);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        JAXBElement element = unmarshaller.unmarshal(new StreamSource(inputStream), configurationClass);
        Object configuration = element.getValue();
        if (!configurationClass.isInstance(configuration)) {
            logger.error("Invalid configuration: expected " + configurationClass + ", got " + configuration);
            return null;
        }
        WebApplicationContext webApplicationContext =
                WebApplicationContextUtils.getRequiredWebApplicationContext(ElementsThreadLocals.getServletContext());
        webApplicationContext.getAutowireCapableBeanFactory().autowireBean(configuration);
        if(configuration instanceof ResourceActionConfiguration) {
            ((ResourceActionConfiguration) configuration).init();
        }
        return (T) configuration;
    }

    public static void configureResourceAction(ResourceAction resourceAction, ActionInstance actionInstance) {
        if(actionInstance.getConfiguration() != null) {
            logger.debug("ActionDescriptor instance {} is already configured", actionInstance);
            return;
        }
        FileObject configurationFile;
        try {
            configurationFile = actionInstance.getDirectory().resolveFile("configuration.xml");
        } catch (FileSystemException e) {
            throw new RuntimeException(e);
        }
        Class<?> configurationClass = ResourceActionLogic.getConfigurationClass(resourceAction.getClass());
        try {
            Object configuration = getLegacyConfiguration(configurationFile, configurationClass);
            actionInstance.setConfiguration(configuration);
        } catch (Throwable t) {
            logger.error("Couldn't load configuration from " + configurationFile.getName().getPath(), t);
        }
        resourceAction.setActionInstance(actionInstance);
    }

}
