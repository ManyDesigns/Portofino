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

package com.manydesigns.portofino.resourceactions;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.portofino.dispatcher.ResourceResolver;
import com.manydesigns.portofino.resourceactions.annotations.ConfigurationClass;
import com.manydesigns.portofino.resourceactions.annotations.ScriptTemplate;
import com.manydesigns.portofino.resourceactions.annotations.SupportsDetail;
import com.manydesigns.portofino.rest.PortofinoRoot;
import com.manydesigns.portofino.servlets.PortofinoDispatcherInitializer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ResourceActionSupport implements ApplicationContextAware {
    public static final String copyright = "Copyright (C) 2005-2020 ManyDesigns srl";

    public static final Logger logger = LoggerFactory.getLogger(ResourceActionSupport.class);
    protected static final JAXBContext JAXB_CONTEXT;
    protected final PortofinoDispatcherInitializer dispatcherInitializer;
    protected final FileObject actionsDirectory;
    protected ApplicationContext applicationContext;

    static {
        try {
            JAXB_CONTEXT = JAXBContext.newInstance(ResourceActionConfiguration.class.getPackage().getName());
        } catch (JAXBException e) {
            throw new Error("Can't instantiate pages jaxb context", e);
        }
    }

    public ResourceActionSupport(PortofinoDispatcherInitializer dispatcherInitializer, FileObject actionsDirectory) {
        this.dispatcherInitializer = dispatcherInitializer;
        this.actionsDirectory = actionsDirectory;
    }

    public static boolean supportsDetail(Class<?> actionClass) {
        if(!ResourceAction.class.isAssignableFrom(actionClass)) {
            return false;
        }
        SupportsDetail supportsDetail = actionClass.getAnnotation(SupportsDetail.class);
        if(supportsDetail != null) {
            return supportsDetail.value();
        } else {
            return supportsDetail(actionClass.getSuperclass());
        }
    }

    public static Class<? extends ResourceActionConfiguration> getConfigurationClass(Class<?> actionClass) {
        if(!ResourceAction.class.isAssignableFrom(actionClass)) {
            return null;
        }
        ConfigurationClass configurationClass = actionClass.getAnnotation(ConfigurationClass.class);
        if(configurationClass != null) {
            return configurationClass.value().asSubclass(ResourceActionConfiguration.class);
        } else {
            return getConfigurationClass(actionClass.getSuperclass());
        }
    }

    public static String getScriptTemplate(Class<?> actionClass) {
        if(!ResourceAction.class.isAssignableFrom(actionClass)) {
            return null;
        }
        ScriptTemplate scriptTemplate = actionClass.getAnnotation(ScriptTemplate.class);
        if(scriptTemplate != null) {
            String templateLocation = scriptTemplate.value();
            try {
                return IOUtils.toString(actionClass.getResourceAsStream(templateLocation), StandardCharsets.UTF_8);
            } catch (Exception e) {
                logger.error("Can't load script template: " + templateLocation + " for class: " + actionClass.getName(), e);
            }
        } else {
            String template = getScriptTemplate(actionClass.getSuperclass());
            if(template != null) {
                return template;
            }
        }
        logger.debug("Falling back to default template for {}", actionClass);
        try {
            InputStream stream =
                    ResourceActionSupport.class.getResourceAsStream
                            ("/com/manydesigns/portofino/resourceactions/default_script_template.txt");
            return IOUtils.toString(stream, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new Error("Can't load script template", e);
        }
    }

    public static String getDescriptionKey(Class<?> actionClass) {
        ResourceActionName annotation = actionClass.getAnnotation(ResourceActionName.class);
        if(annotation != null) {
            return annotation.value();
        } else {
            return actionClass.getName();
        }
    }

    public void mount(FileObject actionDirectory, String segment, String path) throws Exception {
        ResourceAction instance = getResourceAction(actionDirectory);
        if (instance == null) {
            throw new Exception("Cannot mount " + segment + " on " + actionDirectory + ", resource does not exist");
        }
        ResourceActionConfiguration configuration = instance.loadConfiguration();
        if (configuration == null) {
            throw new Exception("Cannot mount " + segment + " on " + actionDirectory + ", resource does not exist");
        }
        Optional<AdditionalChild> existing =
                configuration.getAdditionalChildren().stream().filter(c -> c.getSegment().equals(segment)).findFirst();
        if(existing.isPresent()) {
            String existingPath = existing.get().getPath();
            if(!path.equals(existingPath)) {
                throw new IllegalArgumentException("Another path is already mounted at " + segment + ": " + existingPath);
            }
        } else {
            AdditionalChild child = new AdditionalChild();
            child.setSegment(segment);
            child.setPath(path);
            configuration.getAdditionalChildren().add(child);
            instance.saveConfiguration();
        }
    }

    protected ResourceAction getResourceAction(FileObject actionDirectory) throws Exception {
        ResourceResolver resres = dispatcherInitializer.getResourceResolver();
        ResourceAction action;
        if (actionDirectory.getURI().equals(actionsDirectory.getURI())) {
            action = PortofinoRoot.get(actionsDirectory, resres);
        }  else {
            action = resres.resolve(actionDirectory, ResourceAction.class);
        }
        action.setActionInstance(new ActionInstance(null, actionDirectory, action.getClass()));
        applicationContext.getAutowireCapableBeanFactory().autowireBean(action);
        return action;
    }

    public void mount(FileObject actionDirectory, String segment, Class<?> actionClass) throws Exception {
        mount(actionDirectory, segment, "res:" + actionClass.getName().replace('.', '/') + ".class");
    }

    public void mountPackage(FileObject actionDirectory, String segment, String packageName) throws Exception {
        mount(actionDirectory, segment, "res:" + packageName.replace('.', '/'));
    }

    public void mountPackage(FileObject actionDirectory, String segment, Package pkg) throws Exception {
        mountPackage(actionDirectory, segment, pkg.getName());
    }

    public void unmount(FileObject actionDirectory, String segment) throws Exception {
        ResourceAction instance = getResourceAction(actionDirectory);
        if (instance == null) {
            return;
        }
        ResourceActionConfiguration descriptor = instance.loadConfiguration();
        if (descriptor != null) {
            Optional<AdditionalChild> existing =
                    descriptor.getAdditionalChildren().stream().filter(c -> c.getSegment().equals(segment)).findFirst();
            if (existing.isPresent()) {
                descriptor.getAdditionalChildren().remove(existing.get());
                instance.saveConfiguration();
            }
        }
    }

    protected static FileObject getLegacyActionDescriptorFile(FileObject directory) throws FileSystemException {
        return directory.resolveFile("action.xml");
    }

    public static ResourceActionConfiguration loadLegacyActionDescriptor(FileObject key) throws Exception {
        try(InputStream fileInputStream = key.getContent().getInputStream()) {
            ResourceActionConfiguration action = loadLegacyActionDescriptor(fileInputStream);
            action.init();
            return action;
        }
    }

    public static ResourceActionConfiguration loadLegacyActionDescriptor(InputStream inputStream) throws JAXBException {
        Unmarshaller unmarshaller = JAXB_CONTEXT.createUnmarshaller();
        return (ResourceActionConfiguration) unmarshaller.unmarshal(inputStream);
    }

    public static <T extends ResourceActionConfiguration> T getLegacyConfiguration(
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
        if (!configurationFile.exists()) {
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

        ResourceActionConfiguration action;
        try {
            action = loadLegacyActionDescriptor(getLegacyActionDescriptorFile(resourceAction.getLocation()));
        } catch (Exception e) {
            logger.debug("action.xml not found or not valid", e);
            action = new ResourceActionConfiguration();
            action.init();
        }

        FileObject configurationFile;
        try {
            configurationFile = actionInstance.getDirectory().resolveFile("configuration.xml");
        } catch (FileSystemException e) {
            throw new RuntimeException(e);
        }
        Class<? extends ResourceActionConfiguration> configurationClass =
                getConfigurationClass(resourceAction.getClass());
        try {
            ResourceActionConfiguration configuration = getLegacyConfiguration(configurationFile, configurationClass);
            if (configuration != null) {
                configuration.permissions = action.permissions;
                configuration.additionalChildren.addAll(action.additionalChildren);
            } else {
                configuration = action;
            }
            actionInstance.setConfiguration(configuration);
        } catch (Throwable t) {
            logger.error("Couldn't load configuration from " + configurationFile.getName().getPath(), t);
        }
        resourceAction.setActionInstance(actionInstance);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
