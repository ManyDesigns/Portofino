package com.manydesigns.portofino.shiro;

import org.apache.shiro.web.servlet.ShiroFilter;

import javax.servlet.ServletContext;

public class SelfRegisteringShiroFilter extends ShiroFilter {

    public static final String KEY = SelfRegisteringShiroFilter.class.getName() + "_key";

    @Override
    public void init() throws Exception {
        super.init();
        setContextAttribute(KEY, this);
    }

    @Override
    public void destroy() {
        super.destroy();
        setContextAttribute(KEY, null);
    }

    public static SelfRegisteringShiroFilter get(ServletContext servletContext) {
        return (SelfRegisteringShiroFilter) servletContext.getAttribute(KEY);
    }

}
