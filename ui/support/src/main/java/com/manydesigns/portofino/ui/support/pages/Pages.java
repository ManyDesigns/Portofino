package com.manydesigns.portofino.ui.support.pages;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.blobs.FileBean;
import com.manydesigns.elements.blobs.MultipartWrapper;
import com.manydesigns.portofino.ui.support.ApiInfo;
import com.manydesigns.portofino.ui.support.Resource;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Path("pages")
public class Pages extends Resource {

    private static final Logger logger = LoggerFactory.getLogger(Pages.class);

    @Context
    protected ServletContext servletContext;

    @Context
    protected UriInfo uriInfo;

    @PUT
    @Path("{path:.+}")
    public Response savePage(
        @PathParam("path") String path, @HeaderParam(AUTHORIZATION_HEADER) String auth,
        @QueryParam("loginPath") String loginPath) throws IOException { //TODO should the login path be asked once and then cached?
        if (!checkAdmin(loginPath, auth)) {
            return Response.status(auth != null ? Response.Status.UNAUTHORIZED : Response.Status.FORBIDDEN).build();
        }
        //TODO put to the backend too and/or check the JWT
        if (path.contains("..") || !path.endsWith("config.json")) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Invalid config.json path")
                .type(MediaType.TEXT_PLAIN_TYPE)
                .build();
        }
        path = "pages/" + path;

        MultipartWrapper multipart = ElementsThreadLocals.getMultipart();
        String pageConfiguration = multipart.getParameterValues("pageConfiguration")[0];
        String configPath = servletContext.getRealPath(path);
        String actionConfigurationPath = multipart.getParameterValues("actionConfigurationPath")[0];
        String apiRootUri = ApiInfo.getApiRootUri(servletContext, uriInfo);
        if(actionConfigurationPath.startsWith(apiRootUri)) {
            actionConfigurationPath = actionConfigurationPath.substring(apiRootUri.length());
        }
        if(actionConfigurationPath.startsWith("http://") || actionConfigurationPath.startsWith("https://")) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Non-local action configuration path")
                .type(MediaType.TEXT_PLAIN_TYPE)
                .build();
        }

        File file = new File(configPath);
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(pageConfiguration);
            logger.info("Saved page configuration to " + file.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Could not save config to " + path, e);
            throw new WebApplicationException(e.getMessage(), e);
        }
        String actionConfiguration = multipart.getParameterValues("actionConfiguration")[0];
        Invocation.Builder req = path(actionConfigurationPath).request();
        req = req.header(AUTHORIZATION_HEADER, auth);
        Response response = req.put(Entity.entity(actionConfiguration, MediaType.APPLICATION_JSON_TYPE));
        return response;
    }

    protected boolean checkAdmin(String loginPath, String authorization) {
        Invocation.Builder req = path(loginPath).request();
        req = req.header(AUTHORIZATION_HEADER, authorization);
        Response response = req.get();
        if (response.getStatus() == 200) {
            Map info = response.readEntity(Map.class);
            return (boolean) info.get("administrator");
        }
        return false;
    }


}
