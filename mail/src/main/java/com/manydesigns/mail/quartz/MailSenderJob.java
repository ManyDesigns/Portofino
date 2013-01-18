/*
* Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
* http://www.manydesigns.com/
*
* Unless you have purchased a commercial license agreement from ManyDesigns srl,
* the following license terms apply:
*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License version 3 as published by
* the Free Software Foundation.
*
* There are special exceptions to the terms and conditions of the GPL
* as it is applied to this software. View the full text of the
* exception in file OPEN-SOURCE-LICENSE.txt in the directory of this
* software distribution.
*
* This program is distributed WITHOUT ANY WARRANTY; and without the
* implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see http://www.gnu.org/licenses/gpl.txt
* or write to:
* Free Software Foundation, Inc.,
* 59 Temple Place - Suite 330,
* Boston, MA  02111-1307  USA
*
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
            "Copyright (c) 2005-2012, ManyDesigns srl";

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

        int pollInterval = mailConfiguration.getInt(MailProperties.MAIL_SENDER_POLL_INTERVAL);

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
