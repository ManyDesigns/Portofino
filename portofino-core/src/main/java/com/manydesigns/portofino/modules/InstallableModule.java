package com.manydesigns.portofino.modules;

import com.manydesigns.portofino.model.Domain;
import com.manydesigns.portofino.model.service.ModelService;
import com.vdurmont.semver4j.Semver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;

public abstract class InstallableModule implements Module {

    public static final String MODULES_DOMAIN = "modules";
    private static final Logger logger = LoggerFactory.getLogger(InstallableModule.class);

    @Autowired
    public ModelService modelService;

    protected ModuleStatus status = ModuleStatus.CREATED;

    @PostConstruct
    public void install() throws Exception {
        modelService.addBuiltInClass(InstalledModule.class);
        Domain modulesDomain = modelService.ensureSystemDomain(MODULES_DOMAIN);
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
            start();
            status = ModuleStatus.STARTED;
        } catch (Exception e) {
            status = ModuleStatus.FAILED;
            throw e;
        }
    }

    public void updateFrom(Semver installedVersion) {

    }

    protected abstract void doInstall() throws Exception;

    protected void start() throws Exception {
        status = ModuleStatus.STARTED;
    }

    @Bean(name = MODULES_DOMAIN)
    @Scope("prototype")
    public Domain getModulesDomain() {
        return modelService.ensureSystemDomain(MODULES_DOMAIN);
    }

    @Override
    public ModuleStatus getStatus() {
        return status;
    }
}
