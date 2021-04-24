/*
 * Copyright (C) 2005-2021 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.spring;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.modules.Module;
import com.manydesigns.portofino.servlets.PortofinoDispatcherInitializer;
import io.reactivex.disposables.Disposable;
import org.apache.commons.configuration2.Configuration;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigRegistry;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@link javax.servlet.ServletContextListener} to start and shut down Portofino's Spring application context.
 * The context is made of 3 layers:
 * <ol>
 *     <li><strong>Parent context</strong> - defines Portofino's own beans and modules. It's created once at application startup and destroyed once at shutdown.</li>
 *     <li><strong>User context</strong> - defines beans according to the user-provided SpringConfiguration class, if any. Otherwise it's an empty context.
 *     This context is reloaded automatically whenever the source code of class annotated with
 *     {@link Component}, {@link org.springframework.context.annotation.Configuration}, @{@link Repository} or @{@link Service} changes,
 *     and it can also be refreshed programmatically, via the {@link #refresh()} method. Note that such capability is meant to aid development, and NOT as a
 *     kind of hotswap for production. Requests will fail during a context reload. In fact, in production it can be deactivated by setting
 *     the init parameter <code>reloadContextWhenSourcesChange</code> to <code>false</code> in the deployment descriptor (<code>web.xml</code>).</li>
 *     <li><strong>Bridge context</strong> - a singleton application context meant to be exposed to outside consumers.
 *     It has the user context as a parent and it's created and destroyed only once.</li>
 * </ol>
 */
public class PortofinoContextLoaderListener extends ContextLoaderListener {

    public static final String RELOAD_CONTEXT_WHEN_SOURCES_CHANGE = "reloadContextWhenSourcesChange";
    public static final String PORTOFINO_CONTEXT_LOADER_LISTENER = PortofinoContextLoaderListener.class.getName();

    protected static final ThreadLocal<Boolean> reloadingUserContext = ThreadLocal.withInitial(() -> false);
    protected static final AtomicBoolean refreshing = new AtomicBoolean(true);
    private static final Logger logger = LoggerFactory.getLogger(PortofinoContextLoaderListener.class);
    public static final String PARENT_CONTEXT = "portofino-parent";
    public static final String BRIDGE_CONTEXT = "portofino-bridge";
    public static final String USER_CONTEXT = "portofino-user";

    protected final Set<Class<? extends Module>> moduleClasses = new HashSet<>();
    protected ServletContext servletContext;
    protected ConfigurableWebApplicationContext parentContext;
    protected PortofinoDispatcherInitializer initializer = new PortofinoDispatcherInitializer();
    protected final ConfigurableWebApplicationContext bridgeContext = new AnnotationConfigWebApplicationContext();

