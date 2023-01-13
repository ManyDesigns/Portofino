package com.manydesigns.portofino.upstairs.actions.actions;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.i18n.TextProvider;
import com.manydesigns.elements.text.OgnlTextFormat;
import com.manydesigns.elements.util.RandomUtil;
import com.manydesigns.elements.util.ReflectionUtil;
import com.manydesigns.portofino.resourceactions.ResourceActionConfiguration;
import com.manydesigns.portofino.dispatcher.Resource;
import com.manydesigns.portofino.dispatcher.WithParameters;
import com.manydesigns.portofino.resourceactions.AbstractResourceAction;
import com.manydesigns.portofino.resourceactions.ActionInstance;
import com.manydesigns.portofino.resourceactions.ConfigurationWithDefaults;
import com.manydesigns.portofino.resourceactions.ResourceAction;
import com.manydesigns.portofino.resourceactions.registry.ActionInfo;
import com.manydesigns.portofino.resourceactions.registry.ActionRegistry;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresAdministrator;
import com.manydesigns.portofino.security.RequiresPermissions;
import com.manydesigns.portofino.upstairs.actions.support.ActionTypeInfo;
import ognl.OgnlContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

@RequiresAdministrator
public class ActionsAction extends AbstractResourceAction {

    private static final Logger logger = LoggerFactory.getLogger(ActionsAction.class);

    public static final String PORTOFINO_ACTION_MOVE_TYPE = "application/vnd.com.manydesigns.portofino.action-move";

    @Autowired
    protected ActionRegistry actionRegistry;

    public ActionsAction() {
        minParameters = 0;
        maxParameters = Integer.MAX_VALUE;
    }

