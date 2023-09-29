package com.manydesigns.elements.servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import java.util.Enumeration;

/**
 * Created by alessio on 10/31/17.
 */
public class MutableHttpSession implements HttpSession {

    protected final ServletContext servletContext;

    public MutableHttpSession(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public long getCreationTime() {
        return 0;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public long getLastAccessedTime() {
        return 0;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public void setMaxInactiveInterval(int i) {

    }

    @Override
    public int getMaxInactiveInterval() {
        return 0;
    }

    @Override
    public Object getAttribute(String s) {
        return null;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return null;
    }

    @Override
    public void setAttribute(String s, Object o) {}

    @Override
    public void removeAttribute(String s) {

    }

    @Override
    public void invalidate() {}

    @Override
    public boolean isNew() {
        return false;
    }
}
