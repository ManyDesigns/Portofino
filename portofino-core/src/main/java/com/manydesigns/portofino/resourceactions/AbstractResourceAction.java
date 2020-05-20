/*
 * Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.manydesigns.portofino.resourceactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.messages.RequestMessages;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.FilteredClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.util.MimeTypes;
import com.manydesigns.elements.util.ReflectionUtil;
import com.manydesigns.portofino.actions.ActionDescriptor;
import com.manydesigns.portofino.actions.ActionLogic;
import com.manydesigns.portofino.actions.Group;
import com.manydesigns.portofino.actions.Permissions;
import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.dispatcher.AbstractResourceWithParameters;
import com.manydesigns.portofino.dispatcher.Resource;
import com.manydesigns.portofino.operations.GuardType;
import com.manydesigns.portofino.operations.Operation;
import com.manydesigns.portofino.operations.Operations;
import com.manydesigns.portofino.resourceactions.registry.ActionRegistry;
import com.manydesigns.portofino.security.*;
import com.manydesigns.portofino.shiro.ShiroUtils;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONStringer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.*;

/**
 * Convenient abstract base class for ResourceActions. It has fields to hold values of properties specified by the
 * ResourceAction interface as well as other useful objects injected by the framework. It provides standard
 * implementations of many of the ResourceAction methods, as well as important utility methods to handle hierarchical
 * relations among pages, such as embedding.
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@RequiresPermissions(level = AccessLevel.VIEW)
public abstract class AbstractResourceAction extends AbstractResourceWithParameters implements ResourceAction {
    public static final String COPYRIGHT = "Copyright (C) 2005-2020 ManyDesigns srl";

    //--------------------------------------------------------------------------
    // Properties
    //--------------------------------------------------------------------------

    /** The ActionInstance property. Injected. */
    public ActionInstance actionInstance;
    /** The global configuration object. Injected. */
    @Autowired
    public Configuration portofinoConfiguration;
    @Autowired
    protected CodeBase codeBase;
    @Autowired
    protected ActionRegistry actionRegistry;
    @Autowired
    protected ApplicationContext applicationContext;
    @Context
    protected UriInfo uriInfo;

    /**
     * The context object holds various elements of contextual information such
     * as the HTTP request and response objects.
     */
    protected ActionContext context;

    private static final Logger logger =
            LoggerFactory.getLogger(AbstractResourceAction.class);

    protected AbstractResourceAction() {
        maxParameters = ResourceActionLogic.supportsDetail(getClass()) ? Integer.MAX_VALUE : 0;
    }

    @Override
    protected void initSubResource(Resource resource) {
        super.initSubResource(resource);
        if(resource instanceof ResourceAction) {
            initResourceAction((ResourceAction) resource, getActionInstance(), uriInfo);
        }
    }

    @Override
    protected void initSubResource(Object resource) {
        autowire(resource);
    }

    protected void autowire(Object bean) {
        applicationContext.getAutowireCapableBeanFactory().autowireBean(bean);
    }

    public static void initResourceAction(ResourceAction resourceAction, ActionInstance parentActionInstance, UriInfo uriInfo) {
        HttpServletRequest request = ElementsThreadLocals.getHttpServletRequest();
        HttpServletResponse response = ElementsThreadLocals.getHttpServletResponse();
        ActionDescriptor action;
        try {
            action = ActionLogic.getActionDescriptor(resourceAction.getLocation());
        } catch (ActionNotActiveException e) {
            logger.debug("action.xml not found or not valid", e);
            action = new ActionDescriptor();
            action.init();
        }
        ActionInstance actionInstance = new ActionInstance(
                parentActionInstance, resourceAction.getLocation(), action, resourceAction.getClass());
        actionInstance.setActionBean(resourceAction);
        ActionLogic.configureResourceAction(resourceAction, actionInstance);
        ActionContext context = new ActionContext();
        context.setRequest(request);
        context.setResponse(response);
        context.setServletContext(request.getServletContext());
        if(uriInfo != null) { //TODO for Swagger
            String path = uriInfo.getPath();
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            context.setActionPath(path); //TODO
        }
        resourceAction.setContext(context);
    }

    @Override
    public void prepareForExecution() {}

    @Override
    public void consumeParameter(String pathSegment) {
        super.consumeParameter(pathSegment);
        actionInstance.getParameters().add(pathSegment);
    }

    @Override
    public FileObject getChildrenLocation() throws FileSystemException {
        if(parameters.isEmpty()) {
            return getLocation();
        } else {
            return getLocation().resolveFile(ActionInstance.DETAIL);
        }
    }

    @Override
    public ResourceAction getParent() {
        return (ResourceAction) super.getParent();
    }

    public String getActionPath() {
        return context.getActionPath();
    }

    /**
     * Returns the absolute URI of the REST API root. The API root can be set in the web.xml descriptor with the init
     * parameter <code>portofino.api.root</code>. It can be an absolute URL or a relative URL; if it's relative,
     * it is completed with the resource's protocol, host and port.
     * @return the URI of the REST API root.
     */
    public String getApiRootUri() {
        ServletContext servletContext = getContext().getServletContext();
        String apiRoot = servletContext.getInitParameter("portofino.api.root");
        if (apiRoot == null) {
            apiRoot = "";
        }
        if (apiRoot.contains("://")) {
            //Keep as is
        } else if (!apiRoot.startsWith("/")) {
            apiRoot = servletContext.getContextPath() + "/" + apiRoot;
        }
        if (!apiRoot.contains("://")) {
            URI baseUri = uriInfo.getBaseUri();
            apiRoot = baseUri.getScheme() + "://" + baseUri.getAuthority() + apiRoot;
        }
        if (!apiRoot.endsWith("/")) {
            apiRoot += "/";
        }
        return apiRoot;
    }

    public String getAbsoluteActionPath() {
        String actionPath = getActionPath();
        if(actionPath.startsWith("/")) {
            actionPath = actionPath.substring(1);
        }
        return getApiRootUri() + actionPath;
    }

    //--------------------------------------------------------------------------
    // Getters/Setters
    //--------------------------------------------------------------------------

    @Override
    public ActionInstance getActionInstance() {
        return actionInstance;
    }

    @Override
    public void setActionInstance(ActionInstance actionInstance) {
        this.actionInstance = actionInstance;
    }

    public ActionDescriptor getActionDescriptor() {
        return getActionInstance().getActionDescriptor();
    }

    //--------------------------------------------------------------------------
    // Scripting
    //--------------------------------------------------------------------------

