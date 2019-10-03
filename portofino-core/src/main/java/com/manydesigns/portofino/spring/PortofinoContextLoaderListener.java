package com.manydesigns.portofino.spring;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.modules.Module;
import com.manydesigns.portofino.servlets.PortofinoListener;
import io.reactivex.disposables.Disposable;
import org.apache.commons.configuration.Configuration;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigRegistry;
import org.springframework.context.event.ContextClosedEvent;
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
    protected static final
    AtomicBoolean refreshing = new AtomicBoolean(false);
    private static final Logger logger = LoggerFactory.getLogger(PortofinoContextLoaderListener.class);

    protected final Set<Class<? extends Module>> moduleClasses = new HashSet<>();
    protected ServletContext servletContext;
    protected ConfigurableWebApplicationContext parentContext;
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
        servletContext = event.getServletContext();
        bridgeContext.setId("portofino-bridge");
        super.contextInitialized(event);
        servletContext.setAttribute(PORTOFINO_CONTEXT_LOADER_LISTENER, this);
    }

    public static PortofinoContextLoaderListener get(ServletContext servletContext) {
        return (PortofinoContextLoaderListener) servletContext.getAttribute(PORTOFINO_CONTEXT_LOADER_LISTENER);
    }

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
    protected ConfigurableWebApplicationContext setupUserContext() {
        ConfigurableWebApplicationContext userContext = new AnnotationConfigWebApplicationContext();
        userContext.setParent(parentContext);
        userContext.setId("portofino-user");
        userContext.addApplicationListener(event -> {
            if(event instanceof ContextClosedEvent) {
                if(reloadingUserContext.get()) {
                    logger.debug("Reloading user context, not closing parent");
                } else {
                    parentContext.close();
                }
            }
        });
        CodeBase codeBase = (CodeBase) servletContext.getAttribute(PortofinoListener.CODE_BASE_ATTRIBUTE);
        try {
            //TODO periodically check SpringConfiguration for changes if reload == true?
            Class userConfigurationClass = codeBase.loadClass("SpringConfiguration");
            ((DefaultResourceLoader) userContext).setClassLoader(codeBase.asClassLoader());
            ((AnnotationConfigRegistry) userContext).register(userConfigurationClass);
            boolean reload = !"false".equalsIgnoreCase(servletContext.getInitParameter(RELOAD_CONTEXT_WHEN_SOURCES_CHANGE));
            Disposable subscription = codeBase.getReloads().subscribe(c -> {
                if(reload && isReloadable(c) && refreshing.compareAndSet(false, true)) {
                    logger.info("Detected reload of " + c + ", refreshing the application context");
                    refresh();
                    refreshing.set(false);
                }
            });
            userContext.addApplicationListener(event -> {
                if(event instanceof ContextClosedEvent) {
                    subscription.dispose();
                }
            });
        } catch (Exception e) {
            logger.info("User-defined Spring configuration not found");
            logger.debug("Additional info", e);
        }
        return userContext;
    }

    protected void setupParentContext() {
        parentContext = new AnnotationConfigWebApplicationContext();
        parentContext.setId("portofino-parent");
        parentContext.setServletContext(servletContext);
        ConfigurableEnvironment environment = parentContext.getEnvironment();
        MutablePropertySources sources = environment.getPropertySources();
        Configuration configuration =
                (Configuration) servletContext.getAttribute(PortofinoSpringConfiguration.PORTOFINO_CONFIGURATION);
        sources.addFirst(
                new ConfigurationPropertySource("portofino.properties", configuration));
        AnnotationConfigRegistry annotationConfig = (AnnotationConfigRegistry) parentContext;
        for (Class<?> moduleClass : moduleClasses) {
            annotationConfig.register(moduleClass);
        }
        annotationConfig.register(PortofinoSpringConfiguration.class);
        logger.info("Refreshing parent application context");
        parentContext.refresh();
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
        ElementsThreadLocals.setupDefaultElementsContext();
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

    protected boolean isReloadable(Class c) {
        return c.getAnnotation(Component.class) != null ||
               c.getAnnotation(org.springframework.context.annotation.Configuration.class) != null ||
               c.getAnnotation(Repository.class) != null ||
               c.getAnnotation(Service.class) != null;
    }

    protected ConfigurableWebApplicationContext createParentApplicationContext() {
        return new AnnotationConfigWebApplicationContext();
    }

}
