package com.manydesigns.portofino.ui.support;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import java.util.HashMap;
import java.util.Map;

@Path("/")
public class ApiInfo {

  @Context
  protected ServletContext servletContext;

  @GET
  public Map info() {
    String apiRoot = servletContext.getInitParameter("api-root");
    if(apiRoot == null) {
      apiRoot = "http://localhost:8080/api";
    }
    Map<String, String> info = new HashMap<>();
    info.put("apiRoot", apiRoot);
    return info;
  }

}
