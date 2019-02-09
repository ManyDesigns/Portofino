import com.manydesigns.portofino.persistence.Persistence
import com.manydesigns.portofino.tt.NotificationsJob
import com.manydesigns.portofino.tt.TtUtils
import org.hibernate.Session
import org.quartz.*
import org.quartz.impl.StdSchedulerFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Configuration
class SpringConfiguration {

    Scheduler scheduler
    JobKey notificationJobKey

    @Autowired
    Persistence persistence

    private static final Logger logger = LoggerFactory.getLogger(SpringConfiguration)

    @PostConstruct
    void scheduleJobs() {
        scheduler = StdSchedulerFactory.getDefaultScheduler()
        notificationJobKey = scheduleJob(NotificationsJob, "NotificationsJob", 10, "tt")

        persistence.status.subscribe({ status ->
            logger.info("Persistence status: ${status}")
        })
    }

    @PreDestroy
    void unscheduleJobs() {
        scheduler.deleteJob(notificationJobKey)
    }

    JobKey scheduleJob(Class clazz, String jobName, int pollSecInterval, String jobGroup) {
        try {
            JobDetail job = JobBuilder
                    .newJob(clazz)
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

}
