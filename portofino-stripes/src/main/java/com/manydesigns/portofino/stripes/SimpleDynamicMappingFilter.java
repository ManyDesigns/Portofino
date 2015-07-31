package com.manydesigns.portofino.stripes;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.controller.DispatcherServlet;
import net.sourceforge.stripes.controller.DynamicMappingFilter;
import net.sourceforge.stripes.util.HttpUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Simpler and more efficient DynamicMappingFilter that assumes the way Portofino uses Stripes to save work.
 *
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class SimpleDynamicMappingFilter extends DynamicMappingFilter {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    private DispatcherServlet stripesDispatcher;

    public void init(final FilterConfig config) throws ServletException {
        super.init(config);
        this.stripesDispatcher = new DispatcherServlet();
        this.stripesDispatcher.init(new ServletConfig() {
            public String getInitParameter(String name) {
                return config.getInitParameter(name);
            }

            public Enumeration<String> getInitParameterNames() {
                return config.getInitParameterNames();
            }

            public ServletContext getServletContext() {
                return config.getServletContext();
            }

            public String getServletName() {
                return config.getFilterName();
            }
        });
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // Look for an ActionBean that is mapped to the URI
        String uri = HttpUtil.getRequestedPath((HttpServletRequest) request);
        Class<? extends ActionBean> beanType = getStripesFilter()
                .getInstanceConfiguration().getActionResolver().getActionBeanType(uri);
        if(beanType != null) {
            stripesDispatcher.service(request, response);
        } else {
            chain.doFilter(request, response);
        }
    }

}
