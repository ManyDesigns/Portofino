package com.manydesigns.portofino.spring;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.portofino.modules.BaseModule;
import com.manydesigns.portofino.modules.Module;
import com.manydesigns.portofino.modules.ModuleRegistry;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContext;
import java.io.File;

@Component
public class DefaultSpringConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSpringConfiguration.class);

    protected ServletContext servletContext;

    @Bean(name = "portofinoConfiguration")
    public Configuration getPortofinoConfiguration() {
        return (Configuration) getServletContext().getAttribute(BaseModule.PORTOFINO_CONFIGURATION);
    }

    @Bean(name = "applicationDirectory")
    public File getApplicationDirectory() {
        return (File) getServletContext().getAttribute(BaseModule.APPLICATION_DIRECTORY);
    }

    @Bean
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Autowired
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Bean
    public ModuleRegistry getModuleRegistry(@Autowired Configuration configuration) {
        return new ModuleRegistry(configuration);
    }

    @Bean
    public ModuleInitializer getModuleInitializer() {
        return new ModuleInitializer();
    }

    @Bean
    public ModuleDestroyer getModuleDestroyer() {
        return new ModuleDestroyer();
    }

    public static class ModuleInitializer extends ModuleEventListener<ContextRefreshedEvent> implements ApplicationContextAware {

        protected ApplicationContext applicationContext;

        @Override
        public void onApplicationEvent(ContextRefreshedEvent event) {
            ElementsThreadLocals.setupDefaultElementsContext();
            ElementsThreadLocals.setServletContext(servletContext);
            try {
                logger.info("Loading modules...");
                moduleRegistry.getModules().addAll(applicationContext.getBeansOfType(Module.class).values());
                moduleRegistry.migrateAndInit(servletContext);
                logger.info("Starting modules...");
                moduleRegistry.start();
                logger.info("Modules started.");
            } finally {
                ElementsThreadLocals.removeElementsContext();
            }
        }

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            this.applicationContext = applicationContext;
        }
    }

    public static class ModuleDestroyer extends ModuleEventListener<ContextClosedEvent> {

        @Override
        public void onApplicationEvent(ContextClosedEvent event) {
            logger.info("Stopping modules...");
            moduleRegistry.stop();
            logger.info("Destroying modules...");
            moduleRegistry.destroy();
            logger.info("Modules destroyed.");
        }
    }

}
