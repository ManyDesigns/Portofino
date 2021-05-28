import com.manydesigns.portofino.persistence.Persistence
import com.manydesigns.portofino.quartz.JobRegistration
import com.manydesigns.portofino.quartz.SchedulerService
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

    Disposable subscription

    @Autowired
    Persistence persistence

    @Autowired
    SchedulerService schedulerService

    private static final Logger logger = LoggerFactory.getLogger(SpringConfiguration)

    @PostConstruct
    void init() {
        int pollSecInterval = 10
        def schedule = TriggerBuilder.newTrigger()
                .startAt(DateBuilder.futureDate(pollSecInterval, DateBuilder.IntervalUnit.SECOND))
                .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(pollSecInterval))
        schedulerService.register(new JobRegistration(NotificationsJob, schedule, "tt"))

        subscription = persistence.status.subscribe({ status ->
            logger.info("Persistence status: ${status}")
        })
    }

    @PreDestroy
    void destroy() {
        subscription.dispose()
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
