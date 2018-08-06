package com.manydesigns.portofino.ui.support;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

@Path("/")
public class ApiInfo {

  @Context
  protected ServletContext servletContext;

  @Context
  protected UriInfo uriInfo;

  @GET
  public Map info() {
    Map<String, String> info = new HashMap<>();
    info.put("apiRoot", getApiRootUri(servletContext, uriInfo));
    return info;
  }

  public static String getApiRootUri(ServletContext servletContext, UriInfo uriInfo) {
    String baseUri = servletContext.getInitParameter("portofino.api.root");
    if(baseUri == null) {
      baseUri = "http://localhost:8080/api";
    } else if(!baseUri.startsWith("/")) {
      String appBaseUri = uriInfo.getBaseUri().toString();
      if(appBaseUri.endsWith("/")) {
        baseUri = baseUri.substring(1);
      }
      baseUri = appBaseUri + baseUri;
    }
    return baseUri;
  }


}
