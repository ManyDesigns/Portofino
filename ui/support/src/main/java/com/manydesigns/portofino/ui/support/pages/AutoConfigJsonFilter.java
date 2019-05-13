package com.manydesigns.portofino.ui.support.pages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.manydesigns.portofino.ui.support.ApiInfo;
import org.apache.commons.io.IOUtils;
import org.json.JSONStringer;
import org.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.List;

public class AutoConfigJsonFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(AutoConfigJsonFilter.class);
    protected boolean writeGeneratedFiles;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        writeGeneratedFiles = "true".equalsIgnoreCase(filterConfig.getInitParameter("writeGeneratedFiles"));
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            HttpServletRequest request = (HttpServletRequest) req;
            String path = request.getRequestURI().substring(request.getContextPath().length());
            File configJsonFile = new File(request.getServletContext().getRealPath(path));
            if(path.endsWith("config.json") && !configJsonFile.exists()) {
                StringBuffer requestURL = request.getRequestURL();
                URI baseUri = new URI(requestURL.substring(0, requestURL.lastIndexOf(request.getServletPath())));
                String apiRootUri = ApiInfo.getApiRootUri(req.getServletContext(), baseUri);
                String actionPath = path.substring("/pages".length(), path.length() - "/config.json".length());
                String pageDescriptionUri = apiRootUri + actionPath + "/:description";
                pageDescriptionUri = pageDescriptionUri.replaceAll("([^:])//", "$1/");
                Client c = ClientBuilder.newClient();
                WebTarget target = c.target(pageDescriptionUri);
                Invocation.Builder invocation = target.request();
                String authorizationHeader = request.getHeader("Authorization");
                if(authorizationHeader != null) {
                    invocation = invocation.header("Authorization", authorizationHeader);
                }
                PageDescription pageDescription = invocation.buildGet().invoke(PageDescription.class);
                String type = translateType(pageDescription.superclass);
                if(type != null) {
                    JSONWriter configJson = new JSONStringer().object()
                        .key("title").value(pageDescription.page.title)
                        .key("type").value(type)
                        .key("children").array();
                    if(pageDescription.children != null) {
                        for(String child : pageDescription.children) {
                            configJson.value(child);
                        }
                    }
                    configJson.endArray();
                    configJson.key("detailChildren").array();
                    if(pageDescription.detailChildren != null) {
                        for(String child : pageDescription.detailChildren) {
                            configJson.value(child);
                        }
                    }
                    configJson.endArray();
                    configJson.endObject();
                    String configJsonString = configJson.toString();
                    if(writeGeneratedFiles) {
                        try(FileWriter fw = new FileWriter(configJsonFile)) {
                            fw.write(configJsonString);
                        } catch (IOException e) {
                            logger.warn("Could not save generated file " + configJsonFile, e);
                        }
                    }
                    response.getWriter().write(configJsonString);
                    return;
                } else {
                    logger.warn("Unknown page type: " + pageDescription.superclass);
                }
            }
        } catch (Exception e) {
            logger.debug(e.getMessage(), e);
        }
        chain.doFilter(req, response);
    }

    protected String translateType(String superclass) {
        if("com.manydesigns.portofino.resourceactions.crud.CrudAction".equals(superclass)) {
            return "crud";
        }
        if("com.manydesigns.portofino.resourceactions.crud.AbstractCrudAction".equals(superclass)) {
            return "crud";
        }
        return null;
    }

    @Override
    public void destroy() {

    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class PageDescription {
    public String superclass;
    public List<String> children;
    public List<String> detailChildren;
    public Page page;
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Page {
    public String title;
    //TODO support old (Portofino 4) layout information for embedding
}