    @Path("action")
    public Resource getResource() {
        String actionPath = StringUtils.join(parameters, "/");
        Resource resource = getResource(actionPath);
        if(resource == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return resource;
    }

    @Path(":types")
    @GET
    @RequiresPermissions(level = AccessLevel.NONE)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, ActionTypeInfo> getResourceActionTypes() {
        Map<String, ActionTypeInfo> result = new HashMap<>();
        TextProvider textProvider = ElementsThreadLocals.getTextProvider();
        actionRegistry.iterator().forEachRemaining(a -> {
            String className = a.actionClass.getName();
            result.put(a.description, new ActionTypeInfo(
                    className,
                    a.getActionName(textProvider),
                    textProvider.getTextOrNull(className + ".description"),
                    a.supportsDetail));
        });
        return result;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> describe() {
        return getResourceAction().describe();
    }

    protected AbstractResourceAction getResourceAction() {
        String actionPath = StringUtils.join(parameters, "/");
        Resource resource = getResource(actionPath);
        if(resource instanceof AbstractResourceAction) {
            return ((AbstractResourceAction) resource);
        } else {
            logger.error("Not a ResourceAction: " + resource);
            throw new WebApplicationException();
        }
    }

    @POST
    public void create(String actionClassName) throws Exception {
        String actionPath = StringUtils.join(parameters.subList(0, parameters.size() - 1), "/");
        String segment = parameters.get(parameters.size() - 1);
        ActionInstance parentActionInstance = getPageInstance(actionPath);
        ResourceAction parent = parentActionInstance.getActionBean();

        Class actionClass = codeBase.loadClass(actionClassName);
        ActionInfo info = actionRegistry.getInfo(actionClass);
        String scriptTemplate = info.scriptTemplate;
        boolean supportsDetail = info.supportsDetail;

        String className = actionClass.getSimpleName() + "_" + RandomUtil.createRandomId();
        OgnlContext ognlContext = ElementsThreadLocals.getOgnlContext();
        ognlContext.put("generatedClassName", className);
        ognlContext.put("actionClassName", actionClassName);
        String script = OgnlTextFormat.format(scriptTemplate, parent);

        ResourceActionConfiguration action = new ResourceActionConfiguration();
        ResourceActionConfiguration configuration;
        if(info.configurationClass != null) {
            configuration = ReflectionUtil.newInstance(info.configurationClass);
            if(configuration instanceof ConfigurationWithDefaults) {
                ((ConfigurationWithDefaults) configuration).setupDefaults();
            }
        } else {
            configuration = new ResourceActionConfiguration();
        }
        action.init();

        FileObject directory = parentActionInstance.getChildPageDirectory(segment);
        if(directory.exists()) {
            logger.error("Can't create actionDescriptor - directory {} exists", directory.getName().getPath());
            throw new WebApplicationException(
                    Response.serverError()
                    .entity(ElementsThreadLocals.getText("error.creating.action.the.directory.already.exists"))
                    .build());
        }
        directory.createFolder();
        logger.debug("Creating the new child actionDescriptor in directory: {}", directory);
        ResourceAction theAction = (ResourceAction) actionClass.getConstructor().newInstance();
        autowire(theAction);
        ActionInstance actionInstance = new ActionInstance(null, directory, actionClass);
        actionInstance.setConfiguration(configuration);
        theAction.setActionInstance(actionInstance);
        theAction.saveConfiguration();
        FileObject groovyScriptFile = directory.resolveFile("action.groovy");
        groovyScriptFile.createFile();
        try(Writer w = new OutputStreamWriter(groovyScriptFile.getContent().getOutputStream())) {
            w.write(script);
        }
        if(supportsDetail) {
            FileObject detailDir = directory.resolveFile(AbstractResourceAction.DETAIL);
            logger.debug("Creating _detail directory: {}", detailDir);
            detailDir.createFolder();
        }
        logger.info("Created action of type " + actionClassName + " in directory " + directory);
    }

    @POST
    @Consumes(PORTOFINO_ACTION_MOVE_TYPE)
    public void move(String sourceActionPath) throws FileSystemException {
        String actionPath = StringUtils.join(parameters.subList(0, parameters.size() - 1), "/");
        String segment = parameters.get(parameters.size() - 1);
        copyOrMovePage(sourceActionPath, actionPath, segment, true);
    }

    @DELETE
    public void delete() throws Exception {
        String actionPath = StringUtils.join(parameters, "/");
        ActionInstance actionInstance = getPageInstance(actionPath);
        FileObject directory = actionInstance.getDirectory();
        if(!directory.exists()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        directory.deleteAll();
    }

    public ActionInstance getPageInstance(String actionPath) {
        ResourceAction action = (ResourceAction) getResource(actionPath);
        if(action == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        ActionInstance actionInstance = action.getActionInstance();
        checkPermissions(actionInstance);
        return actionInstance;
    }

    protected void copyOrMovePage(
            String sourceActionPath, String destinationParentActionPath, String segment, boolean move) throws FileSystemException {
        ActionInstance sourceActionInstance = getPageInstance(sourceActionPath);
        ActionInstance destinationParentActionInstance = getPageInstance(destinationParentActionPath);
        FileObject newChild = destinationParentActionInstance.getChildPageDirectory(segment);

        if(move) {
            if(sourceActionPath.equals("/") || sourceActionPath.isEmpty()) {
                throw new WebApplicationException("Cannot move the root action!");
            }
            sourceActionInstance.getDirectory().moveTo(newChild);
        } else {
            newChild.copyFrom(sourceActionInstance.getDirectory(), new AllFileSelector());
        }
    }

    public void checkPermissions(ActionInstance actionInstance) {
        if (!checkPermissionsOnTargetPage(actionInstance)) {
            Response.Status status =
                    security.isUserAuthenticated() ?
                            Response.Status.FORBIDDEN :
                            Response.Status.UNAUTHORIZED;
            throw new WebApplicationException(status);
        }
    }

    //ActionDescriptor create/delete/move etc.
    protected boolean checkPermissionsOnTargetPage(ActionInstance targetActionInstance) {
        return checkPermissionsOnTargetPage(targetActionInstance, AccessLevel.DEVELOP);
    }

    protected boolean checkPermissionsOnTargetPage(ActionInstance targetActionInstance, AccessLevel accessLevel) {
        if(!security.hasPermissions(portofinoConfiguration.getProperties(), targetActionInstance, accessLevel)) {
            logger.warn("User not authorized modify actionDescriptor {}", targetActionInstance);
            return false;
        }
        return true;
    }

    public Resource getResource(String actionPath) {
        Resource resource = getRoot();
        if(actionPath.isEmpty()) {
            return resource;
        }
        String[] pathSegments = actionPath.split("/");
        for(String segment : pathSegments) {
            if(resource instanceof WithParameters) {
                WithParameters withParameters = (WithParameters) resource;
                if(withParameters.getParameters().size() < withParameters.getMinParameters()) {
                    withParameters.consumeParameter(segment);
                    continue;
                }
            }
            Object subResource = null;
            try {
                subResource = resource.getSubResource(segment);
            } catch (Exception e) {
                logger.debug("Could not load resource", e);
            }
            if(subResource instanceof Resource) {
                resource = (Resource) subResource;
            } else if(resource instanceof WithParameters) {
                WithParameters withParameters = (WithParameters) resource;
                if(withParameters.getParameters().size() < withParameters.getMaxParameters()) {
                    withParameters.consumeParameter(segment);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
        return resource;
    }

}
