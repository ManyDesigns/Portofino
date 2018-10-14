package com.manydesigns.portofino.ui.support;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
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
    Invocation.Builder request = path(":description").request();
    Response response = request.get();
    if(response.getStatus() == 200) {
      Map map = response.readEntity(Map.class);
      info.put("loginPath", map.get("loginPath").toString());
    } else {
      throw new WebApplicationException(response);
    }
    return info;
  }

  public static String getApiRootUri(ServletContext servletContext, UriInfo uriInfo) {
    String apiRoot = servletContext.getInitParameter("portofino.api.root");
    if(apiRoot == null) {
      apiRoot = "http://localhost:8080";
    } else if(apiRoot.contains("://")) {
      //Keep as is
    } else if(!apiRoot.startsWith("/")) {
      apiRoot = servletContext.getContextPath() + "/" + apiRoot;
    }
    if(!apiRoot.contains("://")) {
      URI baseUri = uriInfo.getBaseUri();
      apiRoot = baseUri.getScheme() + "://" + baseUri.getAuthority() + apiRoot;
    }
    if(!apiRoot.endsWith("/")) {
      apiRoot += "/";
    }
    return apiRoot;
  }

}
