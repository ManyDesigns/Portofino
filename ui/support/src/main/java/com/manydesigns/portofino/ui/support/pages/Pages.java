package com.manydesigns.portofino.ui.support.pages;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.blobs.MultipartWrapper;
import com.manydesigns.portofino.ui.support.ApiInfo;
import com.manydesigns.portofino.ui.support.Resource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@Path("pages")
public class Pages extends Resource {

    private static final Logger logger = LoggerFactory.getLogger(Pages.class);

    @Context
    protected ServletContext servletContext;

    @Context
    protected UriInfo uriInfo;

    @POST
    @Path("{path:.+}")
    public Response createPageAndAction(
        @PathParam("path") String path, @HeaderParam(AUTHORIZATION_HEADER) String auth,
        @QueryParam("loginPath") String loginPath, @QueryParam("actionClass") String actionClass,
        @QueryParam("actionPath") String actionPath, @QueryParam("childrenProperty") String childrenProperty,
        String pageConfigurationString) {
        checkPathAndAuth(path, auth, loginPath);
        actionPath = getActionPath(actionPath);
        Invocation.Builder request = path(actionPath).request().header(AUTHORIZATION_HEADER, auth);
        Response response = request.post(Entity.text(actionClass));
        if(response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
            saveConfigJson("pages/" + path, pageConfigurationString);
            ObjectMapper mapper = new ObjectMapper();
            String configPath = servletContext.getRealPath("pages/" + path);
            File pageDirectory = new File(configPath).getParentFile();
            File parentDirectory = pageDirectory.getParentFile();
            Map<String, Object> parentConfig;
            File parentConfigFile = new File(parentDirectory, "config.json");
            try (FileReader fr = new FileReader(parentConfigFile)) {
                String parentConfigString = IOUtils.toString(fr);
                parentConfig = mapper.readValue(parentConfigString, Map.class);
                Map pageConfiguration = mapper.readValue(pageConfigurationString, Map.class);
                List<Map> children = (List<Map>) parentConfig.get(childrenProperty);
                if(children == null) {
                    children = new ArrayList<>();
                    parentConfig.put(childrenProperty, children);
                }
                Map<String, Object> child = new HashMap<>();
                child.put("path", pageDirectory.getName());
                child.put("title", pageConfiguration.get("title"));
                children.add(child);
            } catch (IOException e) {
                logger.error("Could not save config to " + parentDirectory.getAbsolutePath(), e);
                throw new WebApplicationException(e.getMessage(), e);
            }
            try (FileWriter fw = new FileWriter(parentConfigFile)) {
                mapper.writerFor(Map.class).writeValue(fw, parentConfig);
            } catch (IOException e) {
                logger.error("Could not save config to " + parentDirectory.getAbsolutePath(), e);
                throw new WebApplicationException(e.getMessage(), e);
            }
        }
        return response;
    }

    @PUT
    @Path("{path:.+}")
    public Response updatePageAndConfiguration(
        @PathParam("path") String path, @HeaderParam(AUTHORIZATION_HEADER) String auth,
        @QueryParam("loginPath") String loginPath) { //TODO should the login path be asked once and then cached?
        checkPathAndAuth(path, auth, loginPath);
        MultipartWrapper multipart = ElementsThreadLocals.getMultipart();
        Response response = saveActionConfiguration(
            auth,
            multipart.getParameterValues("actionConfigurationPath")[0],
            multipart.getParameterValues("actionConfiguration")[0]);
        if(response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
            saveConfigJson("pages/" + path, multipart.getParameterValues("pageConfiguration")[0]);
        }
        return response;
    }

    @DELETE
    @Path("{path:.+}")
    public Response deletePageAndAction(
        @PathParam("path") String path, @HeaderParam(AUTHORIZATION_HEADER) String auth,
        @QueryParam("loginPath") String loginPath, @QueryParam("actionPath") String actionPath,
        @QueryParam("childrenProperty") String childrenProperty) throws IOException {
        checkPathAndAuth(path, auth, loginPath);
        actionPath = getActionPath(actionPath);
        Invocation.Builder request = path(actionPath).request().header(AUTHORIZATION_HEADER, auth);
        Response response = request.delete();
        if(response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
            String configPath = servletContext.getRealPath("pages/" + path);
            File file = new File(configPath);
            FileUtils.deleteDirectory(file.getParentFile());
            ObjectMapper mapper = new ObjectMapper();
            File pageDirectory = new File(configPath).getParentFile();
            File parentDirectory = pageDirectory.getParentFile();
            Map<String, Object> parentConfig;
            File parentConfigFile = new File(parentDirectory, "config.json");
            try (FileReader fr = new FileReader(parentConfigFile)) {
                String parentConfigString = IOUtils.toString(fr);
                parentConfig = mapper.readValue(parentConfigString, Map.class);
                List<Map> children = (List<Map>) parentConfig.get(childrenProperty);
                if(children != null) {
                    Iterator<Map> iterator = children.iterator();
                    while (iterator.hasNext()) {
                        if(iterator.next().get("path").equals(pageDirectory.getName())) {
                            iterator.remove();
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                logger.error("Could not save config to " + parentDirectory.getAbsolutePath(), e);
                throw new WebApplicationException(e.getMessage(), e);
            }
            try (FileWriter fw = new FileWriter(parentConfigFile)) {
                mapper.writerFor(Map.class).writeValue(fw, parentConfig);
            } catch (IOException e) {
                logger.error("Could not save config to " + parentDirectory.getAbsolutePath(), e);
                throw new WebApplicationException(e.getMessage(), e);
            }
        }
        return response;
    }

    @NotNull
    public String getActionPath(String actionPath) {
        String baseUri = ApiInfo.getApiRootUri(servletContext, uriInfo);
        if (actionPath.startsWith(baseUri)) {
            actionPath = actionPath.substring(baseUri.length());
        }
        actionPath = "portofino-upstairs/actions/" + actionPath;
        return actionPath;
    }

    public void checkPathAndAuth(String path, String auth, String loginPath) {
        if (path.contains("..") || !path.endsWith("config.json")) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                .entity("Invalid config.json path")
                .type(MediaType.TEXT_PLAIN_TYPE)
                .build());
        }
        if (!checkAdmin(loginPath, auth)) {
            //Must be explicitly checked because the action configuration path comes as a parameter and could be anything
            throw new WebApplicationException(
                Response.status(auth != null ? Response.Status.UNAUTHORIZED : Response.Status.FORBIDDEN).build());
        }
    }

    public Response saveActionConfiguration(String auth, String actionConfigurationPath, String actionConfiguration) {
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
        Invocation.Builder req = path(actionConfigurationPath).request();
        req = req.header(AUTHORIZATION_HEADER, auth);
        return req.put(Entity.entity(actionConfiguration, MediaType.APPLICATION_JSON_TYPE));
    }

    public void saveConfigJson(String path, String pageConfiguration) {
        String configPath = servletContext.getRealPath(path);
        File file = new File(configPath);
        file.getParentFile().mkdirs();
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(pageConfiguration);
            logger.info("Saved page configuration to " + file.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Could not save config to " + path, e);
            throw new WebApplicationException(e.getMessage(), e);
        }
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
