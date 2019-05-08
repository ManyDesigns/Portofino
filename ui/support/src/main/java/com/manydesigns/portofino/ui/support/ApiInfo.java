package com.manydesigns.portofino.ui.support;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
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
    public Map info() {
        Map<String, String> info = new HashMap<>();
        info.put("apiRoot", getApiRootUri(servletContext, uriInfo));
        try {
            Invocation.Builder request = path(":description").request();
            Response response = request.get();
            if (response.getStatus() == 200) {
                Map map = response.readEntity(Map.class);
                info.put("loginPath", map.get("loginPath").toString());
            } else {
                //Ignore. The client will ask for the loginPath itself.
            }
        } catch (Exception e) {
            //Ignore. The client will ask for the loginPath itself.
        }
        return info;
    }

    public static String getApiRootUri(ServletContext servletContext, UriInfo uriInfo) {
        return getApiRootUri(servletContext, uriInfo.getBaseUri());
    }

    public static String getApiRootUri(ServletContext servletContext, URI baseUri) {
        String apiRoot = servletContext.getInitParameter("portofino.api.root");
        if (apiRoot == null) {
            apiRoot = "http://localhost:8080";
        } else if (apiRoot.contains("://")) {
            //Keep as is
        } else if (!apiRoot.startsWith("/")) {
            apiRoot = servletContext.getContextPath() + "/" + apiRoot;
        }
        if (!apiRoot.contains("://")) {
            apiRoot = baseUri.getScheme() + "://" + baseUri.getAuthority() + apiRoot;
        }
        if (!apiRoot.endsWith("/")) {
            apiRoot += "/";
        }
        return apiRoot;
    }

}
