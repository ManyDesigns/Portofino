package com.manydesigns.portofino.ui.support.pages;

import com.manydesigns.elements.servlet.ServletConstants;
import org.apache.commons.io.IOUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;

public class ConfigJsonCacheFilter implements Filter {


    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        String path = request.getRequestURI().substring(request.getContextPath().length());
        path = URLDecoder.decode(path, "UTF-8"); //TODO read from portofino.properties
        if(path.endsWith("config.json")) {
            //HTTP 1.0
            response.setHeader(ServletConstants.HTTP_PRAGMA, ServletConstants.HTTP_PRAGMA_NO_CACHE);
            response.setHeader(ServletConstants.HTTP_EXPIRES, "0");
            //HTTP 1.1
            response.addHeader(ServletConstants.HTTP_CACHE_CONTROL, ServletConstants.HTTP_CACHE_CONTROL_NO_CACHE);
            response.addHeader(ServletConstants.HTTP_CACHE_CONTROL, ServletConstants.HTTP_CACHE_CONTROL_NO_STORE);

            response.setHeader("Content-Type", "application/json");

            File configJsonFile = new File(request.getServletContext().getRealPath(path));
            try(FileReader reader = new FileReader(configJsonFile)) {
                IOUtils.copy(reader, response.getWriter());
            }
        } else {
            chain.doFilter(req, resp);
        }
    }

    @Override
    public void destroy() {

    }
}
