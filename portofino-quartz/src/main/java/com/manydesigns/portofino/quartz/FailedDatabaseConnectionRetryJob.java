package com.manydesigns.portofino.quartz;

import com.manydesigns.portofino.persistence.Persistence;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

public class FailedDatabaseConnectionRetryJob implements Job {

    @Autowired
    Persistence persistence;

    @Override
    public void execute(JobExecutionContext context) {
        if(Persistence.Status.STARTED == persistence.status.getValue()) {
            persistence.retryFailedConnections();
        }
    }
}
