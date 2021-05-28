package com.manydesigns.portofino.quartz;

import org.quartz.Job;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

public class JobRegistration {

    public final Class<? extends Job> jobClass;
    public final TriggerBuilder<? extends Trigger> triggerBuilder;
    public String jobGroup;
    public JobKey key;
    public String jobName;

    public JobRegistration(Class<? extends Job> jobClass, TriggerBuilder<? extends Trigger> triggerBuilder, String jobGroup) {
        this.jobClass = jobClass;
        this.triggerBuilder = triggerBuilder;
        this.jobGroup = jobGroup;
    }

    public JobRegistration(Class<? extends Job> jobClass, TriggerBuilder<? extends Trigger> triggerBuilder) {
        this(jobClass, triggerBuilder, null);
    }
}
