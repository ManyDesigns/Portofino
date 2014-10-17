package com.manydesigns.portofino.stripes;

import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.exception.DefaultExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ExceptionHandler extends DefaultExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    public Resolution handle(StackOverflowError ex, HttpServletRequest req, HttpServletResponse resp) {
        String contextInfo = " Request: " + req;
        if(req instanceof ServletRequestWrapper) {
            contextInfo += " Wrapped request: " + ((ServletRequestWrapper) req).getRequest();
        }
        try {
            contextInfo += " Request URL: " + req.getRequestURL();
        } catch (Throwable e) {}
        try {
            contextInfo += " Query string: " + req.getQueryString();
        } catch (Throwable e) {}
        try {
            contextInfo += " Include path: " + req.getAttribute(RequestDispatcher.INCLUDE_SERVLET_PATH);
        } catch (Throwable e) {}
        try {
            contextInfo += " Forward path: " + req.getAttribute(RequestDispatcher.FORWARD_SERVLET_PATH);
        } catch (Throwable e) {}
        logger.error("Stack Overflow! Exception swallowed to prevent log flooding." + contextInfo);
        return new ErrorResolution(500);
    }

}
