/*
 * Copyright (C) 2005-2017 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.pageactions;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.util.MimeTypes;
import com.manydesigns.portofino.buttons.ButtonInfo;
import com.manydesigns.portofino.buttons.ButtonsLogic;
import com.manydesigns.portofino.buttons.GuardType;
import com.manydesigns.portofino.dispatcher.AbstractResource;
import com.manydesigns.portofino.dispatcher.AbstractResourceWithParameters;
import com.manydesigns.portofino.dispatcher.Resource;
import com.manydesigns.portofino.operations.Operation;
import com.manydesigns.portofino.operations.Operations;
import com.manydesigns.portofino.pages.Page;
import com.manydesigns.portofino.pages.PageLogic;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresPermissions;
import com.manydesigns.portofino.security.SecurityLogic;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Convenient abstract base class for PageActions. It has fields to hold values of properties specified by the
 * PageAction interface as well as other useful objects injected by the framework. It provides standard
 * implementations of many of the PageAction methods, as well as important utility methods to handle hierarchical
 * relations among pages, such as embedding.
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@RequiresPermissions(level = AccessLevel.VIEW)
public abstract class AbstractPageAction extends AbstractResourceWithParameters implements PageAction {
    public static final String copyright =
        "Copyright (C) 2005-2017 ManyDesigns srl";

    public static final String[][] PAGE_CONFIGURATION_FIELDS =
            {{"id", "title", "description", "template", "detailTemplate", "applyTemplateRecursively"}};
    public static final String[][] PAGE_CONFIGURATION_FIELDS_NO_DETAIL =
            {{"id", "title", "description", "template", "applyTemplateRecursively"}};
    public static final String CONF_FORM_PREFIX = "config";

    //--------------------------------------------------------------------------
    // Properties
    //--------------------------------------------------------------------------

    /**
     * The PageInstance property. Injected.
     */
    public PageInstance pageInstance;

    /**
     * The global configuration object. Injected.
     */
    @Autowired
    public Configuration portofinoConfiguration;

    @Context
    protected UriInfo uriInfo;

    //**************************************************************************
    // Scripting
    //**************************************************************************

    /**
     * The Groovy script for this page.
     */
    protected String script;

    //**************************************************************************
    // Page configuration
    //**************************************************************************

    /**
     * The Form to configure standard page settings.
     */
    public Form pageConfigurationForm;

    /**
     * The context object holds various elements of contextual information such
     * as the HTTP request and response objects.
     */
    protected ActionContext context;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(AbstractPageAction.class);

    protected AbstractPageAction() {
        maxParameters = PageActionLogic.supportsDetail(getClass()) ? Integer.MAX_VALUE : 0;
    }

    //--------------------------------------------------------------------------
    // Admin methods
    //--------------------------------------------------------------------------

    /**
     * Utility method to save the configuration object to a file in this page's directory.
     * @param configuration the object to save. It must be in a state that will produce a valid XML document.
     * @return true if the object was correctly saved, false otherwise.
     */
    protected boolean saveConfiguration(Object configuration) {
        try {
            FileObject confFile = PageLogic.saveConfiguration(pageInstance.getDirectory(), configuration);
            logger.info("Configuration saved to " + confFile.getName().getPath());
            return true;
        } catch (Exception e) {
            logger.error("Couldn't save configuration", e);
            SessionMessages.addErrorMessage("error saving conf");
            return false;
        }
    }

    @Override
    protected void initSubResource(Resource resource) {
        super.initSubResource(resource);
        if(resource instanceof PageAction) {
            initPageAction((PageAction) resource, getPageInstance(), uriInfo);
        }
    }

    public static void initPageAction(PageAction pageAction, PageInstance parentPageInstance, UriInfo uriInfo) {
        HttpServletRequest request = ElementsThreadLocals.getHttpServletRequest();
        HttpServletResponse response = ElementsThreadLocals.getHttpServletResponse();
        Page page;
        try {
            page = PageLogic.getPage(pageAction.getLocation());
        } catch (PageNotActiveException e) {
            logger.debug("page.xml not found or not valid", e);
            page = new Page();
            page.setTitle("");
            page.init();
        }
        PageInstance pageInstance = new PageInstance(
                parentPageInstance, pageAction.getLocation(), page, (Class<? extends PageAction>) pageAction.getClass());
        pageInstance.setActionBean(pageAction);
        PageLogic.configurePageAction(pageAction, pageInstance);
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
        pageAction.setContext(context);
    }

    @Override
    public void prepareForExecution() {

    }

    @Override
    protected void parametersAcquired() throws WebApplicationException {
        pageInstance.getParameters().addAll(parameters);
    }

    @Override
    public FileObject getChildrenLocation() throws FileSystemException {
        if(parameters.isEmpty()) {
            return getLocation();
        } else {
            return getLocation().getChild(PageInstance.DETAIL);
        }
    }

    @Override
    public PageAction getParent() {
        return (PageAction) super.getParent();
    }

    public String getActionPath() {
        return context.getActionPath();
    }

    //--------------------------------------------------------------------------
    // Getters/Setters
    //--------------------------------------------------------------------------

    public Form getPageConfigurationForm() {
        return pageConfigurationForm;
    }

    public void setPageConfigurationForm(Form pageConfigurationForm) {
        this.pageConfigurationForm = pageConfigurationForm;
    }

    @Override
    public PageInstance getPageInstance() {
        return pageInstance;
    }

    @Override
    public void setPageInstance(PageInstance pageInstance) {
        this.pageInstance = pageInstance;
    }

    public Page getPage() {
        return getPageInstance().getPage();
    }

    //--------------------------------------------------------------------------
    // Page configuration
    //--------------------------------------------------------------------------

    /**
     * Sets up the Elements form(s) 
     */
    protected void prepareConfigurationForms() {
        Page page = pageInstance.getPage();

        PageInstance parent = pageInstance.getParent();
        assert parent != null;

        FormBuilder formBuilder = new FormBuilder(EditPage.class)
                .configPrefix(CONF_FORM_PREFIX)
                .configFields(PageActionLogic.supportsDetail(getClass()) ?
                              PAGE_CONFIGURATION_FIELDS :
                              PAGE_CONFIGURATION_FIELDS_NO_DETAIL)
                .configFieldSetNames("Page");

        pageConfigurationForm = formBuilder.build();
        EditPage edit = new EditPage();
        edit.id = page.getId();
        edit.title = page.getTitle();
        edit.description = page.getDescription();
        pageConfigurationForm.readFromObject(edit);
//
//        if(script == null) {
//            prepareScript();
//        }
    }

    /**
     * Reads the page configuration form values from the request. Can be called to re-use the standard page
     * configuration form.
     */
    protected void readPageConfigurationFromRequest() {
        pageConfigurationForm.readFromRequest(context.getRequest());
    }

    /**
     * Validates the page configuration form values. Can be called to re-use the standard page
     * configuration form.
     * @return true iff the form was valid.
     */
    protected boolean validatePageConfiguration() {
        return pageConfigurationForm.validate();
    }

