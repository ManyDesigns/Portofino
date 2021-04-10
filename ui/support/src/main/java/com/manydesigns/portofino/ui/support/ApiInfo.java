package com.manydesigns.portofino.ui.support;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Path("/")
public class ApiInfo extends Resource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> info() {
        Map<String, String> info = new HashMap<>();
        info.put("apiRoot", getApiRootUri(servletContext, uriInfo));
        info.put("loginPath", "/:auth"); //For legacy clients
        return info;
    }

    public static String getApiRootUri(ServletContext servletContext, UriInfo uriInfo) {
        return getApiRootUri(servletContext, uriInfo.getBaseUri());
    }

    public static String getApiRootUri(ServletContext servletContext, URI baseUri) {
        String apiRoot = servletContext.getInitParameter("portofino.api.root");
        if (apiRoot == null) {
            apiRoot = "";
        }
        if (!apiRoot.contains("://")) {
            String baseAddress = baseUri.getScheme() + "://" + baseUri.getAuthority();
            if (!apiRoot.startsWith("/")) {
                baseAddress += servletContext.getContextPath();
            }
            apiRoot = baseAddress + "/" + apiRoot;
        }
        if (!apiRoot.endsWith("/")) {
            apiRoot += "/";
        }
        return apiRoot;
    }

}
