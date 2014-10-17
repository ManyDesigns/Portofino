package com.manydesigns.portofino.stripes;

import net.sourceforge.stripes.exception.DefaultExceptionHandler;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ExceptionHandler extends DefaultExceptionHandler {

    public void handle(StackOverflowError ex, HttpServletRequest req, HttpServletResponse resp) {
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
            contextInfo += " Forward string: " + req.getAttribute(RequestDispatcher.FORWARD_SERVLET_PATH);
        } catch (Throwable e) {}
        throw new RuntimeException("Stack Overflow! Exception swallowed to prevent log flooding." + contextInfo) {
            @Override
            public Throwable fillInStackTrace() {
                return this;
            }
        };
    }

}
