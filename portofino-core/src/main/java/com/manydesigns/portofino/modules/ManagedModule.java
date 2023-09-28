package com.manydesigns.portofino.modules;

import com.manydesigns.portofino.model.Domain;
import com.manydesigns.portofino.model.service.ModelService;
import com.vdurmont.semver4j.Semver;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.ContextRefreshedEvent;

import jakarta.annotation.PostConstruct;
import java.beans.IntrospectionException;
import java.io.IOException;

public abstract class ManagedModule implements Module, ApplicationListener<ContextRefreshedEvent> {

    public static final String MODULES_DOMAIN = "modules";
    private static final Logger logger = LoggerFactory.getLogger(ManagedModule.class);

    @Autowired
    public ModelService modelService;

    protected ModuleStatus status = ModuleStatus.CREATED;

    @PostConstruct
    public void install() throws Exception {
        addRequiredClasses();
        Domain modulesDomain = modelService.ensureTopLevelDomain(MODULES_DOMAIN);
        try {
            Object modelObj = modelService.getJavaObject(modulesDomain, getName());
            if (modelObj instanceof InstalledModule) {
                InstalledModule installed = (InstalledModule) modelObj;
                Semver installedVersion = new Semver(installed.getVersion(), Semver.SemverType.LOOSE);
                Semver thisVersion = new Semver(getModuleVersion(), Semver.SemverType.LOOSE);
                if(thisVersion.isGreaterThan(installedVersion)) {
                    logger.info("Updating module " + getName() + " from " + installedVersion + " to " + thisVersion);
                    updateFrom(installedVersion);
                } else {
                    logger.debug("Module " + getName() + " already at latest version: " + thisVersion);
                }
            } else {
                logger.info("Installing module " + getName() + " version " + getModuleVersion());
                doInstall();
            }
            modelService.putJavaObject(modulesDomain, getName(), new InstalledModule(getModuleVersion()));
            modelService.saveObject(modulesDomain, getName());
            status = ModuleStatus.INSTALLED;
        } catch (Exception e) {
            status = ModuleStatus.FAILED;
            throw e;
        }
    }

    protected void addRequiredClasses() throws IntrospectionException {
        modelService.addBuiltInClass(InstalledModule.class);
    }

    public void updateFrom(Semver installedVersion) {

    }

    protected void doInstall() throws Exception {}

    protected void start(ApplicationContext applicationContext) throws Exception {
        status = ModuleStatus.STARTED;
    }

    @Override
    public void onApplicationEvent(@NotNull ContextRefreshedEvent event) {
        if (status == ModuleStatus.INSTALLED) {
            try {
                start(event.getApplicationContext());
                status = ModuleStatus.STARTED;
            } catch (Exception e) {
                status = ModuleStatus.FAILED;
                throw new RuntimeException(e);
            }
        }
    }

    @Bean(name = MODULES_DOMAIN)
    @Scope("prototype")
    public Domain getModulesDomain() throws IOException {
        return modelService.ensureTopLevelDomain(MODULES_DOMAIN);
    }

    @Override
    public ModuleStatus getStatus() {
        return status;
    }
}