//    protected void prepareScript() {
//        String pageId = actionInstance.getPage().getId();
//        File file = ScriptingUtil.getGroovyScriptFile(actionInstance.getDirectory(), "action");
//        FileReader fr = null;
//        try {
//            fr = new FileReader(file);
//            script = IOUtils.toString(fr);
//        } catch (Exception e) {
//            logger.warn("Couldn't load script for page " + pageId, e);
//        } finally {
//            IOUtils.closeQuietly(fr);
//        }
//    }
//
//    protected void updateScript() {
//        File directory = actionInstance.getDirectory();
//        File groovyScriptFile = ScriptingUtil.getGroovyScriptFile(directory, "action");
//        FileWriter fw = null;
//        try {
//            fw = new FileWriter(groovyScriptFile);
//            fw.write(script);
//            fw.flush();
//            fw.close();
//            Class<?> scriptClass = PageLogic.getActionClass(portofinoConfiguration, directory, false);
//            if(scriptClass == null) {
//                SessionMessages.addErrorMessage(ElementsThreadLocals.getText("script.class.is.not.valid"));
//            }
//        } catch (IOException e) {
//            logger.error("Error writing script to " + groovyScriptFile, e);
//            String msg = ElementsThreadLocals.getText("couldnt.write.script.to._", groovyScriptFile.getAbsolutePath());
//            SessionMessages.addErrorMessage(msg);
//        } catch (Exception e) {
//            String pageId = actionInstance.getPage().getId();
//            logger.warn("Couldn't compile script for page " + pageId, e);
//            SessionMessages.addErrorMessage(ElementsThreadLocals.getText("couldnt.compile.script"));
//        } finally {
//            IOUtils.closeQuietly(fw);
//        }
//    }

    public Map getOgnlContext() {
        return ElementsThreadLocals.getOgnlContext();
    }

    public Configuration getPortofinoConfiguration() {
        return portofinoConfiguration;
    }

    @Override
    public void setContext(ActionContext context) {
        this.context = context;
    }

    @Override
    public ActionContext getContext() {
        return context;
    }

    /**
     * Returns an error response with message saying that the resourceaction is not properly
     * configured.
     */
    public Response resourceActionNotConfigured() {
        return Response.serverError().entity("resource-action-not-configured").build();
    }

    /**
     * Returns the list of operations that can be invoked via REST on this resource.
     * @return the list of operations.
     */
    @io.swagger.v3.oas.annotations.Operation(
        operationId =
            "com.manydesigns.portofino.resourceactions.AbstractResourceAction#describeOperations",
        description =
            "Returns the list of operations that can be invoked via REST on this resource. " +
            "If the user doesn't have permission to invoke an operation, or a VISIBLE guard " +
            "doesn't pass, then the operation is excluded from the result. If an ENABLED guard " +
            "doesn't pass, the operation is included, but it is marked as not available.")
    @ApiResponses({ @ApiResponse(
            responseCode = "200", description = "A list of operations (name, signature, available).")})
    @Path(":operations")
    @GET
    @Produces(MimeTypes.APPLICATION_JSON_UTF8)
    public List describeOperations() {
        HttpServletRequest request = context.getRequest();
        List<Operation> operations = Operations.getOperations(getClass());
        List result = new ArrayList();
        Subject subject = SecurityUtils.getSubject();
        for(Operation operation : operations) {
            logger.trace("Operation: {}", operation);
            Method handler = operation.getMethod();
            boolean isAdmin = SecurityLogic.isAdministrator(request);
            if(!isAdmin &&
                    ((actionInstance != null && !SecurityLogic.hasPermissions(
                            portofinoConfiguration, operation.getMethod(), getClass(), actionInstance, subject)) ||
                            !SecurityLogic.satisfiesRequiresAdministrator(request, this, handler))) {
                continue;
            }
            boolean visible = Operations.doGuardsPass(this, handler, GuardType.VISIBLE);
            if(!visible) {
                continue;
            }
            boolean available = Operations.doGuardsPass(this, handler, GuardType.ENABLED);
            Map<String, Object> operationInfo = new HashMap<>();
            operationInfo.put("name", operation.getName());
            operationInfo.put("signature", operation.getSignature());
            operationInfo.put("available", available);
            result.add(operationInfo);
        }
        return result;
    }

    @Override
    public Map<String, Object> describe() {
        Map<String, Object> description = super.describe();
        description.put("page", actionInstance.getActionDescriptor());
        if(ResourceActionLogic.supportsDetail(getClass())) {
            parameters.add("");
            description.put("detailChildren", getSubResources());
            parameters.remove(parameters.size() - 1);
        }
        return description;
    }

    @Override
    @io.swagger.v3.oas.annotations.Operation(
            operationId =
                    "com.manydesigns.portofino.resourceactions.AbstractResourceAction#isAccessible",
            description =
                    "Returns true if this action is accessible, and an HTTP 40x error if it's not." +
                    "Clients can use this method to check if the action is accessible without invoking any" +
                    "other operations.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The string true, if the action is accessible."),
            @ApiResponse(responseCode = "401", description = "If the action is not accessible and the request is not authenticated."),
            @ApiResponse(responseCode = "403", description = "If the action is not accessible for the authenticated user.")
    })
    @Path(":accessible")
    @GET
    public boolean isAccessible() {
        return true;
    }

    ////////////////
    // Configuration
    ////////////////

    @Nullable
    protected Class<?> getConfigurationClass() {
        return ResourceActionLogic.getConfigurationClass(getClass());
    }

    protected ClassAccessor getConfigurationClassAccessor() {
        Class<?> configurationClass = getConfigurationClass();
        if(configurationClass == null) {
            return null;
        }
        return JavaClassAccessor.getClassAccessor(configurationClass);
    }

    /**
     * Returns the configuration of this action, filtered using permissions.
     * @return the configuration.
     */
    @io.swagger.v3.oas.annotations.Operation(
        operationId = "com.manydesigns.portofino.resourceactions.AbstractResourceAction#getConfiguration",
        description = "Returns the configuration of this action. " +
            "The actual type of the configuration object depends on the action class.")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "The configuration object.")})
    @Path(":configuration")
    @GET
    @Produces(MimeTypes.APPLICATION_JSON_UTF8)
    public Object getConfiguration() {
        Object configuration = actionInstance.getConfiguration();
        if(getConfigurationClass() == null) {
            return configuration;
        }
        ClassAccessor classAccessor = getConfigurationClassAccessor();
        ClassAccessor filteredClassAccessor = filterAccordingToPermissions(classAccessor);
        ResourceActionConfiguration filtered = (ResourceActionConfiguration) classAccessor.newInstance();
        for(PropertyAccessor propertyAccessor : filteredClassAccessor.getProperties()) {
            if(propertyAccessor.isWritable()) {
                propertyAccessor.set(filtered, propertyAccessor.get(configuration));
            }
        }
        filtered.init();
        return filtered;
    }

    @NotNull
    protected ClassAccessor filterAccordingToPermissions(ClassAccessor classAccessor) {
        Permissions permissions = SecurityLogic.calculateActualPermissions(actionInstance);
        Subject subject = SecurityUtils.getSubject();
        return filterAccordingToPermissions(classAccessor, permissions, subject);
    }

    @NotNull
    protected ClassAccessor filterAccordingToPermissions(
            ClassAccessor classAccessor, Permissions permissions, Subject subject) {
        List<String> excluded = new ArrayList<>();
        for(PropertyAccessor property : classAccessor.getProperties()) {
            RequiresPermissions requiresPermissions = property.getAnnotation(RequiresPermissions.class);
            boolean permitted =
                    requiresPermissions == null ||
                    SecurityLogic.hasPermissions(getPortofinoConfiguration(), permissions, subject, requiresPermissions);
            if(!permitted) {
                logger.debug("Property not permitted, filtering: {}", property.getName());
                excluded.add(property.getName());
            }
        }
        if(!excluded.isEmpty()) {
            return FilteredClassAccessor.exclude(classAccessor, excluded.toArray(new String[0]));
        } else {
            return classAccessor;
        }
    }

    @io.swagger.v3.oas.annotations.Operation(
            operationId = "com.manydesigns.portofino.resourceactions.AbstractResourceAction#setConfiguration",
            description = "Update the configuration of this action. " +
                    "The actual type of the configuration object depends on the action class.")
    @RequiresAdministrator
    @PUT
    @Path(":configuration")
    public void setConfiguration(
            @RequestBody(description = "The configuration object in JSON format.")
            String configurationString) throws IOException {
        Class<?> configurationClass = ResourceActionLogic.getConfigurationClass(getClass());
        if(configurationClass == null) {
            throw new WebApplicationException("This resource does not support configuration");
        }
        Object configuration = new ObjectMapper().readValue(configurationString, configurationClass);
        saveConfiguration(configuration);
    }

    /**
     * Utility method to save the configuration object to a file in this action's directory.
     * @param configuration the object to save. It must be in a state that will produce a valid XML document.
     * @return true if the object was correctly saved, false otherwise.
     */
    protected boolean saveConfiguration(Object configuration) {
        try {
            FileObject confFile = ActionLogic.saveConfiguration(actionInstance.getDirectory(), configuration);
            logger.info("Configuration saved to " + confFile.getName().getPath());
            return true;
        } catch (Exception e) {
            logger.error("Couldn't save configuration", e);
            RequestMessages.addErrorMessage("error saving conf");
            return false;
        }
    }

    @io.swagger.v3.oas.annotations.Operation(
            operationId = "com.manydesigns.portofino.resourceactions.AbstractResourceAction#getConfigurationAccessor",
            description = "A ClassAccessor that describes the configuration of this action.")
    @GET
    @Path(":configuration/classAccessor")
    @Produces(MimeTypes.APPLICATION_JSON_UTF8)
    public String getConfigurationAccessor() {
        ClassAccessor classAccessor = getConfigurationClassAccessor();
        if(classAccessor == null) {
            return null;
        }
        JSONStringer jsonStringer = new JSONStringer();
        ReflectionUtil.classAccessorToJson(classAccessor, jsonStringer);
        return jsonStringer.toString();
    }


    ////////////////
    // Configuration
    ////////////////

    @io.swagger.v3.oas.annotations.Operation(
            operationId = "com.manydesigns.portofino.resourceactions.AbstractResourceAction#getActionPermissions",
            description = "An object describing the permissions on this resource; both currently active permissions and supported values.")
    @GET
    @Path(":permissions")
    @Produces(MimeTypes.APPLICATION_JSON_UTF8)
    public Map<String, Object> getActionPermissions() {
        List<Group> allGroups = new ArrayList<>(getActionDescriptor().getPermissions().getGroups());
        Set<String> possibleGroups = ShiroUtils.getPortofinoRealm().getGroups();
        for(String group : possibleGroups) {
            if(allGroups.stream().noneMatch(g -> group.equals(g.getName()))) {
                Group emptyGroup = new Group();
                emptyGroup.setName(group);
                allGroups.add(emptyGroup);
            }
        }
        ActionInstance parentActionInstance = getActionInstance().getParent();
        allGroups.forEach(g -> {
            if(g.getAccessLevel() == null) {
                if(parentActionInstance != null) {
                    Permissions parentPermissions =
                            SecurityLogic.calculateActualPermissions(parentActionInstance);
                    g.setActualAccessLevel(parentPermissions.getActualLevels().get(g.getName()));
                }
            }
        });
        Map<String, Object> result = new HashMap<>();
        result.put("groups", allGroups);
        result.put("permissions", getSupportedPermissions());
        return result;
    }

    @io.swagger.v3.oas.annotations.Operation(
            operationId = "com.manydesigns.portofino.resourceactions.AbstractResourceAction#setActionPermissions",
            description = "Set the permissions about this resource.")
    @RequiresAdministrator
    @PUT
    @Path(":permissions")
    @Consumes(MimeTypes.APPLICATION_JSON_UTF8)
    public void setActionPermissions(
            @RequestBody(description = "An array of permissions, one for each user group. Each element of the array " +
                    "has a group name, a desired access level (null means inherited) and a list of action-specific permissions.")
            List<Group> groups) throws Exception {
        List<Group> existingGroups = getActionDescriptor().getPermissions().getGroups();
        existingGroups.clear();
        existingGroups.addAll(groups);
        FileObject saved = ActionLogic.saveActionDescriptor(actionInstance);
        logger.info("Saved permissions to " + saved.getName().getPath());
    }

    public String[] getSupportedPermissions() {
        Class<?> actualActionClass = getActionInstance().getActionClass();
        SupportsPermissions supportsPermissions = actualActionClass.getAnnotation(SupportsPermissions.class);
        while(supportsPermissions == null && actualActionClass.getSuperclass() != Object.class) {
            actualActionClass = actualActionClass.getSuperclass();
            supportsPermissions = actualActionClass.getAnnotation(SupportsPermissions.class);
        }
        if(supportsPermissions != null && supportsPermissions.value().length > 0) {
            return supportsPermissions.value();
        } else {
            return new String[0];
        }
    }

}
