package com.manydesigns.portofino.ui.support.blob;

import com.manydesigns.portofino.ui.support.Resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.Response;

@Path("blobs")
public class DownloadBlob extends Resource {

  @GET
  public Response download(@QueryParam("path") String path, @QueryParam("token") String token) {
    Invocation.Builder req = path(path).request();
    if(token != null) {
      req = req.header("Authorization", "Bearer " + token);
    }
    return req.get();
  }


}
