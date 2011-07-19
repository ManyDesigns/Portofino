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
import com.manydesigns.elements.util.InstanceBuilder;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.context.Application;
import com.manydesigns.portofino.context.ServerInfo;
import com.manydesigns.portofino.context.hibernate.HibernateApplicationImpl;
import com.manydesigns.portofino.dispatcher.Dispatcher;
import com.manydesigns.portofino.email.EmailTask;
import com.sun.xml.bind.v2.ClassFactory;
import com.sun.xml.bind.v2.runtime.Coordinator;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.io.File;
import java.lang.reflect.Field;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Timer;


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

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected Properties elementsProperties;
    protected Properties portofinoProperties;
    protected ServletContext servletContext;
    protected ServerInfo serverInfo;
    protected Application application;
    protected Dispatcher dispatcher;
    protected Timer scheduler;

    public static final Logger logger =
            LoggerFactory.getLogger(PortofinoListener.class);
    private static final int PERIOD = 10000;
    private static final int DELAY = 5000;
    private static final int DELAY2 = 5300;

    //**************************************************************************
    // ServletContextListener implementation
    //**************************************************************************

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        // clear the Mapping Diagnostic Context for logging
        MDC.clear();
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        elementsProperties = ElementsProperties.getProperties();
        portofinoProperties = PortofinoProperties.getProperties();

        servletContext = servletContextEvent.getServletContext();
        serverInfo = new ServerInfo(servletContext);

        logger.info("\n" + SEPARATOR +
                "\n--- ManyDesigns Portofino {} starting..." +
                "\n--- Context path: {}" +
                "\n--- Real path: {}" +
                "\n" + SEPARATOR,
                new String[] {
                        portofinoProperties.getProperty(
                                PortofinoProperties.PORTOFINO_VERSION_PROPERTY),
                        serverInfo.getContextPath(),
                        serverInfo.getRealPath()
                }
        );

        servletContext.setAttribute(ServerInfo.KEY,serverInfo);

        servletContext.setAttribute(
                ElementsProperties.KEY, elementsProperties);
        servletContext.setAttribute(
                PortofinoProperties.KEY, portofinoProperties);

        boolean success = true;

        // check servlet API version
        if (serverInfo.getServletApiMajor() < 2 ||
                (serverInfo.getServletApiMajor() == 2 &&
                        serverInfo.getServletApiMinor() < 3)) {
            logger.error("Servlet API version must be >= 2.3. Found: {}.",
                    serverInfo.getServletApiVersion());
            success = false;
        }

        if (success) {
            createContext();
            createDispatcher();
        }

        if (success) {
            String securityType = portofinoProperties
                    .getProperty(PortofinoProperties.SECURITY_TYPE_PROPERTY, "application");
            String mailHost = portofinoProperties
                    .getProperty(PortofinoProperties.MAIL_SMTP_HOST);
            String mailSender = portofinoProperties
                    .getProperty(PortofinoProperties.MAIL_SMTP_SENDER);
            Boolean mailEnabled = Boolean.parseBoolean(portofinoProperties
                    .getProperty(PortofinoProperties.MAIL_ENABLED, "false"));
            if ("application".equals(securityType)&&mailEnabled)
            {
                if (null==mailSender
                        || null == mailHost ) {
                    logger.info("User admin email or smtp server not set in" +
                            " portofino-custom.properties");
                } else {
                    scheduler = new java.util.Timer(true);
                    try {

                        //Invio mail
                        scheduler.schedule(new EmailTask(application),
                                DELAY2, PERIOD);
                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.error("Problems in starting schedulers", e);
                    }
                }
            }
        }
        stopWatch.stop();
        if (success) {
            logger.info("ManyDesigns Portofino successfully started in {} ms.",
                    stopWatch.getTime());
        } else {
            logger.error("Failed to start ManyDesigns Portofino.");
        }
    }

    public void createDispatcher() {
        dispatcher = new Dispatcher(application);
        servletContext.setAttribute(Dispatcher.KEY, dispatcher);
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        // clear the Mapping Diagnostic Context for logging
        MDC.clear();

        logger.info("ManyDesigns Portofino stopping...");

        if (scheduler!=null) {
            logger.info("Terminating the scheduler...");
            scheduler.cancel();
            EmailTask.stop();
        }

        // clean up ThreadLocals and possible memory leaks


        clearJaxbThreadLocal();

        Enumeration<Driver> en = DriverManager.getDrivers();
        while ( en.hasMoreElements() )
            try {
                Driver driver = en.nextElement();
                if ( driver.getClass().getClassLoader() == getClass().getClassLoader() )
                    DriverManager.deregisterDriver(driver);
            } catch ( SQLException e1 ) {
                
            }

        ElementsThreadLocals.destroy();
        System.gc();


        try {
            application.stopFileManager();
        } catch (Exception e) {
            logger.warn("cannot stop FileManager");
        }

        logger.info("ManyDesigns Portofino stopped.");
    }

    public static void clearJaxbThreadLocal() {
        try {
            Field f = ClassFactory.class.getDeclaredField( "tls" );
            f.setAccessible( true );
            ( (ThreadLocal<?>) f.get( null ) ).set( null );
            f = Coordinator.class.getDeclaredField( "activeTable" );
            f.setAccessible( true );
            ( (ThreadLocal<?>) f.get( null ) ).set( null );
        } catch ( Throwable e ) {
            logger.error( "Failed to plug thread local leaks of jaxb." );
            logger.trace( "Stack trace:", e );
        }
    }

    public static void clearJaxb() {
        try {
            Field f = ClassFactory.class.getDeclaredField( "tls" );
            f.setAccessible( true );
            ( (ThreadLocal<?>) f.get( null ) ).remove();
            f = Coordinator.class.getDeclaredField( "activeTable" );
            f.setAccessible( true );
            ( (ThreadLocal<?>) f.get( null ) ).remove();
        } catch ( Throwable e ) {
            logger.error( "Failed to plug thread local leaks of jaxb." );
            logger.trace( "Stack trace:", e );
        }
    }

    public void setFieldValue(String className, String fieldName, Object obj, Object fieldValue) {
        Field field = getFieldByReflection(className, fieldName);
        if (field == null) {
            return;
        }
        try {
            field.set(null, fieldValue);
        } catch (Throwable e) {
            e.printStackTrace();
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
            e.printStackTrace();
            return null;
        }
    }

    
    //**************************************************************************
    // HttpSessionListener implementation
    //**************************************************************************

    public void sessionCreated(HttpSessionEvent httpSessionEvent) {
        HttpSession session = httpSessionEvent.getSession();
        logger.info("Session created: id={}", session.getId());
    }

    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        HttpSession session = httpSessionEvent.getSession();
        logger.info("Session destroyed: id={}", session.getId());
    }

    protected void createContext() {
        logger.info("Creating Context and " +
                "registering on servlet context...");
        // create and register the container first, without exceptions

        try {
            ElementsThreadLocals.setupDefaultElementsContext();

            String managerClassName =
                    portofinoProperties.getProperty(
                            PortofinoProperties.CONTEXT_CLASS_PROPERTY);
            InstanceBuilder<Application> builder =
                    new InstanceBuilder<Application>(
                            Application.class,
                            HibernateApplicationImpl.class,
                            logger);
            application = builder.createInstance(managerClassName);
            servletContext.setAttribute(Application.KEY, application);

            String storeDir = FilenameUtils.normalize(portofinoProperties.getProperty(
                PortofinoProperties.PORTOFINO_STOREDIR_PROPERTY));
            String workDir = FilenameUtils.normalize(portofinoProperties.getProperty(
                PortofinoProperties.PORTOFINO_WORKDIR_PROPERTY));

            String connectionsFileName =
                    portofinoProperties.getProperty(
                            PortofinoProperties.CONNECTION_FILE_PROPERTY);
            String modelLocation =
                    portofinoProperties.getProperty(
                            PortofinoProperties.MODEL_LOCATION_PROPERTY);

            String rootDirPath = servletContext.getRealPath("/");
            File modelFile;
            if (rootDirPath == null) {
                modelFile = new File(modelLocation);
            } else {

                modelFile = new File (rootDirPath, modelLocation);
            }

            if(FilenameUtils.getPrefixLength(storeDir)==-1
                    || FilenameUtils.getPrefixLength(storeDir)==0){
                storeDir = FilenameUtils.concat(rootDirPath, storeDir);
            }
            if(FilenameUtils.getPrefixLength(workDir)==-1
                    || FilenameUtils.getPrefixLength(storeDir)==0){
                workDir = FilenameUtils.concat(rootDirPath, workDir);
            }
            logger.info("Storing directory:" + storeDir);
            logger.info("Working directory:" + workDir);
            application.createFileManager(storeDir, workDir);

            application.startFileManager();



            application.loadConnections(connectionsFileName);
            application.loadXmlModel(modelFile);


        } catch (Throwable e) {
            logger.error(ExceptionUtils.getRootCauseMessage(e), e);
        } finally {
            ElementsThreadLocals.removeElementsContext();
        }
    }
}