    public PortofinoContextLoaderListener(Set<Class<?>> candidateModuleClasses) {
        for(Class<?> candidate: candidateModuleClasses) {
            if(!candidate.isInterface() && !Modifier.isAbstract(candidate.getModifiers()) &&
                    Module.class.isAssignableFrom(candidate)) {
                moduleClasses.add(candidate.asSubclass(Module.class));
            }
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        bridgeContext.setId(BRIDGE_CONTEXT);
        servletContext = event.getServletContext();
        servletContext.setAttribute(PORTOFINO_CONTEXT_LOADER_LISTENER, this);
        try {
            ElementsThreadLocals.setupDefaultElementsContext();
            ElementsThreadLocals.setServletContext(servletContext);
            initializer.initWithServletContext(servletContext);
            super.contextInitialized(event);
            refreshing.set(false);
        } finally {
            ElementsThreadLocals.removeElementsContext();
        }
    }

    public static PortofinoContextLoaderListener get(ServletContext servletContext) {
        return (PortofinoContextLoaderListener) servletContext.getAttribute(PORTOFINO_CONTEXT_LOADER_LISTENER);
    }

    @NotNull
    @Override
    protected ConfigurableWebApplicationContext createWebApplicationContext(@NotNull ServletContext sc) {
        if(parentContext == null) {
            setupParentContext();
            setupBridgeContext();
        }
        ConfigurableWebApplicationContext userContext = setupUserContext();
        bridgeContext.setParent(userContext);
        return bridgeContext;
    }

    protected void setupBridgeContext() {
        bridgeContext.addApplicationListener(event -> {
            if(event instanceof ContextClosedEvent) {
                assert bridgeContext.getParent() != null;
                ((ConfigurableApplicationContext) bridgeContext.getParent()).close();
            }
        });
    }

    @NotNull
    public ConfigurableWebApplicationContext setupUserContext() {
        ConfigurableWebApplicationContext userContext = new AnnotationConfigWebApplicationContext();
        userContext.setParent(parentContext);
        userContext.setId(USER_CONTEXT);
        userContext.addApplicationListener(event -> {
            if(event instanceof ContextClosedEvent) {
                if(reloadingUserContext.get()) {
                    logger.debug("Reloading user context, not closing parent");
                } else {
                    parentContext.close();
                }
            }
        });
        try {
            Class<?> userConfigurationClass = initializer.getCodeBase().loadClass("SpringConfiguration");
            ((DefaultResourceLoader) userContext).setClassLoader(initializer.getCodeBase().asClassLoader());
            ((AnnotationConfigRegistry) userContext).register(userConfigurationClass);
            configureContextReload(userContext, initializer.getCodeBase());
        } catch (Exception e) {
            logger.info("User-defined Spring configuration not found");
            logger.debug("Additional info", e);
        }
        return userContext;
    }

    protected void configureContextReload(ConfigurableWebApplicationContext userContext, CodeBase codeBase) {
        boolean reload = !"false".equalsIgnoreCase(servletContext.getInitParameter(RELOAD_CONTEXT_WHEN_SOURCES_CHANGE));
        //TODO periodically check SpringConfiguration for changes if reload == true?
        if(reload) {
            Disposable subscription = codeBase.getReloads().subscribe(c -> {
                if (isConfigurationClass(c)) {
                    if (refreshing.compareAndSet(false, true)) {
                        logger.info("Detected reload of " + c + ", refreshing the application context");
                        try {
                            refresh();
                        } finally {
                            refreshing.set(false);
                        }
                    } else {
                        logger.warn("Detected reload of " + c + ", but the context is already refreshing");
                    }
                }
            });
            userContext.addApplicationListener(event -> {
                if (event instanceof ContextClosedEvent) {
                    subscription.dispose();
                }
            });
        }
    }

    protected void setupParentContext() {
        AnnotationConfigWebApplicationContext parentContext = new AnnotationConfigWebApplicationContext();
        parentContext.setParent(setupGrandParentContext());
        parentContext.setId(PARENT_CONTEXT);
        parentContext.setServletContext(servletContext);
        ConfigurableEnvironment environment = parentContext.getEnvironment();
        MutablePropertySources sources = environment.getPropertySources();
        Configuration configuration =
                (Configuration) servletContext.getAttribute(PortofinoSpringConfiguration.PORTOFINO_CONFIGURATION);
        sources.addFirst(
                new ConfigurationPropertySource("portofino.properties", configuration));
        for (Class<?> moduleClass : moduleClasses) {
            parentContext.register(moduleClass);
        }
        parentContext.register(PortofinoWebSpringConfiguration.class);
        parentContext.register(PortofinoSpringConfiguration.class);
        logger.info("Refreshing parent application context");
        parentContext.refresh();
        this.parentContext = parentContext;
    }

    @NotNull
    protected ApplicationContext setupGrandParentContext() {
        GenericApplicationContext grandParent = new GenericApplicationContext();
        grandParent.refresh();
        grandParent.getBeanFactory().registerSingleton("codeBase", initializer.getCodeBase());
        grandParent.getBeanFactory().registerSingleton(
                PortofinoSpringConfiguration.APPLICATION_DIRECTORY, initializer.getApplicationRoot());
        grandParent.getBeanFactory().registerSingleton(
                PortofinoSpringConfiguration.PORTOFINO_CONFIGURATION, initializer.getConfiguration());
        grandParent.getBeanFactory().registerSingleton(
                PortofinoSpringConfiguration.PORTOFINO_CONFIGURATION_FILE, initializer.getConfigurationFile());
        return grandParent;
    }

    @Override
    protected void configureAndRefreshWebApplicationContext(ConfigurableWebApplicationContext bridgeContext, ServletContext sc) {
        ConfigurableWebApplicationContext userContext = (ConfigurableWebApplicationContext) bridgeContext.getParent();
        assert userContext != null;
        logger.info("Configuring and refreshing user application context");
        super.configureAndRefreshWebApplicationContext(userContext, sc);
        logger.info("Refreshing bridge context");
        bridgeContext.refresh();
    }

    @Override
    protected void customizeContext(@NotNull ServletContext sc, ConfigurableWebApplicationContext bridgeContext) {
        if(ElementsThreadLocals.getElementsContext() == null) {
            ElementsThreadLocals.setupDefaultElementsContext();
        }
        ConfigurableWebApplicationContext userContext = (ConfigurableWebApplicationContext) bridgeContext.getParent();
        assert userContext != null;
        super.customizeContext(sc, userContext);
    }

    public void refresh() {
        reloadingUserContext.set(true);
        try {
            ConfigurableWebApplicationContext userContext = (ConfigurableWebApplicationContext) bridgeContext.getParent();
            assert userContext != null;
            userContext.close();
            createWebApplicationContext(servletContext);
            configureAndRefreshWebApplicationContext(bridgeContext, servletContext);
        } finally {
            reloadingUserContext.set(false);
        }
    }

    protected boolean isConfigurationClass(Class<?> c) {
        return c.getAnnotation(Component.class) != null ||
               c.getAnnotation(org.springframework.context.annotation.Configuration.class) != null ||
               c.getAnnotation(Repository.class) != null ||
               c.getAnnotation(Service.class) != null;
    }

    protected ConfigurableWebApplicationContext createParentApplicationContext() {
        return new AnnotationConfigWebApplicationContext();
    }

    public boolean isUserContextRefreshing() {
        return refreshing.get();
    }

}
