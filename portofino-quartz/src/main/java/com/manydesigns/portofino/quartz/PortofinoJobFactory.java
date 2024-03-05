/*
* Copyright (C) 2005-2024 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.quartz;

import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.di.Injections;
import com.manydesigns.portofino.modules.BaseModule;
import com.manydesigns.portofino.modules.DatabaseModule;
import com.manydesigns.portofino.persistence.Persistence;
import groovy.lang.GroovyObject;
import groovy.util.GroovyScriptEngine;
import org.quartz.*;
import org.quartz.simpl.SimpleJobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.File;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class PortofinoJobFactory extends SimpleJobFactory {
    public static final String copyright =
            "Copyright (C) 2005-2024 ManyDesigns srl";

    private static final Logger logger = LoggerFactory.getLogger(PortofinoJobFactory.class);
    private final ServletContext servletContext;
    @Inject(BaseModule.GROOVY_CLASS_PATH)
    private File groovyClassPath;
    @Inject(BaseModule.GROOVY_SCRIPT_ENGINE)
    private GroovyScriptEngine groovyScriptEngine;

    public PortofinoJobFactory(ServletContext servletContext) {
        this.servletContext = servletContext;
        Injections.inject(this, servletContext, null);
    }

    @Override
    public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
        Job job = instantiateJob(bundle);
        return new Job() {
            @Override
            public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
                try {
                    job.execute(jobExecutionContext);
                } finally {
                    try {
                        //In a different class to make the database module optional at runtime
                        SessionCleaner.closeSessions(servletContext);
                    } catch (NoClassDefFoundError e) {
                        logger.debug("Database module not available, not closing sessions", e);
                    }
                }
            }
        };
    }

    protected Job instantiateJob(TriggerFiredBundle bundle) throws SchedulerException {
        JobDetail jobDetail = bundle.getJobDetail();
        Class jobClass = jobDetail.getJobClass();
        if(GroovyObject.class.isAssignableFrom(jobClass)) {
            //Attempt to reload
            String classFileName = jobClass.getName().replace('.', File.separatorChar) + ".groovy";
            try {
                File groovyFile = new File(groovyClassPath, classFileName);
                Class newClass = groovyScriptEngine.loadScriptByName(groovyFile.toURI().toString());
                if(Job.class.isAssignableFrom(newClass)) {
                    jobClass = newClass;
                } else {
                    logger.error(newClass + " does not extend " + Job.class + ", not reloading the job.");
                }
            } catch (Exception e) {
                logger.error("Failed to reload job class " + jobClass.getName(), e);
            }
        }
        Job job;
        try {
            job = (Job) jobClass.newInstance();
        } catch (Exception e) {
            throw new SchedulerException("Problem instantiating class '" + jobDetail.getJobClass().getName() + "'", e);
        }
        Injections.inject(job, servletContext, null);
        return job;
    }

}

class SessionCleaner {
    static void closeSessions(ServletContext servletContext) {
        Persistence persistence = (Persistence) servletContext.getAttribute(DatabaseModule.PERSISTENCE);
        persistence.closeSessions();
    }
}
