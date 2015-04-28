/*
 * Copyright (C) 2005-2015 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.modules;

import com.manydesigns.mail.quartz.MailScheduler;
import com.manydesigns.mail.queue.MailQueue;
import com.manydesigns.mail.setup.MailQueueSetup;
import com.manydesigns.portofino.di.Inject;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class MailModule implements Module {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    @Inject(BaseModule.SERVLET_CONTEXT)
    public ServletContext servletContext;

    @Inject(BaseModule.PORTOFINO_CONFIGURATION)
    public Configuration configuration;

    protected MailQueueSetup mailQueueSetup;

    protected ModuleStatus status = ModuleStatus.CREATED;

    //**************************************************************************
    // Constants
    //**************************************************************************

    public final static String MAIL_QUEUE = "com.manydesigns.mail.queue";
    public final static String MAIL_SENDER = "com.manydesigns.mail.sender";

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(MailModule.class);

    @Override
    public String getModuleVersion() {
        return ModuleRegistry.getPortofinoVersion();
    }

    @Override
    public int getMigrationVersion() {
        return 1;
    }

    @Override
    public double getPriority() {
        return 20;
    }

    @Override
    public String getId() {
        return "mail";
    }

    @Override
    public String getName() {
        return "Mail";
    }

    @Override
    public int install() {
        return 1;
    }

    @Override
    public void init() {
        mailQueueSetup = new MailQueueSetup(configuration);
        mailQueueSetup.setup();

        MailQueue mailQueue = mailQueueSetup.getMailQueue();
        if(mailQueue == null) {
            logger.info("Mail queue not enabled");
            return;
        }

        servletContext.setAttribute(MAIL_QUEUE, mailQueue);
        servletContext.setAttribute(MAIL_SENDER, mailQueueSetup.getMailSender());

        status = ModuleStatus.ACTIVE;
    }

    @Override
    public void start() {
        //Quartz integration (optional)
        try {
            //In classe separata per permettere al modulo di essere caricato anche in assenza di Quartz a runtime
            MailScheduler.setupMailScheduler(mailQueueSetup);
        } catch (NoClassDefFoundError e) {
            logger.debug(e.getMessage(), e);
            logger.info("Quartz is not available, mail scheduler not started");
        }
        status = ModuleStatus.STARTED;
    }

    @Override
    public void stop() {
        status = ModuleStatus.STOPPED;
    }

    @Override
    public void destroy() {
        status = ModuleStatus.DESTROYED;
    }

    @Override
    public ModuleStatus getStatus() {
        return status;
    }
}
