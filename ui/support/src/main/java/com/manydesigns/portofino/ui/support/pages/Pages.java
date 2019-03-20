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
    public static final String PORTOFINO_PAGE_MOVE_TYPE = "application/vnd.com.manydesigns.portofino.page-move";

    @Context
    protected ServletContext servletContext;

    @Context
    protected UriInfo uriInfo;

    @POST
    @Path("{path:.+}")
    public Response createPageAndAction(
        @HeaderParam(AUTHORIZATION_HEADER) String auth,
        @PathParam("path") String path, @QueryParam("loginPath") String loginPath,
        @QueryParam("actionClass") String actionClass, @QueryParam("actionPath") String actionPath,
        @QueryParam("childrenProperty") String childrenProperty, String pageConfigurationString) {
        checkPathAndAuth(path, auth, loginPath);
        if(actionPath != null && actionClass != null) {
            actionPath = getActionPath(actionPath);
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
        @PathParam("path") String path, @QueryParam("actionConfigurationPath") String actionConfigurationPath,
        @QueryParam("loginPath") String loginPath) { //TODO should the login path be asked once and then cached?
        checkPathAndAuth(path, auth, loginPath);
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
            writeConfig(mapper, parentConfig, parentConfigFile);
        }
        return response;
    }

    @POST
    @Path("{destinationPath:.+}")
    @Consumes(PORTOFINO_PAGE_MOVE_TYPE)
    public Response movePageAndAction(
        @HeaderParam(AUTHORIZATION_HEADER) String auth,
        @PathParam("destinationPath") String destinationPath, @QueryParam("loginPath") String loginPath,
        @QueryParam("sourceActionPath") String sourceActionPath,
        @QueryParam("destinationActionParent") String destinationActionParent,
        @QueryParam("detail") boolean detail,
        String sourcePath) throws IOException {
        checkPathAndAuth(destinationPath, auth, loginPath);
        checkPath(sourcePath);
        String baseUri = ApiInfo.getApiRootUri(servletContext, uriInfo);
        if (sourceActionPath.startsWith(baseUri)) {
            sourceActionPath = sourceActionPath.substring(baseUri.length());
        }
        File destParentConfigFile = new File(servletContext.getRealPath("pages/" + destinationPath));
        String[] segments = sourceActionPath.split("/");
        String segment = segments[segments.length - 1];
        String destinationActionPath = getActionPath(destinationActionParent + (detail ? "/_detail/" : "/") + segment);
        Invocation.Builder request = path(destinationActionPath).request().header(AUTHORIZATION_HEADER, auth);
        Response response = request.post(Entity.entity(sourceActionPath, PORTOFINO_PAGE_MOVE_TYPE));
        if(response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
            File sourceConfigFile = new File(servletContext.getRealPath(sourcePath));
            File destParentConfigDir = destParentConfigFile.getParentFile();
            if(destParentConfigDir.isDirectory() || destParentConfigDir.mkdirs()) {
                File destConfigDir = new File(destParentConfigDir, segment);
                destConfigDir.mkdirs();
                movePage(sourceConfigFile, new File(destConfigDir, "config.json"), detail ? "detailChildren" : "children");
            } else {
                throw new WebApplicationException("Could not create " + destParentConfigDir.getAbsolutePath());
            }
        }
        return response;
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
            mapper.writerFor(Map.class).writeValue(fw, config);
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
        checkPath(path);
        if (!checkAdmin(loginPath, auth)) {
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
            fw.write(pageConfiguration);
            logger.info("Saved page configuration to " + file.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Could not save config to " + file.getAbsolutePath(), e);
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
