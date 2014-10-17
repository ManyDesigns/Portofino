package com.manydesigns.portofino.stripes;

import net.sourceforge.stripes.exception.DefaultExceptionHandler;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ExceptionHandler extends DefaultExceptionHandler {

    public void handle(StackOverflowError ex, HttpServletRequest req, HttpServletResponse resp) {
        throw new RuntimeException(
                "Stack Overflow! Exception swallowed to prevent log flooding." +
                        " Request URL: " + req.getRequestURL() +
                        " Query string: " + req.getQueryString() +
                        " Include path: " +  req.getAttribute(RequestDispatcher.INCLUDE_SERVLET_PATH) +
                        " Forward string: " + req.getAttribute(RequestDispatcher.FORWARD_SERVLET_PATH)) {
            @Override
            public Throwable fillInStackTrace() {
                return this;
            }
        };
    }

}
