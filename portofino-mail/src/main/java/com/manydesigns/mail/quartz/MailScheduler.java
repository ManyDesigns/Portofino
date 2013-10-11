/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.mail.quartz;

import com.manydesigns.mail.setup.MailProperties;
import com.manydesigns.mail.setup.MailQueueSetup;
import org.apache.commons.configuration.Configuration;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class MailScheduler {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public static final Logger logger = LoggerFactory.getLogger(MailScheduler.class);
    public static final int DEFAULT_POLL_INTERVAL = 1000;

    public static void setupMailScheduler(MailQueueSetup mailQueueSetup) {
        String group = "portofino-mail";
        setupMailScheduler(mailQueueSetup, group);
    }

    public static void setupMailScheduler(MailQueueSetup mailQueueSetup, String group) {
        Configuration mailConfiguration = mailQueueSetup.getMailConfiguration();
        if(mailConfiguration != null) {
            if(mailConfiguration.getBoolean(MailProperties.MAIL_QUARTZ_ENABLED, false)) {
                logger.info("Scheduling mail sends with Quartz job");
                try {
                    String serverUrl = mailConfiguration.getString(MailProperties.MAIL_SENDER_SERVER_URL);
                    logger.info("Scheduling mail sends using URL: {}", serverUrl);

                    Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
                    JobDetail job = JobBuilder
                            .newJob(URLInvokeJob.class)
                            .withIdentity("mail.sender", group)
                            .usingJobData(URLInvokeJob.URL_KEY, serverUrl)
                            .build();

                    int pollInterval = mailConfiguration.getInt(
                            MailProperties.MAIL_SENDER_POLL_INTERVAL, DEFAULT_POLL_INTERVAL);

                    Trigger trigger = TriggerBuilder.newTrigger()
                        .withIdentity("mail.sender.trigger", group)
                        .startNow()
                        .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                                .withIntervalInMilliseconds(pollInterval)
                                .repeatForever())
                        .build();

                    scheduler.scheduleJob(job, trigger);
                } catch (Exception e) {
                    logger.error("Could not schedule mail sender job", e);
                }
            } else {
                logger.info("Mail scheduling using Quartz is disabled");
            }
        }
    }
}
