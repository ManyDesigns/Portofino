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

import com.manydesigns.mail.sender.MailSender;
import com.manydesigns.mail.setup.MailProperties;
import org.apache.commons.configuration.Configuration;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class MailSenderJob implements Job {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public static final String MAIL_SENDER_KEY = "mail.sender";
    public static final String MAIL_IDS_TO_MARK_AS_SENT = "mail.sender.idsToMarkAsSent";

    public static final Logger logger = LoggerFactory.getLogger(MailSenderJob.class);

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        MailSender sender;
        try {
            sender = (MailSender) jobExecutionContext.getScheduler().getContext().get(MAIL_SENDER_KEY);
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
            return;
        }
        if(sender != null) {
            Set<String> idsToMarkAsSent = (Set<String>) jobDataMap.get(MAIL_IDS_TO_MARK_AS_SENT);
            if(idsToMarkAsSent == null) {
                idsToMarkAsSent = new HashSet<String>();
            }
            int serverErrors = sender.runOnce(idsToMarkAsSent);
            if(serverErrors < 0) {
                logger.warn("Mail sender did not run.");
            } else if(serverErrors > 0) {
                logger.warn("Mail sender encountered {} server errors.", serverErrors);
            }
            jobDataMap.put(MAIL_IDS_TO_MARK_AS_SENT, idsToMarkAsSent);
        }
    }

    /**
     * Utility method to schedule the job at a fixed interval.
     */
    public static void schedule(MailSender mailSender, Configuration mailConfiguration, String group)
            throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        JobDetail job = JobBuilder
                .newJob(MailSenderJob.class)
                .withIdentity("mail.sender", group)
                .build();

        int pollInterval = mailConfiguration.getInt(
                MailProperties.MAIL_SENDER_POLL_INTERVAL, MailScheduler.DEFAULT_POLL_INTERVAL);

        Trigger trigger = TriggerBuilder.newTrigger()
            .withIdentity("mail.sender.trigger", group)
            .startNow()
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInMilliseconds(pollInterval)
                    .repeatForever())
            .build();

        scheduler.getContext().put(MailSenderJob.MAIL_SENDER_KEY, mailSender);
        scheduler.scheduleJob(job, trigger);
    }
}
