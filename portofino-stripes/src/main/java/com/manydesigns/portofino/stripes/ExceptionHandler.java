package com.manydesigns.portofino.stripes;

import net.sourceforge.stripes.exception.DefaultExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ExceptionHandler extends DefaultExceptionHandler {

    public void handle(StackOverflowError ex, HttpServletRequest req, HttpServletResponse resp) {
        throw new RuntimeException("Stack Overflow! Exception swallowed to prevent log flooding. Some context: " + req.getRequestURI() + (req.getQueryString() != null ? req.getQueryString() : ""));
    }

}
