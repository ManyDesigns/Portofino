package com.manydesigns.portofino.quartz;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashSet;
import java.util.Set;

public class SchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);

    @Autowired(required = false)
    public Set<JobRegistration> jobs = new HashSet<>();

    protected final Scheduler scheduler;
    protected volatile boolean started;

    public SchedulerService(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public synchronized void register(JobRegistration job) {
        this.jobs.add(job);
        if(started) {
            schedule(job);
        }
    }

    @PostConstruct
    public synchronized void schedule() {
        jobs.forEach(this::schedule);
        started = true;
    }

    public synchronized void schedule(JobRegistration j) {
        j.jobName = "Job " + j.jobClass.getSimpleName();
        JobDetail job = JobBuilder.newJob(j.jobClass).withIdentity(j.jobName).build();
        try {
            scheduler.scheduleJob(job, j.triggerBuilder.withIdentity("Trigger for " + j.jobName).build());
            j.key = job.getKey();
            logger.info("Scheduled " + j.jobName);
        } catch (SchedulerException e) {
            logger.error("Could not schedule " + j.jobName, e);
        }
    }

    public synchronized void unschedule(JobRegistration registration) {
        try {
            if(started) {
                scheduler.deleteJob(registration.key);
            }
            jobs.remove(registration);
            registration.key = null;
        } catch (SchedulerException e) {
            logger.error("Could not unschedule " + registration.jobName, e);
        }
    }

    @PreDestroy
    public synchronized void unschedule() {
        jobs.forEach(j -> {
            try {
                if(!scheduler.isShutdown()) {
                    scheduler.deleteJob(j.key);
                    logger.info("Deleted " + j.jobName);
                }
            } catch (SchedulerException e) {
                logger.error("Could not delete " + j.jobName, e);
            }
        });
    }

    public Scheduler getScheduler() {
        return scheduler;
    }
}
