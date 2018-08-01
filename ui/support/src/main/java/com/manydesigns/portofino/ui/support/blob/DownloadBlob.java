package com.manydesigns.portofino.ui.support.blob;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Path("blobs")
public class DownloadBlob {

  @Context
  protected ServletContext servletContext;

  @GET
  public Response download(@QueryParam("path") String path, @QueryParam("token") String token) {
    Client c = ClientBuilder.newClient();
    String baseUri = servletContext.getInitParameter("api-root");
    if(baseUri == null) {
      baseUri = "http://localhost:8080/api";
    }
    if(path.startsWith(baseUri)) {
      path = path.substring(baseUri.length());
    }
    WebTarget target = c.target(baseUri);
    Invocation.Builder req = target.path(path).request();
    if(token != null) {
      req = req.header("Authorization", "Bearer " + token);
    }
    return req.get();
  }

}
