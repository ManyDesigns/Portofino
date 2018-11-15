package com.manydesigns.portofino.servlets;

import com.manydesigns.portofino.rest.PortofinoFilter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Set;

public class CorsServletContainerInitializer implements ServletContainerInitializer {

    private static final Logger logger = LoggerFactory.getLogger(CorsServletContainerInitializer.class);
    public static final String TOMCAT_CORS_FILTER = "org.apache.catalina.filters.CorsFilter";
    public static final String JETTY_CORS_FILTER = "org.eclipse.jetty.servlets.CrossOriginFilter";
    public static final String DEFAULT_ALLOWED_HEADERS =
            "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, " +
            "Access-Control-Request-Headers, Authorization, Accept-Encoding, Accept-Language, " +
            "Cache-Control, Connection, Host, Referer, User-Agent";
    public static final String DEFAULT_EXPOSED_HEADERS =
            "X-Portofino-Pretty-Name, " +  //TODO this is used by the CRUD action. Maybe these should be configurable by modules somehow?
            PortofinoFilter.MESSAGE_HEADER;

    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
        if(ctx.getInitParameter("cors.configured.externally") != null) {
            logger.info("CORS filter configured externally.");
            return;
        }
        String allowedHeaders = ctx.getInitParameter("cors.allowed.headers");
        if(allowedHeaders == null) {
            allowedHeaders = DEFAULT_ALLOWED_HEADERS;
        }
        String exposedHeaders = ctx.getInitParameter("cors.exposed.headers");
        if(exposedHeaders == null) {
            exposedHeaders = DEFAULT_EXPOSED_HEADERS;
        }
        String allowedOrigins = ctx.getInitParameter("cors.allowed.origins");
        String allowedMethods = "DELETE, GET, POST, PUT";
        boolean corsFilterInstalled = false;
        try {
            Class.forName(TOMCAT_CORS_FILTER);
            FilterRegistration.Dynamic corsFilter = ctx.addFilter("corsFilter", TOMCAT_CORS_FILTER);
            logger.info("Installing Tomcat CORS filter");
            corsFilter.setInitParameter("cors.allowed.headers", allowedHeaders);
            corsFilter.setInitParameter("cors.exposed.headers", exposedHeaders);
            corsFilter.setInitParameter("cors.allowed.methods", allowedMethods);
            if(!StringUtils.isEmpty(allowedOrigins)) {
                corsFilter.setInitParameter("cors.allowed.origins", allowedOrigins);
            }
            corsFilter.addMappingForUrlPatterns(null, false, "/*");
            corsFilterInstalled = true;
        } catch (ClassNotFoundException e) {
            logger.debug("Tomcat CORS filter not available.", e);
        }
        try {
            Class.forName(JETTY_CORS_FILTER);
            FilterRegistration.Dynamic corsFilter = ctx.addFilter("corsFilter", JETTY_CORS_FILTER);
            logger.info("Installing Jetty CORS filter");
            corsFilter.setInitParameter("allowedHeaders", allowedHeaders);
            corsFilter.setInitParameter("exposedHeaders", exposedHeaders);
            corsFilter.setInitParameter("allowedMethods", allowedMethods);
            if(!StringUtils.isEmpty(allowedOrigins)) {
                corsFilter.setInitParameter("allowedOrigins", allowedOrigins);
            }
            corsFilter.addMappingForUrlPatterns(null, false, "/*");
            corsFilterInstalled = true;
        } catch (ClassNotFoundException e) {
            logger.debug("Jetty CORS filter not available.", e);
        }
        if(!corsFilterInstalled) {
            logger.warn("No CORS Filter configured. Make sure you configure CORS for your server if you're accessing the API from a browser on a different domain.");
        }
    }
}
