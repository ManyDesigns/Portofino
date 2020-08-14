package com.manydesigns.portofino.ui.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;

public class Resource {

    public static final String AUTHORIZATION_HEADER = "Authorization";

    private static final Logger logger = LoggerFactory.getLogger(Resource.class);

    @Context
    protected ServletContext servletContext;

    @Context
    protected HttpServletRequest request;

    @Context
    protected UriInfo uriInfo;

    public WebTarget path(String path) {
        Client c = ClientBuilder.newClient();
        return path(c, path);
    }

    protected WebTarget path(Client c, String path) {
        String localAddr = request.getLocalAddr();
        if(localAddr.contains(":")) { //Assume this is an IPv6 address
            localAddr = "[" + localAddr + "]"; //https://www.ietf.org/rfc/rfc2732.txt
        }
        String localUri = request.getScheme() + "://" + localAddr + ":" + request.getLocalPort() + request.getContextPath();

        String baseUri = null;
        String apiRootUri = ApiInfo.getApiRootUri(servletContext, uriInfo);
        try {
            URI uri = new URI(localUri);
            baseUri = ApiInfo.getApiRootUri(servletContext, uri);
        } catch (URISyntaxException e) {
            logger.debug("Invalid local URI: " + localUri, e);
        }

        if (baseUri != null && path.startsWith(baseUri)) {
            path = path.substring(baseUri.length());
        } else if(path.startsWith(apiRootUri)) {
            path = path.substring(apiRootUri.length());
        }
        WebTarget target = c.target(baseUri);
        return target.path(path);
    }

}