//    /**
//     * Updates the page with values from the page configuration. Can be called to re-use the standard page
//     * configuration form. Should be called only after validatePageConfiguration() returned true.
//     * @return true iff the page was correctly saved.
//     */
//    protected boolean updatePageConfiguration() {
//        EditPage edit = new EditPage();
//        pageConfigurationForm.writeToObject(edit);
//        Page page = pageInstance.getPage();
//        page.setTitle(edit.title);
//        page.setDescription(edit.description);
//        page.getLayout().setTemplate(edit.template);
//        page.getDetailLayout().setTemplate(edit.detailTemplate);
//        try {
//            File pageFile = PageLogic.savePage(pageInstance.getDirectory(), page);
//            logger.info("Page saved to " + pageFile.getAbsolutePath());
//        } catch (Exception e) {
//            logger.error("Couldn't save page", e);
//            return false; //TODO handle return value + script + session msg
//        }
//        if(edit.applyTemplateRecursively) {
//            FileFilter filter = new FileFilter() {
//                public boolean accept(File pathname) {
//                    return pathname.isDirectory();
//                }
//            };
//            updateTemplate(pageInstance.getDirectory(), filter, edit);
//        }
//        Subject subject = SecurityUtils.getSubject();
//        if(SecurityLogic.hasPermissions(portofinoConfiguration, getPageInstance(), subject, AccessLevel.DEVELOP)) {
//            updateScript();
//        }
//        return true;
//    }

    //--------------------------------------------------------------------------
    // Scripting
    //--------------------------------------------------------------------------

