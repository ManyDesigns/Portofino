package com.manydesigns.portofino.servlets;

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
    public static final String TOMCAT_CORS_ENABLED = "cors.enabled";
    public static final String DEFAULT_ALLOWED_HEADERS =
            "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, " +
                    "Access-Control-Request-Headers, Authorization, Accept-Encoding, Accept-Language, " +
                    "Cache-Control, Connection, Host, Referer, User-Agent";

    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
        if (Boolean.parseBoolean(ctx.getInitParameter(TOMCAT_CORS_ENABLED))) {
            logger.info("Tomcat CORS filter  enabled");
            try {
                Class.forName(TOMCAT_CORS_FILTER);
                FilterRegistration.Dynamic corsFilter = ctx.addFilter("corsFilter", TOMCAT_CORS_FILTER);
                logger.info("Installing Tomcat CORS filter");
                String allowedHeaders = ctx.getInitParameter("cors.allowed.headers");
                if (allowedHeaders == null) {
                    allowedHeaders = DEFAULT_ALLOWED_HEADERS;
                }
                corsFilter.setInitParameter("cors.allowed.headers", allowedHeaders);
                corsFilter.setInitParameter("cors.allowed.methods", "DELETE, GET, POST, PUT");
                String allowedOrigins = ctx.getInitParameter("cors.allowed.origins");
                if (!StringUtils.isEmpty(allowedOrigins)) {
                    corsFilter.setInitParameter("cors.allowed.origins", allowedOrigins);
                }
                corsFilter.addMappingForUrlPatterns(null, false, "*");
            } catch (ClassNotFoundException e) {
                logger.info("Tomcat CORS filter not available");
            }
        } else {
            logger.info("Tomcat CORS filter not enabled");
        }
    }
}
