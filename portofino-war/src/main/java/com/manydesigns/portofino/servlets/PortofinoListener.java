/*
 * Copyright (C) 2005-2010 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.servlets;

import com.manydesigns.elements.ElementsProperties;
import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.elements.util.InstanceBuilder;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.context.Context;
import com.manydesigns.portofino.context.ServerInfo;
import com.manydesigns.portofino.context.hibernate.HibernateContextImpl;
import com.manydesigns.portofino.email.EmailTask;
import com.manydesigns.portofino.email.PasswordGenerator;
import org.apache.commons.lang.time.StopWatch;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.Timer;
import java.util.logging.Logger;


/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class PortofinoListener
        implements ServletContextListener, HttpSessionListener {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************

    public static final String SEPARATOR =
            "----------------------------------------" +
            "----------------------------------------";

    public static final String ELEMENTS_PROPERTIES_ATTRIBUTE =
            "elementsProperties";
    public static final String PORTOFINO_PROPERTIES_ATTRIBUTE =
            "portofinoProperties";
    public static final String SERVER_INFO_ATTRIBUTE =
            "serverInfo";
    public static final String CONTEXT_ATTRIBUTE =
            "context";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected Properties elementsProperties;
    protected Properties portofinoProperties;
    protected ServletContext servletContext;
    protected ServerInfo serverInfo;
    protected Context context;
    protected Timer scheduler;

    public static final Logger logger =
            LogUtil.getLogger(PortofinoListener.class);
    private static final int PERIOD = 10000;
    private static final int DELAY = 5000;
    private static final int DELAY2 = 5300;

    //**************************************************************************
    // ServletContextListener implementation
    //**************************************************************************

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        LogUtil.initializeLoggingSystem();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        elementsProperties = ElementsProperties.getProperties();
        portofinoProperties = PortofinoProperties.getProperties();

        servletContext = servletContextEvent.getServletContext();
        serverInfo = new ServerInfo(servletContext);

        LogUtil.infoMF(logger, "\n" + SEPARATOR +
                "\n--- ManyDesigns Portofino {0} starting..." +
                "\n--- Context path: {1}" +
                "\n--- Real path: {2}" +
                "\n" + SEPARATOR,
                portofinoProperties.getProperty(
                        PortofinoProperties.PORTOFINO_VERSION_PROPERTY),
                serverInfo.getContextPath(),
                serverInfo.getRealPath()
        );

        servletContext.setAttribute(SERVER_INFO_ATTRIBUTE,
                serverInfo);

        servletContext.setAttribute(ELEMENTS_PROPERTIES_ATTRIBUTE,
                elementsProperties);
        servletContext.setAttribute(PORTOFINO_PROPERTIES_ATTRIBUTE,
                portofinoProperties);

        boolean success = true;

        // check servlet API version
        if (serverInfo.getServletApiMajor() < 2 ||
                (serverInfo.getServletApiMajor() == 2 &&
                        serverInfo.getServletApiMinor() < 3)) {
            LogUtil.severeMF(logger,
                    "Servlet API version must be >= 2.3. Found: {0}.",
                    serverInfo.getServletApiVersion());
            success = false;
        }
        if (success) {
            createContext();
        }
        if (success) {
            String securityType = (String) portofinoProperties
                    .getProperty("security.type", "application");
            String mailHost = (String) portofinoProperties
                    .getProperty("mail.smtp.host");
            String mailSender = (String) portofinoProperties
                    .getProperty("mail.smtp.sender");
            if ("application".equals(securityType))
            {
                if (null==mailSender
                        || null == mailHost ) {
                    LogUtil.infoMF(logger, "User admin email or smtp server not set in" +
                            " portofino-custom.properties");
                } else {
                    scheduler = new java.util.Timer(true);
                    //Password generator
                    scheduler.schedule(new PasswordGenerator(context), DELAY, PERIOD);
                    try {

                        //Invio mail
                        scheduler.schedule(new EmailTask(context),
                                DELAY2, PERIOD);
                    } catch (Exception e) {
                        e.printStackTrace();
                        LogUtil.severe(logger, "Problems in starting schedulers", e);
                    }
                }
            }
        }
        stopWatch.stop();
        if (success) {
            LogUtil.infoMF(logger,
                    "ManyDesigns Portofino successfully started in {0} ms.",
                    stopWatch.getTime());
        } else {
            logger.severe("Failed to start ManyDesigns Portofino.");
        }
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        logger.info("ManyDesigns Portofino stopping...");

        if (scheduler!=null) {
            logger.info("Terminating the scheduler...");
            scheduler.cancel();
            EmailTask.stop();
        }

        // clean up ThreadLocals and possible memory leaks
        ElementsThreadLocals.destroy();
        setFieldValue("org.apache.commons.lang.builder.ToStringStyle", "registry", null, null);
        setFieldValue("org.apache.struts2.config.Settings", "settingsImpl", null, null);
        setFieldValue("org.apache.struts2.config.Settings", "defaultImpl", null, null);
        setFieldValue("org.apache.struts2.dispatcher.Dispatcher", "instance", null, null);
        setFieldValue("org.apache.struts2.dispatcher.Dispatcher", "dispatcherListeners", null, null);
        setFieldValue("com.opensymphony.xwork2.ActionContext", "actionContext", null, null);
        System.gc();

        // remove attributes from application context
        servletContext.removeAttribute(CONTEXT_ATTRIBUTE);
        servletContext.removeAttribute(SERVER_INFO_ATTRIBUTE);
        servletContext.removeAttribute(PORTOFINO_PROPERTIES_ATTRIBUTE);
        servletContext.removeAttribute(ELEMENTS_PROPERTIES_ATTRIBUTE);

        logger.info("ManyDesigns Portofino stopped.");
    }

    public void setFieldValue(String className, String fieldName, Object obj, Object fieldValue) {
        Field field = getFieldByReflection(className, fieldName);
        if (field == null) {
            return;
        }
        try {
            field.set(null, fieldValue);
        } catch (Throwable e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public Field getFieldByReflection(String className, String fieldName) {
        try {
            ClassLoader cl = getClass().getClassLoader();
            Class aClass = cl.loadClass(className);
            Field field = aClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (Throwable e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return null;
        }
    }

    
    //**************************************************************************
    // HttpSessionListener implementation
    //**************************************************************************

    public void sessionCreated(HttpSessionEvent httpSessionEvent) {
        HttpSession session = httpSessionEvent.getSession();
        LogUtil.infoMF(logger, "Session created: id={0}",
                session.getId());
    }

    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        HttpSession session = httpSessionEvent.getSession();
        LogUtil.infoMF(logger, "Session destroyed: id={0}", session.getId());
    }

    protected void createContext() {
        logger.info("Creating Context and " +
                "registering on servlet context...");
        // create and register the container first, without exceptions

        ElementsThreadLocals.setupDefaultElementsContext();

        String managerClassName =
                portofinoProperties.getProperty(
                        PortofinoProperties.CONTEXT_CLASS_PROPERTY);
        InstanceBuilder<Context> builder =
                new InstanceBuilder<Context>(
                        Context.class,
                        HibernateContextImpl.class,
                        logger);
        context = builder.createInstance(managerClassName);

        servletContext.setAttribute(CONTEXT_ATTRIBUTE, context);

        context.loadConnectionsAsResource(
                portofinoProperties.getProperty(
                        PortofinoProperties.CONNECTIONS_LOCATION_PROPERTY));
        context.loadXmlModelAsResource(
                portofinoProperties.getProperty(
                        PortofinoProperties.MODEL_LOCATION_PROPERTY));

        ElementsThreadLocals.removeElementsContext();
    }
}
