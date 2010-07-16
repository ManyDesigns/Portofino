package com.manydesigns.portofino.methods;

import com.manydesigns.portofino.base.context.MDContext;
import com.manydesigns.portofino.base.context.ServerInfo;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


public class PortofinoServletContextListener implements ServletContextListener {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public static final String SEPARATOR =
            "----------------------------------------" +
            "----------------------------------------";

    public static final String PORTOFINO_VERSION = "4.0.0-SNAPSHOT";

//    public static final String MODEL_LOCATION = "portofino-model.xml";
    public static final String MODEL_LOCATION =
        "databases/jpetstore/postgresql/jpetstore-postgres.xml";

    public static final String SERVER_INFO_ATTRIBUTE =
            "serverInfo";
    public static final String PORTOFINO_VERSION_ATTRIBUTE =
            "portofinoVersion";
    public static final String MDCONTEXT_ATTRIBUTE =
            "mdContext";

    protected final Logger logger;
    protected ServletContext servletContext;
    protected ServerInfo serverInfo;
    protected MDContext mdContext;

    /**
     * Creates a new instance of PortofinoServletContextListener
     */
    public PortofinoServletContextListener() {
        logger = Logger.getLogger(PortofinoServletContextListener.class);
    }

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        LogMF.info(logger, "\n" + SEPARATOR +
                "\n--- ManyDesigns Portofino {0} starting..." +
                "\n" + SEPARATOR, PORTOFINO_VERSION);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();


        servletContext = servletContextEvent.getServletContext();
        serverInfo = new ServerInfo(servletContext);
        
        servletContext.setAttribute(SERVER_INFO_ATTRIBUTE,
                serverInfo);
        servletContext.setAttribute(PORTOFINO_VERSION_ATTRIBUTE,
                PORTOFINO_VERSION);

        boolean success = true;

        // check servlet API version
        if (serverInfo.getServletApiMajor() < 2 ||
                (serverInfo.getServletApiMajor() == 2 &&
                        serverInfo.getServletApiMinor() < 3)) {
            LogMF.fatal(logger,
                    "Servlet API version must be >= 2.3. Found: {0}.",
                    new String[] {serverInfo.getServletApiVersion()});
            success = false;
        }

        if (success) {
            logger.info("Creating MDContext and " +
                    "registering on servlet context...");
            // create and register the container first, without exceptions
            mdContext = new MDContext();
            mdContext.loadXmlModelAsResource(MODEL_LOCATION);
            servletContext.setAttribute(MDCONTEXT_ATTRIBUTE, mdContext);
        }

        stopWatch.stop();
        if (success) {
            LogMF.info(logger,
                    "ManyDesigns Portofino successfully started in {0} ms.",
                    stopWatch.getTime());
        } else {
            logger.fatal("Failed to start ManyDesigns Portofino.");
        }
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        logger.info("ManyDesigns Portofino stopping...");

        logger.info("Unregistering MDContext from servlet context...");
        servletContext.removeAttribute(MDCONTEXT_ATTRIBUTE);

        logger.info("ManyDesigns Portofino stopped.");
    }

}
