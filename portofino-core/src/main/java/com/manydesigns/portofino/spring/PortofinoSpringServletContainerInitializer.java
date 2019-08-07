package com.manydesigns.portofino.spring;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.modules.BaseModule;
import com.manydesigns.portofino.modules.Module;
import com.manydesigns.portofino.resourceactions.ResourceAction;
import com.manydesigns.portofino.servlets.PortofinoListener;
import io.reactivex.disposables.Disposable;
import org.apache.commons.configuration.Configuration;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
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
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@HandlesTypes(Module.class)
public class PortofinoSpringServletContainerInitializer implements ServletContainerInitializer {

    private static final Logger logger = LoggerFactory.getLogger(PortofinoSpringServletContainerInitializer.class);
    public static final String RELOAD_CONTEXT_WHEN_SOURCES_CHANGE = "reloadContextWhenSourcesChange";

    protected ServletContext servletContext;
    protected final Set<Class<? extends Module>> moduleClasses = new HashSet<>();
    protected final AtomicBoolean refreshing = new AtomicBoolean(false);

    @Override
    public void onStartup(Set<Class<?>> moduleClasses, ServletContext servletContext) throws ServletException {
        this.servletContext = servletContext;
        for(Class<?> candidate: moduleClasses) {
            if(!candidate.isInterface() && !Modifier.isAbstract(candidate.getModifiers()) &&
               Module.class.isAssignableFrom(candidate)) {
               this.moduleClasses.add(candidate.asSubclass(Module.class));
            }
        }
        registerContextLoaderListener(servletContext);
    }

    protected void registerContextLoaderListener(ServletContext servletContext) {
        ContextLoaderListener listener = new ContextLoaderListener() {

            @Override
            protected WebApplicationContext createWebApplicationContext(@NotNull ServletContext sc) {
                ConfigurableWebApplicationContext parentContext = createParentApplicationContext();
                ConfigurableWebApplicationContext rootContext = createRootApplicationContext();
                rootContext.setParent(parentContext);
                rootContext.addApplicationListener(event -> {
                    if(event instanceof ContextClosedEvent) {
                        parentContext.close();
                    }
                });
                CodeBase codeBase = (CodeBase) servletContext.getAttribute(PortofinoListener.CODE_BASE_ATTRIBUTE);
                try {
                    Class userConfigurationClass = codeBase.loadClass("SpringConfiguration");
                    ((DefaultResourceLoader) rootContext).setClassLoader(codeBase.asClassLoader());
                    ((AnnotationConfigRegistry) rootContext).register(userConfigurationClass);
                    boolean reload = !"false".equalsIgnoreCase(sc.getInitParameter(RELOAD_CONTEXT_WHEN_SOURCES_CHANGE));
                    Disposable subscription = codeBase.getReloads().subscribe(c -> {
                        if(reload && isReloadable(c) && refreshing.compareAndSet(false, true)) {
                            logger.info("Detected reload of " + c + ", refreshing the application context");
                            refresh(rootContext, codeBase);
                            refreshing.set(false);
                        }
                    });
                    rootContext.addApplicationListener(event -> {
                        if(event instanceof ContextClosedEvent) {
                            subscription.dispose();
                        }
                    });
                } catch (Exception e) {
                    logger.info("User-defined Spring configuration not found");
                    logger.debug("Additional info", e);
                }
                return rootContext;
            }

            @Override
            protected void customizeContext(@NotNull ServletContext sc, ConfigurableWebApplicationContext wac) {
                ElementsThreadLocals.setupDefaultElementsContext();
                ConfigurableWebApplicationContext parent = (ConfigurableWebApplicationContext) wac.getParent();
                assert parent != null;
                parent.setServletContext(sc);
                ConfigurableEnvironment environment = parent.getEnvironment();
                MutablePropertySources sources = environment.getPropertySources();
                Configuration configuration =
                        (Configuration) servletContext.getAttribute(BaseModule.PORTOFINO_CONFIGURATION);
                sources.addFirst(
                        new ConfigurationPropertySource("portofino.properties", configuration));
                AnnotationConfigRegistry annotationConfig = (AnnotationConfigRegistry) parent;
                for(Class<?> moduleClass : moduleClasses) {
                    annotationConfig.register(moduleClass);
                }
                annotationConfig.register(PortofinoSpringConfiguration.class);
                parent.refresh();
                super.customizeContext(sc, wac);
            }
        };
        listener.setContextInitializers(getRootApplicationContextInitializers());
        servletContext.addListener(listener);
    }

    protected boolean isReloadable(Class c) {
        return c.getAnnotation(Component.class) != null || c.getAnnotation(Repository.class) != null || c.getAnnotation(Service.class) != null;
    }

    public static void refresh(ConfigurableApplicationContext applicationContext, CodeBase codeBase) {
        //Spring enhances @Configuration classes. To do so it loads them by name from its classloader.
        //Thus, replacing the classloader with a fresh one has also the side-effect of making Spring reload the user
        //SpringConfiguration class, provided it already existed and was annotated with @Configuration.
        //Note that Spring won't pick up new @Bean methods. It will also barf badly on removed @Bean methods,
        //effectively crashing the application. Changing the return value and even the return type is fine.
        ((DefaultResourceLoader) applicationContext).setClassLoader(codeBase.asClassLoader());
        applicationContext.refresh();
    }

    protected ConfigurableWebApplicationContext createParentApplicationContext() {
        return new AnnotationConfigWebApplicationContext();
    }

    protected ConfigurableWebApplicationContext createRootApplicationContext() {
        return new AnnotationConfigWebApplicationContext();
    }

    protected ApplicationContextInitializer<?>[] getRootApplicationContextInitializers() {
        return new ApplicationContextInitializer[0];
    }
}
