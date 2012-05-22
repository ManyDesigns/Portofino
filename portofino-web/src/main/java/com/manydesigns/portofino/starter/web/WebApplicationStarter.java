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

package com.manydesigns.portofino.starter.web;

import com.manydesigns.portofino.scripting.ScriptingUtil;
import com.manydesigns.portofino.starter.ApplicationStarter;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class WebApplicationStarter extends ApplicationStarter {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    private final static Logger logger =
            LoggerFactory.getLogger(WebApplicationStarter.class);

    public WebApplicationStarter(Configuration portofinoConfiguration) {
        super(portofinoConfiguration);
    }

    protected ApplicationListener applicationListener;

    @Override
    public boolean setupApplication() {
        boolean success = super.setupApplication();
        if(success) {
            logger.info("Initializing base classloader for application {}", tmpApplication.getAppId());
            ScriptingUtil.initBaseClassLoader(tmpApplication);
            File appListenerFile = new File(tmpApplication.getAppScriptsDir(), "AppListener.groovy");
            try {
                Object o = ScriptingUtil.getGroovyObject(appListenerFile);
                if(o != null) {
                    if(o instanceof ApplicationListener) {
                        applicationListener = (ApplicationListener) o;
                        logger.info("Invoking application listener defined in {}", appListenerFile.getAbsolutePath());
                        success = applicationListener.applicationStarting(tmpApplication);
                    } else {
                        logger.error("Candidate app listener " + o +
                                     " is not an instance of " + ApplicationListener.class);
                        success = false;
                    }
                } else {
                    logger.debug("No app listener present");
                }
            } catch (Throwable e) {
                logger.error("Could not invoke app listener", e);
                success = false;
            }
            if(!success) {
                //Clean up
                logger.info("Removing base classloader for application {}", tmpApplication.getAppId());
                ScriptingUtil.removeBaseClassLoader(tmpApplication);
            }
        }
        return success;
    }

    @Override
    protected void destroyApplication() {
        super.destroyApplication();
        if(applicationListener != null) {
            try {
                applicationListener.applicationDestroying(application);
            } catch (Throwable t) {
                logger.error("Application listener threw an exception during shutdown", t);
            }
        }
        logger.info("Removing base classloader for application {}", application.getAppId());
        ScriptingUtil.removeBaseClassLoader(application);
    }
}
