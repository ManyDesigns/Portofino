import com.manydesigns.portofino.persistence.Persistence
import com.manydesigns.portofino.tt.Dependency
import com.manydesigns.portofino.tt.NotificationsJob
import com.manydesigns.portofino.tt.Refresh
import com.manydesigns.portofino.tt.TtUtils
import io.reactivex.disposables.Disposable
import org.hibernate.Session
import org.quartz.*
import org.quartz.impl.StdSchedulerFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Configuration
class SpringConfiguration {

    Scheduler scheduler
    JobKey notificationJobKey
    Disposable subscription

    @Autowired
    Persistence persistence

    private static final Logger logger = LoggerFactory.getLogger(SpringConfiguration)

    @PostConstruct
    void scheduleJobs() {
        scheduler = StdSchedulerFactory.getDefaultScheduler()
        notificationJobKey = scheduleJob(NotificationsJob, "NotificationsJob", 10, "tt")

        subscription = persistence.status.subscribe({ status ->
            logger.info("Persistence status: ${status}")
        })
    }

    @PreDestroy
    void unscheduleJobs() {
        if(!scheduler.isShutdown()) {
            scheduler.deleteJob(notificationJobKey)
        }
        subscription.dispose()
    }

    JobKey scheduleJob(Class jobClass, String jobName, int pollSecInterval, String jobGroup) {
        try {
            JobDetail job = JobBuilder
                    .newJob(jobClass)
                    .withIdentity(jobName + ".job", jobGroup)
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(jobName + ".trigger", jobGroup)
                    .startAt(DateBuilder.futureDate(pollSecInterval, DateBuilder.IntervalUnit.SECOND))
                    .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(pollSecInterval))
                    .build();

            scheduler.scheduleJob(job, trigger);
            return job.getKey();
        } catch (Exception e) {
            logger.error("Could not schedule " + jobName + " job", e);
            return null
        }
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
