package com.manydesigns.portofino.methods;

import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.apache.log4j.LogMF;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletContext;

import com.manydesigns.portofino.base.context.MDContext;
import com.manydesigns.portofino.base.context.ServletContextInfo;


public class PortofinoServletContextListener implements ServletContextListener {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public static final String SEPARATOR =
            "----------------------------------------" +
            "----------------------------------------";

    public static final String PORTOFINO_VERSION = "4.0.0-SNAPSHOT";


    public static final String SERVLET_CONTEXT_INFO_ATTRIBUTE =
            "servletContextInfo";
    public static final String MDCONTEXT_ATTRIBUTE =
            "mdContext";

    protected final Logger logger;
    protected ServletContext servletContext;
    protected ServletContextInfo servletContextInfo;
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
        servletContextInfo = new ServletContextInfo(servletContext);

        boolean success = true;

        // check servlet API version
        if (servletContextInfo.getServletApiMajor() < 2 ||
                (servletContextInfo.getServletApiMajor() == 2 &&
                        servletContextInfo.getServletApiMinor() < 3)) {
            LogMF.fatal(logger,
                    "Servlet API version must be >= 2.3. Found: {0}.",
                    new String[] {servletContextInfo.getServletApiVersion()});
            success = false;
        }

        if (success) {
            logger.info("Creating MDContext and " +
                    "registering on servlet context...");
            // create and register the container first, without exceptions
            mdContext = new MDContext();
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
