package com.manydesigns.portofino.ui.support.blob;

import com.manydesigns.portofino.ui.support.Resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

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
