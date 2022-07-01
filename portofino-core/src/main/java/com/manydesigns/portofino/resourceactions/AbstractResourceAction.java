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
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.FilteredClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.util.MimeTypes;
import com.manydesigns.elements.util.ReflectionUtil;
import com.manydesigns.portofino.ResourceActionsModule;
import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.config.ConfigurationSource;
import com.manydesigns.portofino.dispatcher.AbstractResourceWithParameters;
import com.manydesigns.portofino.dispatcher.Resource;
import com.manydesigns.portofino.model.Domain;
import com.manydesigns.portofino.model.service.ModelService;
import com.manydesigns.portofino.operations.GuardType;
import com.manydesigns.portofino.operations.Operation;
import com.manydesigns.portofino.operations.Operations;
import com.manydesigns.portofino.security.*;
import com.manydesigns.portofino.security.noop.NoSecurity;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONStringer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Convenient abstract base class for ResourceActions. It has fields to hold values of properties specified by the
 * ResourceAction interface as well as other useful objects injected by the framework. It provides standard
 * implementations of many of the ResourceAction methods.
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
    public ConfigurationSource portofinoConfiguration;
    @Autowired
    protected CodeBase codeBase;
    @Autowired
    public ApplicationContext applicationContext;
    @Autowired
    public ModelService modelService;
    @Autowired
    @Qualifier(ResourceActionsModule.ACTIONS_DOMAIN)
    public Domain actionsDomain;
    @Autowired
    @Qualifier(ResourceActionsModule.ACTIONS_DIRECTORY)
    public FileObject actionsDirectory;
    @Autowired(required = false)
    protected SecurityFacade security = NoSecurity.AT_ALL;
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
        maxParameters = ResourceActionSupport.supportsDetail(getClass()) ? Integer.MAX_VALUE : 0;
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

    public static void initResourceAction(
            ResourceAction resourceAction, ActionInstance parentActionInstance, UriInfo uriInfo) {
        if(resourceAction.getActionInstance() == null) {
            ActionInstance actionInstance = new ActionInstance(
                    parentActionInstance, resourceAction.getLocation(), resourceAction.getClass());
            actionInstance.setActionBean(resourceAction);
            resourceAction.setActionInstance(actionInstance);
            try {
                if (resourceAction.loadConfiguration() != null) {
                    resourceAction.configured();
                }
            } catch (Exception e) {
                logger.error("Could not load configuration for " + resourceAction.getPath(), e);
            }
        }

        HttpServletRequest request = ElementsThreadLocals.getHttpServletRequest();
        HttpServletResponse response = ElementsThreadLocals.getHttpServletResponse();
        ActionContext context = new ActionContext();
        context.setRequest(request);
        context.setResponse(response);
        context.setServletContext(ElementsThreadLocals.getServletContext());
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
    protected FileObject getChildLocation(String pathSegment) throws FileSystemException {
        Optional<AdditionalChild> child = getAdditionalChild(pathSegment);
        if(child.isPresent()) {
            return VFS.getManager().resolveFile(child.get().getPath());
        }
        return super.getChildLocation(pathSegment);
    }

    @NotNull
    protected Optional<AdditionalChild> getAdditionalChild(String pathSegment) {
        return getConfiguration().getAdditionalChildren().stream()
                .filter(c -> c.getSegment().equals(pathSegment))
                .findFirst();
    }

    @Override
    public Collection<String> getSubResources() {
        Collection<String> subResources = super.getSubResources();
        getConfiguration().getAdditionalChildren().forEach(c -> {
            if(!subResources.contains(c.getSegment())) {
                subResources.add(c.getSegment());
            }
        });
        return subResources;
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

    @Override
    public void configured() {}

    public Map getOgnlContext() {
        return ElementsThreadLocals.getOgnlContext();
    }

    public ConfigurationSource getPortofinoConfiguration() {
        return portofinoConfiguration;
    }

    @Override
    public SecurityFacade getSecurity() {
        return security;
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
     * Returns an error response with message saying that the resource-action is not properly
     * configured.
     * @return the {@link Response}.
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
    public List<Map<String, Object>> describeOperations() {
        HttpServletRequest request = context.getRequest();
        List<Operation> operations = Operations.getOperations(getClass());
        List<Map<String, Object>> result = new ArrayList<>();
        for(Operation operation : operations) {
            logger.trace("Operation: {}", operation);
            Method handler = operation.getMethod();
            if (!security.isOperationAllowed(
                    this, portofinoConfiguration.getProperties(), request, operation, handler)) {
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
        if(ResourceActionSupport.supportsDetail(getClass())) {
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
                    "Returns true if this action is accessible, and an HTTP 40x error if it's not. " +
                    "Clients can use this method to check if the action is accessible without invoking any " +
                    "other operations.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The string true, if the action is accessible."),
            @ApiResponse(responseCode = "401", description = "If the action is not accessible and the request is not authenticated."),
            @ApiResponse(responseCode = "403", description = "If the action is not accessible for the authenticated user.")
    })
    @Path(":accessible")
    @GET
    public boolean isAccessible() {
        try {
            return security.isOperationAllowed(context.request, actionInstance, this, getClass().getMethod("isAccessible"));
        } catch (NoSuchMethodException e) {
            return true;
        }
    }

    @Override
    @io.swagger.v3.oas.annotations.Operation(
            operationId =
                    "com.manydesigns.portofino.resourceactions.AbstractResourceAction#getAccessibleChildren",
            description =
                    "Returns the list of accessible children.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The the list of accessible children, if the action itself is accessible."),
            @ApiResponse(responseCode = "401", description = "If the action is not accessible and the request is not authenticated."),
            @ApiResponse(responseCode = "403", description = "If the action is not accessible for the authenticated user.")
    })
    @Path(":accessible-children")
    @GET
    public List<String> getAccessibleChildren() {
        return getSubResources().stream().filter(this::isChildResourceAccessible).collect(Collectors.toList());
    }

    private boolean isChildResourceAccessible(String segment) {
        try {
            Object subResource = getSubResource(segment);
            if(subResource instanceof ResourceAction) {
                return ((ResourceAction) subResource).isAccessible();
            } else {
                return true;
            }
        } catch (Exception e) {
            logger.debug("Inaccessible sub-resource: " + segment, e);
            return false;
        }
    }

    ////////////////
    // Configuration
    ////////////////

    @Nullable
    protected Class<? extends ResourceActionConfiguration> getConfigurationClass() {
        return ResourceActionSupport.getConfigurationClass(getClass());
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
    public Object getSerializedConfiguration() {
        Object configuration = getConfiguration();
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
        List<String> excluded = new ArrayList<>();
        for(PropertyAccessor property : classAccessor.getProperties()) {
            RequiresPermissions requiresPermissions = property.getAnnotation(RequiresPermissions.class);
            Configuration conf = getPortofinoConfiguration().getProperties();
            boolean permitted =
                    requiresPermissions == null ||
                    security.hasPermissions(conf, permissions, requiresPermissions);
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
            String configurationString) throws Exception {
        Class<? extends ResourceActionConfiguration> configurationClass =
                ResourceActionSupport.getConfigurationClass(getClass());
        if(configurationClass == null) {
            throw new WebApplicationException("This resource does not support configuration");
        }
        ResourceActionConfiguration configuration =
                new ObjectMapper().readValue(configurationString, configurationClass);
        actionInstance.setConfiguration(configuration);
        saveConfiguration();
    }

    /**
     * Utility method to save the configuration object to the model.
     * It must be in a state that will produce a valid XML document.
     */
    public void saveConfiguration() throws Exception {
        Domain domain = getConfigurationDomain();
        domain.putObject("configuration", getConfiguration(), modelService.getClassesDomain());
        modelService.saveObject(domain, "configuration");
    }

    public Domain getConfigurationDomain() {
        Domain parentDomain;
        if (parent instanceof ResourceAction) {
            ResourceAction parentAction = (ResourceAction) parent;
            parentDomain = parentAction.getConfigurationDomain();
            if (!parentAction.getActionInstance().getParameters().isEmpty()) {
                parentDomain = parentDomain.ensureDomain(ActionInstance.DETAIL);
            }
        } else {
            parentDomain = actionsDomain;
        }
        return parentDomain.ensureDomain(getSegment());
    }

    public ResourceActionConfiguration loadConfiguration() throws Exception {
        Object configuration = modelService.getJavaObject(getConfigurationDomain(), "configuration");
        if (configuration != null) {
            if (!(configuration instanceof ResourceActionConfiguration)) {
                throw new RuntimeException(
                        "Action configuration for " + this + " is not of the right type: " + configuration);
            }
            actionInstance.setConfiguration((ResourceActionConfiguration) configuration);
        } else {
            // Try loading legacy action.xml and configuration.xml
            ResourceActionSupport.legacyConfigureResourceAction(this, actionInstance);
            if (getConfiguration() != null && actionInstance.getDirectory() != null) {
                FileObject oldActionXml = actionInstance.getDirectory().resolveFile("action.xml");
                FileObject oldConf = actionInstance.getDirectory().resolveFile("configuration.xml");
                try {
                    saveConfiguration();
                    logger.info("Migrated configuration.xml and action.xml from " +
                            actionInstance.getDirectory().getPath() + ", deleting");
                    oldConf.delete();
                    oldActionXml.delete();
                } catch (Exception e) {
                    logger.error("Could not migrate configuration.xml and action.xml from " +
                            actionInstance.getDirectory().getPath());
                }
            }
            configuration = getConfiguration();
        }
        if (configuration == null) {
            ResourceActionConfiguration actionConfiguration = new ResourceActionConfiguration();
            actionConfiguration.init();
            configuration = actionConfiguration;
            actionInstance.setConfiguration(actionConfiguration);
        }
        if (applicationContext != null) {
            applicationContext.getAutowireCapableBeanFactory().autowireBean(configuration);
        }
        ((ResourceActionConfiguration) configuration).init();
        return (ResourceActionConfiguration) configuration;
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
        List<Group> allGroups = new ArrayList<>(getConfiguration().getPermissions().getGroups());
        Set<String> possibleGroups = security.getGroups();
        for(String group : possibleGroups) {
            if(allGroups.stream().noneMatch(g -> group.equals(g.getName()))) {
                Group emptyGroup = new Group();
                emptyGroup.setName(group);
                allGroups.add(emptyGroup);
            }
        }
        ActionInstance parentActionInstance = getActionInstance().getParent();
        allGroups.forEach(g -> {
            if(g.getAccessLevelName() == null) {
                if(parentActionInstance != null) {
                    Permissions parentPermissions =
                            SecurityLogic.calculateActualPermissions(parentActionInstance);
                    g.setAccessLevel(parentPermissions.getActualLevels().get(g.getName()));
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
        List<Group> existingGroups = getConfiguration().getPermissions().getGroups();
        existingGroups.clear();
        existingGroups.addAll(groups);
        saveConfiguration();
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

    //Mount points
    @Override
    public void mount(@NotNull String segment, @NotNull String path) throws Exception {
        if (getConfiguration() == null) {
            loadConfiguration();
        }
        Optional<AdditionalChild> existing =
                getConfiguration().getAdditionalChildren().stream().filter(
                        c -> c.getSegment().equals(segment)
                ).findFirst();
        if(existing.isPresent()) {
            String existingPath = existing.get().getPath();
            if(!path.equals(existingPath)) {
                throw new IllegalArgumentException("Another path is already mounted at " + segment + ": " + existingPath);
            }
        } else {
            AdditionalChild child = new AdditionalChild();
            child.setSegment(segment);
            child.setPath(path);
            getConfiguration().getAdditionalChildren().add(child);
            saveConfiguration();
            logger.info("Mounted " + segment + " --> " + path + " at " + getLocation());
        }
    }

    @Override
    public void unmount(String segment) throws Exception {
        ResourceActionConfiguration descriptor = getConfiguration();
        if (descriptor == null) {
            descriptor = loadConfiguration();
        }
        if (descriptor != null) {
            Optional<AdditionalChild> existing =
                    descriptor.getAdditionalChildren().stream().filter(c -> c.getSegment().equals(segment)).findFirst();
            if (existing.isPresent()) {
                descriptor.getAdditionalChildren().remove(existing.get());
                saveConfiguration();
            }
        }
    }

}
