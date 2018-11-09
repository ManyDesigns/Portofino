import com.manydesigns.portofino.tt.NotificationsJob
import org.quartz.DateBuilder
import org.quartz.JobBuilder
import org.quartz.JobDetail
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.SimpleScheduleBuilder
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.quartz.impl.StdSchedulerFactory
import org.springframework.context.annotation.Configuration

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Configuration
class SpringConfiguration {

    Scheduler scheduler
    JobKey notificationJobKey

    @PostConstruct
    void scheduleJobs() {
        scheduler = StdSchedulerFactory.getDefaultScheduler()
        notificationJobKey = scheduleJob(NotificationsJob, "NotificationsJob", 10, "tt")
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