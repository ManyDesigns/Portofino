package com.manydesigns.portofino.resteasy;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import javax.servlet.*;
import java.io.IOException;

public class ResteasyCleanupFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
//        request.getServletContext().removeAttribute(ResteasyProviderFactory.class.getName());
//        request.getServletContext().removeAttribute(Dispatcher.class.getName());
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}

}
