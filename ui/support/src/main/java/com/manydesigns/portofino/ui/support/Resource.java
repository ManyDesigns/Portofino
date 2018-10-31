package com.manydesigns.portofino.ui.support;

import javax.servlet.ServletContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

public class Resource {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    @Context
    protected ServletContext servletContext;

    @Context
    protected UriInfo uriInfo;

    public WebTarget path(String path) {
        Client c = ClientBuilder.newClient();
        String baseUri = ApiInfo.getApiRootUri(servletContext, uriInfo);
        if (path.startsWith(baseUri)) {
            path = path.substring(baseUri.length());
        }
        WebTarget target = c.target(baseUri);
        return target.path(path);
    }

}
