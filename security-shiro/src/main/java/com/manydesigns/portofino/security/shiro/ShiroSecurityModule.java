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

import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.ResourceActionsModule;
import com.manydesigns.portofino.actions.ActionLogic;
import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.modules.Module;
import com.manydesigns.portofino.modules.ModuleStatus;
import com.manydesigns.portofino.shiro.SecurityClassRealm;
import com.manydesigns.portofino.shiro.SelfRegisteringShiroFilter;
import com.manydesigns.portofino.spring.PortofinoContextLoaderListener;
import com.manydesigns.portofino.spring.PortofinoSpringConfiguration;
import io.jsonwebtoken.io.Encoders;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.vfs2.FileObject;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.util.LifecycleUtils;
import org.apache.shiro.web.env.EnvironmentLoader;
import org.apache.shiro.web.env.WebEnvironment;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;
import java.util.UUID;

import static com.manydesigns.portofino.spring.PortofinoSpringConfiguration.PORTOFINO_CONFIGURATION;

/**
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class ShiroSecurityModule implements Module, ApplicationListener<ContextRefreshedEvent> {
    public static final String copyright =
            "Copyright (C) 2005-2021 ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    @Autowired
    public ServletContext servletContext;

    @Autowired
    @Qualifier(PORTOFINO_CONFIGURATION)
    public Configuration configuration;

    @Autowired
    @Qualifier(PortofinoSpringConfiguration.PORTOFINO_CONFIGURATION_FILE)
    public FileBasedConfigurationBuilder<?> configurationFile;

    @Autowired
    @Qualifier(ResourceActionsModule.ACTIONS_DIRECTORY)
    public FileObject actionsDirectory;

    @Autowired
    public CodeBase codeBase;

    protected EnvironmentLoader environmentLoader = new EnvironmentLoader();
    protected SecurityClassRealm realm;

    protected ModuleStatus status = ModuleStatus.CREATED;

    //**************************************************************************
    // Logging
    //**************************************************************************

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

    @PostConstruct
    public void init() throws Exception {
        if(!configuration.containsKey("jwt.secret")) {
            String jwtSecret = Encoders.BASE64.encode((UUID.randomUUID() + UUID.randomUUID().toString()).getBytes());
            logger.warn("No jwt.secret property was set, so we generated one: {}.", jwtSecret);
            configuration.setProperty("jwt.secret", jwtSecret);
            try {
                configurationFile.save();
                logger.info("Saved configuration file {}", configurationFile.getFileHandler().getFile().getAbsolutePath());
            } catch (ConfigurationException e) {
                logger.warn("Configuration could not be saved", e);
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

        String loginPath = actionsDirectory.getName().getPath() + configuration.getString(PortofinoProperties.LOGIN_PATH);
        logger.info("Login action: " + loginPath);
        ActionLogic.unmount(actionsDirectory, ":auth");
        ActionLogic.mount(actionsDirectory, ":auth", loginPath);
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
        if(PortofinoContextLoaderListener.BRIDGE_CONTEXT.equals(applicationContext.getId())) {
            realm.setApplicationContext(applicationContext);
            try {
                LifecycleUtils.init(realm);
            } catch (Exception e) {
                logger.warn("Security class not found or invalid or initialization failed. We will reload and/or initialize it on next use.", e);
            }
        }
    }
}
