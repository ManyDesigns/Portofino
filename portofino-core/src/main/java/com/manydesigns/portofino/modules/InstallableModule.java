package com.manydesigns.portofino.modules;

import com.manydesigns.portofino.model.Domain;
import com.manydesigns.portofino.model.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;

public abstract class InstallableModule implements Module {

    public static final String MODULES_DOMAIN = "modules";

    @Autowired
    public ModelService modelService;

    protected ModuleStatus status = ModuleStatus.CREATED;

    @PostConstruct
    public void install() throws Exception {
        modelService.addBuiltInClass(InstalledModule.class);
        Domain modulesDomain = modelService.ensureTopLevelDomain(MODULES_DOMAIN, true);
        // TODO check installed version
        try {
            doInstall();
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

    protected abstract void doInstall() throws Exception;

    protected void start() throws Exception {
        status = ModuleStatus.STARTED;
    }

    @Bean(name = MODULES_DOMAIN)
    @Scope("prototype")
    public Domain getModulesDomain() {
        return modelService.ensureTopLevelDomain(MODULES_DOMAIN, true);
    }

    @Override
    public ModuleStatus getStatus() {
        return status;
    }
}
