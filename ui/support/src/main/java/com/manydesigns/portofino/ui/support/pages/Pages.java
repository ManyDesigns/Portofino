package com.manydesigns.portofino.ui.support.pages;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.blobs.MultipartWrapper;
import com.manydesigns.portofino.ui.support.ApiInfo;
import com.manydesigns.portofino.ui.support.Resource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

@Path("pages")
public class Pages extends Resource {

    private static final Logger logger = LoggerFactory.getLogger(Pages.class);
    public static final String PORTOFINO_ACTION_MOVE_TYPE = "application/vnd.com.manydesigns.portofino.action-move";
    public static final String PORTOFINO_PAGE_MOVE_TYPE = "application/vnd.com.manydesigns.portofino.page-move";

    @POST
    @Path("{path:.+}")
    public Response createPageAndAction(
        @HeaderParam(AUTHORIZATION_HEADER) String auth,
        @PathParam("path") String path,
        @QueryParam("actionClass") String actionClass, @QueryParam("actionPath") String actionPath,
        @QueryParam("childrenProperty") String childrenProperty, String pageConfigurationString) {
        checkPathAndAuth(path, auth);
        actionPath = getActionPath(actionPath);
        if(actionPath != null && actionClass != null) {
            Invocation.Builder request = path(actionPath).request().header(AUTHORIZATION_HEADER, auth);
            Response response = request.post(Entity.text(actionClass));
            if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
                createPage(path, childrenProperty, pageConfigurationString);
            }
            return response;
        } else {
            createPage(path, childrenProperty, pageConfigurationString);
            return Response.ok().build();
        }
    }

    public void createPage(String path, String childrenProperty, String pageConfigurationString) {
        saveConfigJson("pages/" + path, pageConfigurationString);
        ObjectMapper mapper = new ObjectMapper();
        String configPath = servletContext.getRealPath("pages/" + path);
        File pageDirectory = new File(configPath).getParentFile();
        File parentDirectory = pageDirectory.getParentFile();
        Map<String, Object> parentConfig;
        File parentConfigFile = new File(parentDirectory, "config.json");
        try (FileReader fr = new FileReader(parentConfigFile)) {
            Map pageConfiguration = mapper.readValue(pageConfigurationString, Map.class);
            String parentConfigString = IOUtils.toString(fr);
            parentConfig = mapper.readValue(parentConfigString, Map.class);
            List<Map> children = ensureChildren(parentConfig, childrenProperty);
            Map<String, Object> child = new HashMap<>();
            child.put("path", pageDirectory.getName());
            child.put("title", pageConfiguration.get("title"));
            child.put("showInNavigation", true);
            children.add(child);
        } catch (IOException e) {
            logger.error("Could not save config to " + parentDirectory.getAbsolutePath(), e);
            throw new WebApplicationException(e.getMessage(), e);
        }
        writeConfig(mapper, parentConfig, parentConfigFile);
    }

    @NotNull
    public List<Map> ensureChildren(Map<String, Object> config, String childrenProperty) {
        List<Map> children = (List<Map>) config.get(childrenProperty);
        if (children == null) {
            children = new ArrayList<>();
            config.put(childrenProperty, children);
        }
        return children;
    }

    @PUT
    @Path("{path:.+}")
    public Response updatePageAndConfiguration(
        @HeaderParam(AUTHORIZATION_HEADER) String auth,
        @PathParam("path") String path, @QueryParam("actionConfigurationPath") String actionConfigurationPath) {
        checkPathAndAuth(path, auth);
        MultipartWrapper multipart = ElementsThreadLocals.getMultipart();
        String[] actionConfigurationParameter = multipart.getParameterValues("actionConfiguration");
        String pageConfiguration = multipart.getParameterValues("pageConfiguration")[0];
        String pageConfigurationPath = "pages/" + path;
        if(actionConfigurationParameter != null) {
            Response response = saveActionConfiguration(auth, actionConfigurationPath, actionConfigurationParameter[0]);
            if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
                saveConfigJson(pageConfigurationPath, pageConfiguration);
            }
            return response;
        } else {
            saveConfigJson(pageConfigurationPath, pageConfiguration);
            return Response.ok().build();
        }
    }

    @DELETE
    @Path("{path:.+}")
    public Response deletePageAndAction(
        @PathParam("path") String path, @HeaderParam(AUTHORIZATION_HEADER) String auth,
        @QueryParam("actionPath") String actionPath,
        @QueryParam("childrenProperty") String childrenProperty) throws IOException {
        checkPathAndAuth(path, auth);
        actionPath = getActionPath(actionPath);
        Response response;
        if(actionPath != null) {
            Invocation.Builder request = path(actionPath).request().header(AUTHORIZATION_HEADER, auth);
            response = request.delete();
            if(response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                return response;
            }
        } else {
            response = Response.ok().build();
        }
        deletePage(path, childrenProperty);
        return response;
    }

    protected void deletePage(String path, String childrenProperty) throws IOException {
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
        writeConfig(mapper, parentConfig, parentConfigFile);
    }

    @POST
    @Path("{destinationPath:.+}")
    @Consumes(PORTOFINO_PAGE_MOVE_TYPE)
    public Response movePageAndAction(
        @HeaderParam(AUTHORIZATION_HEADER) String auth,
        @PathParam("destinationPath") String destinationPath,
        @QueryParam("segment") String segment,
        @QueryParam("sourceActionPath") String sourceActionPath,
        @QueryParam("destinationActionParent") String destinationActionParent,
        @QueryParam("detail") boolean detail,
        String sourcePath) throws IOException {
        checkPathAndAuth(destinationPath, auth);
        checkPath(sourcePath);
        String baseUri = ApiInfo.getApiRootUri(servletContext, uriInfo);
        File destParentConfigFile = new File(servletContext.getRealPath("pages/" + destinationPath));
        if(destinationActionParent == null) {
            movePage(sourcePath, destParentConfigFile, segment, detail);
            return Response.ok().build();
        }
        if (sourceActionPath.startsWith(baseUri)) {
            sourceActionPath = sourceActionPath.substring(baseUri.length());
        }
        String actionPath = destinationActionParent + (detail ? "/_detail/" : "/") + segment;
        String destinationActionPath = getActionPath(actionPath);
        if(destinationActionPath == null) {
            throw new WebApplicationException("Invalid destination action path: " + actionPath);
        }
        Invocation.Builder request = path(destinationActionPath).request().header(AUTHORIZATION_HEADER, auth);
        Response response = request.post(Entity.entity(sourceActionPath, PORTOFINO_ACTION_MOVE_TYPE));
        if(response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
            movePage(sourcePath, destParentConfigFile, segment, detail);
        }
        return response;
    }

    public void movePage(String sourcePath, File destParentConfigFile, String segment, boolean detail) throws IOException {
        if(segment.contains("..") || segment.contains(File.separator)) {
            throw new IllegalArgumentException("Invalid segment: " + segment);
        }
        File sourceConfigFile = new File(servletContext.getRealPath(sourcePath));
        File destParentConfigDir = destParentConfigFile.getParentFile();
        File destConfigDir = new File(destParentConfigDir, segment); //lgtm [java/path-injection] until https://github.com/Semmle/ql/issues/1416 is fixed
        if(destConfigDir.isDirectory() || destConfigDir.mkdirs()) {
            movePage(sourceConfigFile, new File(destConfigDir, "config.json"), detail ? "detailChildren" : "children");
        } else {
            throw new WebApplicationException("Could not create directory " + destConfigDir.getAbsolutePath());
        }
    }

    public void movePage(File sourceConfigFile, File destConfigFile, String childrenProperty) throws IOException {
        FileUtils.moveFile(sourceConfigFile, destConfigFile);

        ObjectMapper mapper = new ObjectMapper();
        Map originalChild;

        Map<String, Object> parentSourceConfig;
        File sourceDirectory = sourceConfigFile.getParentFile();
        File parentSourceConfigFile = new File(sourceDirectory.getParentFile(), "config.json");
        try (FileReader fr = new FileReader(parentSourceConfigFile)) {
            String parentConfigString = IOUtils.toString(fr);
            parentSourceConfig = mapper.readValue(parentConfigString, Map.class);
            List<Map> children = (List<Map>) parentSourceConfig.get("children");
            originalChild = removeChild(sourceDirectory.getName(), children);
            if(originalChild == null) {
                children = (List<Map>) parentSourceConfig.get("detailChildren");
                originalChild = removeChild(sourceDirectory.getName(), children);
            }
        } catch (IOException e) {
            logger.error("Could not save config to " + parentSourceConfigFile.getAbsolutePath(), e);
            throw new WebApplicationException(e.getMessage(), e);
        }
        writeConfig(mapper, parentSourceConfig, parentSourceConfigFile);

        Map<String, Object> parentDestConfig;
        File destDirectory = destConfigFile.getParentFile();
        File parentDestConfigFile = new File(destDirectory.getParentFile(), "config.json");
        try (FileReader fr = new FileReader(parentDestConfigFile)) {
            String parentConfigString = IOUtils.toString(fr);
            parentDestConfig = mapper.readValue(parentConfigString, Map.class);
            List<Map> children = ensureChildren(parentDestConfig, childrenProperty);
            Map<String, Object> child = new HashMap<>();
            child.putAll(originalChild);
            child.put("path", destDirectory.getName());
            children.add(child);
        } catch (IOException e) {
            logger.error("Could not save config to " + parentDestConfigFile.getAbsolutePath(), e);
            throw new WebApplicationException(e.getMessage(), e);
        }
        writeConfig(mapper, parentDestConfig, parentDestConfigFile);
    }

    public void writeConfig(ObjectMapper mapper, Map<String, Object> config, File configFile) {
        try (FileWriter fw = new FileWriter(configFile)) {
            ObjectWriter objectWriter = mapper.writerFor(Map.class);
            objectWriter.withDefaultPrettyPrinter().writeValue(fw, config);
        } catch (IOException e) {
            logger.error("Could not save config to " + configFile.getAbsolutePath(), e);
            throw new WebApplicationException(e.getMessage(), e);
        }
    }

    public Map removeChild(String name, List<Map> children) {
        if(children != null) {
            Iterator<Map> iterator = children.iterator();
            while (iterator.hasNext()) {
                Map child = iterator.next();
                if(name.equals(child.get("path"))) {
                    iterator.remove();
                    return child;
                }
            }
        }
        return null;
    }

    public String getActionPath(String actionPath) {
        if(actionPath == null) {
            return null;
        }
        String baseUri = ApiInfo.getApiRootUri(servletContext, uriInfo);
        if (actionPath.startsWith(baseUri)) {
            actionPath = actionPath.substring(baseUri.length());
        } else {
            try {
                new URL(actionPath);
                return null; //Action is external
            } catch (MalformedURLException e) {
                logger.debug(actionPath, e);
            }
        }
        actionPath = "portofino-upstairs/actions/" + actionPath;
        return actionPath;
    }

    public void checkPathAndAuth(String path, String auth) {
        checkPath(path);
        if (!checkAdmin(auth)) {
            //Must be explicitly checked because the action configuration path comes as a parameter and could be anything
            throw new WebApplicationException(
                Response.status(auth != null ? Response.Status.UNAUTHORIZED : Response.Status.FORBIDDEN).build());
        }
    }

    public void checkPath(String path) {
        if (path.contains("..") || !path.endsWith("config.json")) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                .entity("Invalid config.json path")
                .type(MediaType.TEXT_PLAIN_TYPE)
                .build());
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
            //Note this is different from the format used by Jackson, it would make sense to use Jackson here too
            new JSONObject(pageConfiguration).write(fw, 2, 0);
            logger.info("Saved page configuration to " + file.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Could not save config to " + file.getAbsolutePath(), e);
            throw new WebApplicationException(e.getMessage(), e);
        }
    }

    protected boolean checkAdmin(String authorization) {
        Invocation.Builder req = path(":auth").request();
        req = req.header(AUTHORIZATION_HEADER, authorization);
        Response response = req.get();
        if (response.getStatus() == 200) {
            Map info = response.readEntity(Map.class);
            return (boolean) info.get("administrator");
        }
        return false;
    }

}
