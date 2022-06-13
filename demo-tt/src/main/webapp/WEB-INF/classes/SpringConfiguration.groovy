/*
* Copyright (C) 2005-2022 ManyDesigns srl.  All rights reserved.
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


import com.manydesigns.portofino.persistence.Persistence
import com.manydesigns.portofino.quartz.JobRegistration
import com.manydesigns.portofino.quartz.SchedulerService
import com.manydesigns.portofino.tt.Dependency
import com.manydesigns.portofino.tt.NotificationsJob
import com.manydesigns.portofino.tt.Refresh
import io.reactivex.disposables.Disposable
import org.quartz.DateBuilder
import org.quartz.SimpleScheduleBuilder
import org.quartz.TriggerBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Configuration
class SpringConfiguration {

    Disposable subscription
    JobRegistration notificationsJob

    @Autowired
    Persistence persistence

    @Autowired
    SchedulerService schedulerService

    static final Logger logger = LoggerFactory.getLogger(SpringConfiguration)

    @PostConstruct
    void init() {
        int pollSecInterval = 10
        def schedule = TriggerBuilder.newTrigger()
                .startAt(DateBuilder.futureDate(pollSecInterval, DateBuilder.IntervalUnit.SECOND))
                .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(pollSecInterval))
        notificationsJob = new JobRegistration(NotificationsJob, schedule, "tt")
        schedulerService.register(notificationsJob)

        subscription = persistence.status.subscribe({ status ->
            logger.info("Persistence status: ${status}")
        })
    }

    @PreDestroy
    void destroy() {
        subscription.dispose()
        schedulerService.unschedule(notificationsJob)
    }

    @Bean
    Refresh getRefresh() {
        new Refresh()
    }

    @Bean
    Dependency getDependency() {
        new Dependency()
    }

}
