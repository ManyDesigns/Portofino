/*
 * Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.mail;

import com.manydesigns.mail.quartz.MailScheduler;
import com.manydesigns.mail.queue.MailQueue;
import com.manydesigns.mail.rest.SendMailAction;
import com.manydesigns.mail.sender.MailSender;
import com.manydesigns.mail.setup.MailProperties;
import com.manydesigns.mail.setup.MailQueueSetup;
import com.manydesigns.portofino.ResourceActionsModule;
import com.manydesigns.portofino.dispatcher.DispatcherInitializer;
import com.manydesigns.portofino.model.service.ModelService;
import com.manydesigns.portofino.modules.Module;
import com.manydesigns.portofino.modules.ModuleStatus;
import com.manydesigns.portofino.rest.PortofinoRoot;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.vfs2.FileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;

import static com.manydesigns.portofino.ResourceActionsModule.ACTIONS_DIRECTORY;

/**
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class MailModule implements Module {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    @Autowired
    public ServletContext servletContext;
    @Autowired
    public Configuration configuration;
    @Autowired
    public ModelService modelService;
    @Autowired
    @Qualifier(ACTIONS_DIRECTORY)
    public FileObject actionsDirectory;
    @Autowired
    public DispatcherInitializer dispatcherInitializer;

    protected MailQueueSetup mailQueueSetup;

    protected ModuleStatus status = ModuleStatus.CREATED;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(MailModule.class);

    @Override
    public String getModuleVersion() {
        return Module.getPortofinoVersion();
    }

    @Override
    public String getName() {
        return "Mail";
    }

    @PostConstruct
    public void init() throws Exception {
        mailQueueSetup = new MailQueueSetup(configuration);
        mailQueueSetup.setup();
        maybeSetupQuartz();

        if(configuration.getBoolean(MailProperties.MAIL_SENDER_ACTION_ENABLED, true)) {
            modelService.modelEvents.filter(evt -> evt == ModelService.EventType.LOADED).take(1).subscribe(evt -> {
                String segment = configuration.getString(
                        MailProperties.MAIL_SENDER_ACTION_SEGMENT, "portofino-send-mail");
                try {
                    PortofinoRoot root = ResourceActionsModule.getRootResource(
                            actionsDirectory, dispatcherInitializer.getResourceResolver(), servletContext, modelService);
                    root.mount(segment, SendMailAction.class);
                } catch (Exception e) {
                    logger.error("Could not install send mail action", e);
                }
            });
        }

        status = ModuleStatus.STARTED;
    }

    protected void maybeSetupQuartz() {
        try {
            //In classe separata per permettere al modulo di essere caricato anche in assenza di Quartz a runtime
            MailScheduler.setupMailScheduler(mailQueueSetup);
        } catch (NoClassDefFoundError e) {
            logger.debug(e.getMessage(), e);
            logger.info("Quartz is not available, mail scheduler not started");
        }
    }

    @PreDestroy
    public void destroy() {
        status = ModuleStatus.DESTROYED;
    }

    @Bean
    public MailQueue getMailQueue() {
        return mailQueueSetup.getMailQueue();
    }

    @Bean
    public MailSender getMailSender() {
        return mailQueueSetup.getMailSender();
    }

    @Override
    public ModuleStatus getStatus() {
        return status;
    }

}
