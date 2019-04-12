package com.manydesigns.portofino.upstairs.actions.actions;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.text.OgnlTextFormat;
import com.manydesigns.elements.util.RandomUtil;
import com.manydesigns.elements.util.ReflectionUtil;
import com.manydesigns.portofino.dispatcher.Resource;
import com.manydesigns.portofino.dispatcher.WithParameters;
import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.pageactions.ConfigurationWithDefaults;
import com.manydesigns.portofino.pageactions.PageAction;
import com.manydesigns.portofino.pageactions.PageInstance;
import com.manydesigns.portofino.pageactions.registry.ActionInfo;
import com.manydesigns.portofino.pages.Page;
import com.manydesigns.portofino.pages.PageLogic;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresAdministrator;
import com.manydesigns.portofino.security.SecurityLogic;
import ognl.OgnlContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

@RequiresAdministrator
public class ActionsAction extends AbstractPageAction {

    public static final String PORTOFINO_PAGE_MOVE_TYPE = "application/vnd.com.manydesigns.portofino.page-move";

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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> describe() {
        return getPageAction().describe();
    }

    protected AbstractPageAction getPageAction() {
        String actionPath = StringUtils.join(parameters, "/");
        Resource resource = getResource(actionPath);
        if(resource instanceof AbstractPageAction) {
            return ((AbstractPageAction) resource);
        } else {
            logger.error("Not a PageAction: " + resource);
            throw new WebApplicationException();
        }
    }

    @POST
    public void create(String actionClassName) throws Exception {
        String actionPath = StringUtils.join(parameters.subList(0, parameters.size() - 1), "/");
        String segment = parameters.get(parameters.size() - 1);
        PageInstance parentPageInstance = getPageInstance(actionPath);
        PageAction parent = parentPageInstance.getActionBean();

        Class actionClass = codeBase.loadClass(actionClassName);
        ActionInfo info = actionRegistry.getInfo(actionClass);
        String scriptTemplate = info.scriptTemplate;
        Class<?> configurationClass = info.configurationClass;
        boolean supportsDetail = info.supportsDetail;

        String className = RandomUtil.createRandomId();
        if(Character.isDigit(className.charAt(0))) {
            className = "_" + className;
        }
        OgnlContext ognlContext = ElementsThreadLocals.getOgnlContext();
        ognlContext.put("generatedClassName", className);
        ognlContext.put("actionClassName", actionClassName);
        String script = OgnlTextFormat.format(scriptTemplate, parent);

        Page page = new Page();
        Object configuration = null;
        if(configurationClass != null) {
            configuration = ReflectionUtil.newInstance(configurationClass);
            if(configuration instanceof ConfigurationWithDefaults) {
                ((ConfigurationWithDefaults) configuration).setupDefaults();
            }
        }
        page.init();

        FileObject directory = parentPageInstance.getChildPageDirectory(segment);
        if(directory.exists()) {
            logger.error("Can't create page - directory {} exists", directory.getName().getPath());
            throw new WebApplicationException("error.creating.page.the.directory.already.exists");
        }
        directory.createFolder();
        logger.debug("Creating the new child page in directory: {}", directory);
        PageLogic.savePage(directory, page);
        if(configuration != null) {
            PageLogic.saveConfiguration(directory, configuration);
        }
        FileObject groovyScriptFile = directory.resolveFile("action.groovy");
        groovyScriptFile.createFile();
        try(Writer w = new OutputStreamWriter(groovyScriptFile.getContent().getOutputStream())) {
            w.write(script);
        }
        if(supportsDetail) {
            FileObject detailDir = directory.resolveFile(PageInstance.DETAIL);
            logger.debug("Creating _detail directory: {}", detailDir);
            detailDir.createFolder();
        }
    }

    @POST
    @Consumes(PORTOFINO_PAGE_MOVE_TYPE)
    public void move(String sourceActionPath) throws FileSystemException {
        String actionPath = StringUtils.join(parameters.subList(0, parameters.size() - 1), "/");
        String segment = parameters.get(parameters.size() - 1);
        copyOrMovePage(sourceActionPath, actionPath, segment, true);
    }

    @DELETE
    public void delete() throws Exception {
        String actionPath = StringUtils.join(parameters, "/");
        PageInstance pageInstance = getPageInstance(actionPath);
        FileObject directory = pageInstance.getDirectory();
        if(!directory.exists()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        directory.deleteAll();
    }

    public PageInstance getPageInstance(String actionPath) {
        PageAction action = (PageAction) getResource(actionPath);
        if(action == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        PageInstance pageInstance = action.getPageInstance();
        checkPermissions(pageInstance);
        return pageInstance;
    }

    protected void copyOrMovePage(
            String sourceActionPath, String destinationParentActionPath, String segment, boolean move) throws FileSystemException {
        PageInstance sourcePageInstance = getPageInstance(sourceActionPath);
        PageInstance destinationParentPageInstance = getPageInstance(destinationParentActionPath);
        FileObject newChild = destinationParentPageInstance.getChildPageDirectory(segment);

        if(move) {
            if(sourceActionPath.equals("/") || sourceActionPath.isEmpty()) {
                throw new WebApplicationException("Cannot move the root action!");
            }
            sourcePageInstance.getDirectory().moveTo(newChild);
        } else {
            newChild.copyFrom(sourcePageInstance.getDirectory(), new AllFileSelector());
        }
    }

    public void checkPermissions(PageInstance pageInstance) {
        if (!checkPermissionsOnTargetPage(pageInstance)) {
            Response.Status status =
                    SecurityUtils.getSubject().isAuthenticated() ?
                            Response.Status.FORBIDDEN :
                            Response.Status.UNAUTHORIZED;
            throw new WebApplicationException(status);
        }
    }

    //Page create/delete/move etc.
    protected boolean checkPermissionsOnTargetPage(PageInstance targetPageInstance) {
        return checkPermissionsOnTargetPage(targetPageInstance, AccessLevel.DEVELOP);
    }

    protected boolean checkPermissionsOnTargetPage(PageInstance targetPageInstance, AccessLevel accessLevel) {
        Subject subject = SecurityUtils.getSubject();
        if(!SecurityLogic.hasPermissions(portofinoConfiguration, targetPageInstance, subject, accessLevel)) {
            logger.warn("User not authorized modify page {}", targetPageInstance);
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