//    protected void prepareScript() {
//        String pageId = pageInstance.getPage().getId();
//        File file = ScriptingUtil.getGroovyScriptFile(pageInstance.getDirectory(), "action");
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
//        File directory = pageInstance.getDirectory();
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
//            String pageId = pageInstance.getPage().getId();
//            logger.warn("Couldn't compile script for page " + pageId, e);
//            SessionMessages.addErrorMessage(ElementsThreadLocals.getText("couldnt.compile.script"));
//        } finally {
//            IOUtils.closeQuietly(fw);
//        }
//    }

    public Map getOgnlContext() {
        return ElementsThreadLocals.getOgnlContext();
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
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

    //--------------------------------------------------------------------------
    // Utitilities
    //--------------------------------------------------------------------------

    /**
     * Returns an error response with message saying that the pageaction is not properly
     * configured.
     */
    public Response pageActionNotConfigured() {
        return Response.serverError().entity("page-action-not-configured").build();
    }

    @Path(":buttons")
    @GET
    @Produces(MimeTypes.APPLICATION_JSON_UTF8)
    @Deprecated
    public List getButtons() {
        HttpServletRequest request = context.getRequest();
        String list = request.getParameter("list");
        List<ButtonInfo> buttons = ButtonsLogic.getButtonsForClass(getClass(), list);
        List result = new ArrayList();
        Subject subject = SecurityUtils.getSubject();
        for(ButtonInfo button : buttons) {
            logger.trace("ButtonInfo: {}", button);
            Method handler = button.getMethod();
            boolean isAdmin = SecurityLogic.isAdministrator(request);
            if(!isAdmin &&
               ((pageInstance != null && !SecurityLogic.hasPermissions(
                       portofinoConfiguration, button.getMethod(), button.getFallbackClass(), pageInstance, subject)) ||
                !SecurityLogic.satisfiesRequiresAdministrator(request, this, handler))) {
                continue;
            }
            boolean visible = ButtonsLogic.doGuardsPass(this, handler, GuardType.VISIBLE);
            if(!visible) {
                continue;
            }
            boolean enabled = ButtonsLogic.doGuardsPass(this, handler, GuardType.ENABLED);
            Map<String, Object> buttonData = new HashMap<String, Object>();
            buttonData.put("list", button.getButton().list());
            buttonData.put("group", button.getButton().group());
            buttonData.put("icon", button.getButton().icon());
            buttonData.put("iconBefore", button.getButton().iconBefore());
            buttonData.put("text", ElementsThreadLocals.getText(button.getButton().key()));
            buttonData.put("order", button.getButton().order());
            buttonData.put("type", button.getButton().type());
            buttonData.put("method", button.getMethod().getName());
            buttonData.put("enabled", enabled);
            result.add(buttonData);
        }
        return result;
    }

    /**
     * Returns the list of operations that can be invoked via REST on this resource.
     * @return the list of operations.
     */
    @ApiOperation(
        nickname =
            "com.manydesigns.portofino.pageactions.AbstractPageAction#describeOperations",
        value =
            "Returns the list of operations that can be invoked via REST on this resource. " +
            "If the user doesn't have permission to invoke an operation, or a VISIBLE guard " +
            "doesn't pass, then the operation is excluded from the result. If an ENABLED guard " +
            "doesn't pass, the operation is included, but it is marked as not available.")
    @ApiResponses({ @ApiResponse(
            code = 200, message = "A list of operations (name, signature, available).")})
    @Path(":operations")
    @GET
    @Produces(MimeTypes.APPLICATION_JSON_UTF8)
    public List describeOperations() {
        HttpServletRequest request = context.getRequest();
        List<Operation> operations = Operations.getOperations(getClass());
        List result = new ArrayList();
        Subject subject = SecurityUtils.getSubject();
        for(Operation operation : operations) {
            logger.trace("ButtonInfo: {}", operation);
            Method handler = operation.getMethod();
            boolean isAdmin = SecurityLogic.isAdministrator(request);
            if(!isAdmin &&
                    ((pageInstance != null && !SecurityLogic.hasPermissions(
                            portofinoConfiguration, operation.getMethod(), getClass(), pageInstance, subject)) ||
                            !SecurityLogic.satisfiesRequiresAdministrator(request, this, handler))) {
                continue;
            }
            boolean visible = ButtonsLogic.doGuardsPass(this, handler, GuardType.VISIBLE);
            if(!visible) {
                continue;
            }
            boolean available = ButtonsLogic.doGuardsPass(this, handler, GuardType.ENABLED);
            Map<String, Object> operationInfo = new HashMap<>();
            operationInfo.put("name", operation.getName());
            operationInfo.put("signature", operation.getSignature());
            operationInfo.put("available", available);
            result.add(operationInfo);
        }
        return result;
    }

    @Path(":configuration")
    @GET
    @Produces(MimeTypes.APPLICATION_JSON_UTF8)
    public Object getConfiguration() {
        return pageInstance.getConfiguration();
    }

    @Override
    protected void describe(Map<String, Object> description) {
        super.describe(description);
        description.put("page", pageInstance.getPage());
    }

    /**
     * Returns a description of this PageAction.
     * @since 4.2.1
     * @return the page's description as JSON.
     * @deprecated replaced by :description.
     * @see #describe(Map)
     * @see AbstractResource#getJSONDescription()
     */
    @Path(":page")
    @GET
    @Produces(MimeTypes.APPLICATION_JSON_UTF8)
    @Deprecated
    public Map<String, Object> getPageDescription() {
        Map<String, Object> description = new HashMap<String, Object>();
        description.put("superclass", pageInstance.getActionClass().getSuperclass().getName());
        description.put("class", pageInstance.getActionClass().getName());
        description.put("page", pageInstance.getPage());
        return description;
    }
}