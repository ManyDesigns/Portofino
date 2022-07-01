/*
 * Copyright (C) 2005-2022 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.microservices.boot;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.portofino.code.AggregateCodeBase;
import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.code.JavaCodeBase;
import com.manydesigns.portofino.dispatcher.ResourceResolver;
import com.manydesigns.portofino.modules.Module;
import com.manydesigns.portofino.servlets.PortofinoDispatcherInitializer;
import com.manydesigns.portofino.spring.PortofinoContextLoaderListener;
import com.manydesigns.portofino.spring.PortofinoSpringConfiguration;
import com.manydesigns.portofino.spring.PortofinoWebSpringConfiguration;
import org.apache.commons.vfs2.FileObject;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import javax.servlet.ServletContext;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Alessio Stalla – alessiostalla@gmail.com
 */
public class PortofinoAnnotationConfigServletWebServerApplicationContext extends
        AnnotationConfigServletWebServerApplicationContext {

    protected final FileObject applicationDirectory;
    private static final Logger logger =
            LoggerFactory.getLogger(PortofinoAnnotationConfigServletWebServerApplicationContext.class);

    public PortofinoAnnotationConfigServletWebServerApplicationContext(FileObject applicationDirectory) {
        this.applicationDirectory = applicationDirectory;
    }

    @Override
    protected void prepareWebApplicationContext(ServletContext servletContext) {
        super.prepareWebApplicationContext(servletContext);
        ElementsThreadLocals.setupDefaultElementsContext();
        ElementsThreadLocals.setServletContext(servletContext);

        //Only scan once, for performance
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false, getEnvironment());
        scanner.addIncludeFilter(new AssignableTypeFilter(CodeBase.class));
        scanner.addExcludeFilter(new AssignableTypeFilter(AggregateCodeBase.class));
        scanner.addExcludeFilter(new AssignableTypeFilter(JavaCodeBase.class));
        scanner.addIncludeFilter(new AssignableTypeFilter(ResourceResolver.class));
        scanner.addIncludeFilter(new AssignableTypeFilter(Module.class));
        Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents("");
        Set<Class<? extends CodeBase>> cbClasses = findImplementations(CodeBase.class, candidateComponents);
        Set<Class<? extends ResourceResolver>> resres = findImplementations(ResourceResolver.class, candidateComponents);
        PortofinoDispatcherInitializer initializer = new PortofinoDispatcherInitializer(cbClasses, resres) {
            @Override
            protected String getApplicationDirectoryPath() {
                return applicationDirectory.getName().getURI();
            }
        };
        initializer.initWithServletContext(servletContext);

        ApplicationContext grandParent = PortofinoContextLoaderListener.setupGrandParentContext(initializer);

        AnnotationConfigWebApplicationContext parent = new AnnotationConfigWebApplicationContext();
        parent.setParent(grandParent);
        parent.setServletContext(servletContext);

        Set<Class<? extends Module>> moduleClasses = findImplementations(Module.class, candidateComponents);
        moduleClasses.forEach(parent::register);

        parent.register(PortofinoWebSpringConfiguration.class);
        parent.register(PortofinoSpringConfiguration.class);

        //User-defined beans
        try {
            Class<?> userConfigurationClass = initializer.getCodeBase().loadClass("SpringConfiguration");
            parent.setClassLoader(initializer.getCodeBase().asClassLoader());
            parent.register(userConfigurationClass);
        } catch (Exception e) {
            logger.info("User-defined Spring configuration not found");
            logger.debug("Additional info", e);
        }

        parent.refresh();
        setParent(parent);
    }

    @NotNull
    private <T> Set<Class<? extends T>> findImplementations(Class<T> target, Set<BeanDefinition> candidateComponents) {
        Set<Class<? extends T>> implementations = new LinkedHashSet<>();
        candidateComponents.forEach(c -> {
            try {
                Class<?> aClass = Class.forName(c.getBeanClassName());
                if (target.isAssignableFrom(aClass)) {
                    implementations.add((Class<? extends T>) aClass);
                }
            } catch (Exception e) {
                logger.error("Could not load module class", e);
            }
        });
        return implementations;
    }
}
