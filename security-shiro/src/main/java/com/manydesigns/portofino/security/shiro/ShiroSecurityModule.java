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

package com.manydesigns.portofino.security.shiro;

import com.manydesigns.portofino.ResourceActionsModule;
import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.config.ConfigurationSource;
import com.manydesigns.portofino.dispatcher.DispatcherInitializer;
import com.manydesigns.portofino.model.service.ModelService;
import com.manydesigns.portofino.modules.Module;
import com.manydesigns.portofino.modules.ModuleStatus;
import com.manydesigns.portofino.resourceactions.ResourceActionSupport;
import com.manydesigns.portofino.resourceactions.login.DefaultLoginAction;
import com.manydesigns.portofino.rest.PortofinoRoot;
import com.manydesigns.portofino.security.SecurityLogic;
import com.manydesigns.portofino.shiro.SecurityClassRealm;
import com.manydesigns.portofino.shiro.SelfRegisteringShiroFilter;
import com.manydesigns.portofino.shiro.ShiroSecurity;
import com.manydesigns.portofino.spring.PortofinoContextLoaderListener;
import io.jsonwebtoken.io.Encoders;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.vfs2.FileObject;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.util.LifecycleUtils;
import org.apache.shiro.web.env.EnvironmentLoader;
import org.apache.shiro.web.env.WebEnvironment;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;
import java.util.UUID;

/**
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class ShiroSecurityModule implements
        Module, ApplicationContextAware, ApplicationListener<ContextRefreshedEvent> {
    public static final String copyright =
            "Copyright (C) 2005-2021 ManyDesigns srl";

    @Autowired
    public ServletContext servletContext;

    @Autowired
    public ConfigurationSource configuration;
    @Autowired
    @Qualifier(ResourceActionsModule.ACTIONS_DIRECTORY)
    public FileObject actionsDirectory;
    @Autowired
    public CodeBase codeBase;
    @Autowired
    public ModelService modelService;
    @Autowired
    public DispatcherInitializer dispatcherInitializer;

    protected ApplicationContext applicationContext;
    protected EnvironmentLoader environmentLoader = new EnvironmentLoader();
    protected SecurityClassRealm realm;

    protected ModuleStatus status = ModuleStatus.CREATED;

    public static final Logger logger =
            LoggerFactory.getLogger(ShiroSecurityModule.class);

    @Override
    public String getModuleVersion() {
        return Module.getPortofinoVersion();
    }

    @Override
    public String getName() {
        return "Shiro Security";
    }

    @Bean
    public ShiroSecurity getSecurityFacade() {
        return new ShiroSecurity();
    }

    @PostConstruct
    public void init() throws Exception {
        if(!configuration.getProperties().containsKey("jwt.secret")) {
            String jwtSecret = Encoders.BASE64.encode((UUID.randomUUID() + UUID.randomUUID().toString()).getBytes());
            logger.warn("No jwt.secret property was set, so we generated one: {}.", jwtSecret);
            configuration.getProperties().setProperty("jwt.secret", jwtSecret);
            if (configuration.isWritable()) try {
                configuration.save();
            } catch (ConfigurationException e) {
                logger.warn("Configuration could not be saved: the jwt.secret won't be persisted.", e);
            } else {
                logger.warn("Configuration is not writable: the jwt.secret won't be persisted.");
            }
        }

        logger.info("Initializing Shiro environment");
        WebEnvironment environment = environmentLoader.initEnvironment(servletContext);
        RealmSecurityManager rsm = (RealmSecurityManager) environment.getWebSecurityManager();
        SelfRegisteringShiroFilter shiroFilter = SelfRegisteringShiroFilter.get(servletContext);
        if(shiroFilter != null) {
            try {
                //when reloading the Spring context, this overwrites the filter's stale security manager.
                shiroFilter.init();
            } catch (Exception e) {
                logger.error("Could not initialize the Shiro filter", e);
                status = ModuleStatus.FAILED;
                return;
            }
        }
        logger.debug("Creating SecurityClassRealm");
        realm = new SecurityClassRealm(codeBase, "Security");
        rsm.setRealm(realm);

        modelService.modelEvents.filter(evt -> evt == ModelService.EventType.LOADED).take(1).subscribe(evt -> {
            try {
                PortofinoRoot root = ResourceActionsModule.getRootResource(
                        actionsDirectory, dispatcherInitializer.getResourceResolver(),
                        servletContext, applicationContext, modelService);
                SecurityLogic.installLogin(root, configuration.getProperties(), DefaultLoginAction.class);
            } catch (Exception e) {
                logger.error("Could not install login action", e);
            }
        });
        status = ModuleStatus.STARTED;
    }

    @PreDestroy
    public void destroy() {
        logger.info("Destroying Shiro environment...");
        environmentLoader.destroyEnvironment(servletContext);
        status = ModuleStatus.DESTROYED;
    }

    @Override
    public ModuleStatus getStatus() {
        return status;
    }

    @Override
    public void onApplicationEvent(@NotNull ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        realm.setApplicationContext(applicationContext);
        try {
            LifecycleUtils.init(realm);
        } catch (Exception e) {
            logger.warn(
                    "Security class not found or invalid or initialization failed. " +
                    "We will reload and/or initialize it on next use.", e);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
