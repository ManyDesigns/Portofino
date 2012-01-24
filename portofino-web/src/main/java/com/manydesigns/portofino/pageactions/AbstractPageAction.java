package com.manydesigns.portofino.pageactions;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.ognl.OgnlUtils;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.text.TextFormat;
import com.manydesigns.portofino.ApplicationAttributes;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.dispatcher.*;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.ModelObject;
import com.manydesigns.portofino.pages.*;
import com.manydesigns.portofino.navigation.ResultSetNavigation;
import com.manydesigns.portofino.scripting.ScriptingUtil;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.stripes.ModelActionResolver;
import com.manydesigns.portofino.system.model.users.annotations.RequiresPermissions;
import groovy.lang.GroovyObject;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.controller.StripesConstants;
import net.sourceforge.stripes.controller.StripesFilter;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

@RequiresPermissions(level = AccessLevel.VIEW)
public abstract class AbstractPageAction extends AbstractActionBean implements PageAction {
    public static final String DEFAULT_LAYOUT_CONTAINER = "default";
    public static final String[][] PAGE_CONFIGURATION_FIELDS =
            {{"id", "subtreeRoot", "layout", "detailLayout", "description"}};
    public static final String[][] PAGE_CONFIGURATION_FIELDS_NO_DETAIL =
            {{"id", "subtreeRoot", "layout", "description"}};
    public static final String PAGE_PORTLET_NOT_CONFIGURED = "/layouts/portlet-not-configured.jsp";
    public static final String PORTOFINO_PORTLET_EXCEPTION = "portofino.portlet.exception";

    public static final String CONF_FORM_PREFIX = "config";
    public static final String DEFAULT_LAYOUT = "/layouts/portlet/portlet-page-1-2-1-symmetric.jsp";

    //--------------------------------------------------------------------------
    // Properties
    //--------------------------------------------------------------------------

    @Inject(RequestAttributes.DISPATCH)
    public Dispatch dispatch;

    public PageInstance pageInstance;

    @Inject(RequestAttributes.APPLICATION)
    public Application application;

    @Inject(RequestAttributes.MODEL)
    public Model model;

    @Inject(ApplicationAttributes.PORTOFINO_CONFIGURATION)
    public Configuration portofinoConfiguration;

    //--------------------------------------------------------------------------
    // UI
    //--------------------------------------------------------------------------

    public final MultiMap portlets = new MultiHashMap();
    public String returnToParentTarget;
    public final Map<String, String> returnToParentParams = new HashMap<String, String>();

    //--------------------------------------------------------------------------
    // Navigation
    //--------------------------------------------------------------------------

    protected ResultSetNavigation resultSetNavigation;
    public String cancelReturnUrl;

    //**************************************************************************
    // Scripting
    //**************************************************************************

    protected String script;

    //**************************************************************************
    // Page configuration
    //**************************************************************************

    public String title;
    public Form pageConfigurationForm;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(AbstractPageAction.class);

    public boolean isEmbedded() {
        return getContext().getRequest().getAttribute(
                StripesConstants.REQ_ATTR_INCLUDE_PATH) != null;
    }

    public Resolution prepare(PageInstance pageInstance, ActionBeanContext context) {
        this.pageInstance = pageInstance;
        return null;
    }

    /*protected void dereferencePageInstance() {
        if(pageInstance != null) {
            pageInstance = pageInstance.dereference();
        }
    }*/

    public boolean supportsParameters() {
        return false;
    }

    public String getDescription() {
        return pageInstance.getName();
    }

    public void setupReturnToParentTarget() {
        PageInstance[] pageInstancePath =
                dispatch.getPageInstancePath();
        boolean hasPrevious = !getPage().isSubtreeRoot() && pageInstancePath.length > 1;
        returnToParentTarget = null;
        if (hasPrevious) {
            int previousPos = pageInstancePath.length - 2;
            PageInstance previousPageInstance = pageInstancePath[previousPos];
            //Page previousPage = previousPageInstance.getPage();

            //TODO ripristinare
            /*if(!previousPage.isShowInNavigation()) {
                return;
            }*/

            PageAction actionBean = previousPageInstance.getActionBean();
            if(actionBean != null) {
                returnToParentTarget = actionBean.getDescription();
            }
        }
    }

    //--------------------------------------------------------------------------
    // Admin methods
    //--------------------------------------------------------------------------

    protected void saveConfiguration() {
        Object configuration = pageInstance.getConfiguration();
        if(configuration instanceof ModelObject) {
            Model model = application.getModel();
            if(model != null) {
                model.init((ModelObject) configuration);
            } else {
                logger.error("Model is null, cannot init configuration");
                SessionMessages.addErrorMessage("error saving conf");
                return;
            }
        }
        try {
            DispatcherLogic.saveConfiguration(pageInstance.getDirectory(), configuration);
        } catch (Exception e) {
            logger.error("Couldn't save configuration", e);
            SessionMessages.addErrorMessage("error saving conf");
        }
    }

    //--------------------------------------------------------------------------
    // Getters/Setters
    //--------------------------------------------------------------------------

    public Dispatch getDispatch() {
        return dispatch;
    }

    public String getReturnToParentTarget() {
        return returnToParentTarget;
    }

    public Map<String, String> getReturnToParentParams() {
        return returnToParentParams;
    }

    public MultiMap getPortlets() {
        return portlets;
    }

    public boolean isMultipartRequest() {
        return false;
    }

    public ResultSetNavigation getResultSetNavigation() {
        return resultSetNavigation;
    }

    public void setResultSetNavigation(ResultSetNavigation resultSetNavigation) {
        this.resultSetNavigation = resultSetNavigation;
    }

    public Form getPageConfigurationForm() {
        return pageConfigurationForm;
    }

    public void setPageConfigurationForm(Form pageConfigurationForm) {
        this.pageConfigurationForm = pageConfigurationForm;
    }

    protected void setupPortlets(PageInstance pageInstance, String myself) {
        Layout layout = pageInstance.getLayout();
        if(layout == null) {
            PortletInstance myPortletInstance = new PortletInstance("p", 0, myself);
            portlets.put(DEFAULT_LAYOUT_CONTAINER, myPortletInstance);
            return;
        }
        Self self = layout.getSelf();
        int myOrder = self.getActualOrder();
        PortletInstance myPortletInstance = new PortletInstance("p", myOrder, myself);
        String layoutContainer = self.getContainer();
        if (layoutContainer == null) {
            layoutContainer = DEFAULT_LAYOUT_CONTAINER;
        }
        portlets.put(layoutContainer, myPortletInstance);
        for(ChildPage page : layout.getChildPages()) {
            String layoutContainerInParent = page.getContainer();
            if(layoutContainerInParent != null) {
                String newPath = dispatch.getOriginalPath() + "/" + page.getName();
                PortletInstance portletInstance =
                        new PortletInstance(
                                "c" + page.getName(),
                                page.getActualOrder(),
                                newPath);

                portlets.put(layoutContainerInParent, portletInstance);
            }
        }
        for(Object entryObj : portlets.entrySet()) {
            Map.Entry entry = (Map.Entry) entryObj;
            List portletContainer = (List) entry.getValue();
            Collections.sort(portletContainer);
        }
    }

    protected Resolution forwardToPortletPage(String pageJsp) {
        setupPortlets(pageInstance, pageJsp);
        HttpServletRequest request = context.getRequest();
        request.setAttribute("cancelReturnUrl", getCancelReturnUrl());
        return new ForwardResolution(getLayoutJsp(pageInstance.getLayout()));
    }

    protected String getLayoutJsp(Layout layout) {
        if(layout == null) {
            return DEFAULT_LAYOUT;
        }
        String layoutName = layout.getLayout();
        if(StringUtils.isBlank(layoutName)) {
            return DEFAULT_LAYOUT;
        }
        return layoutName;
    }

    @Button(list = "configuration", key = "commons.cancel", order = 99)
    public Resolution cancel() {
        return new RedirectResolution(getCancelReturnUrl(), false);
    }

    public PageInstance getPageInstance() {
        return pageInstance;
    }

    public Page getPage() {
        return getPageInstance().getPage();
    }

    public void setPageInstance(PageInstance pageInstance) {
        this.pageInstance = pageInstance;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public String getCancelReturnUrl() {
        if (!StringUtils.isEmpty(cancelReturnUrl)) {
            return cancelReturnUrl;
        } else {
            String url = (String) context.getRequest().getAttribute("cancelReturnUrl");
            if(!StringUtils.isEmpty(url)) {
                return url;
            } else {
                return dispatch.getAbsoluteOriginalPath();
            }
        }
    }

    public void setCancelReturnUrl(String cancelReturnUrl) {
        this.cancelReturnUrl = cancelReturnUrl;
    }

    //--------------------------------------------------------------------------
    // Page configuration
    //--------------------------------------------------------------------------

    protected void prepareConfigurationForms() {
        Page page = pageInstance.getPage();

        PageInstance parent = pageInstance.getParent();
        assert parent != null;

        FormBuilder formBuilder = new FormBuilder(EditPage.class)
                .configPrefix(CONF_FORM_PREFIX)
                .configFields(supportsParameters() ? PAGE_CONFIGURATION_FIELDS : PAGE_CONFIGURATION_FIELDS_NO_DETAIL)
                .configFieldSetNames("Page");

        SelectionProvider layoutSelectionProvider = createLayoutSelectionProvider();
        formBuilder.configSelectionProvider(layoutSelectionProvider, "layout");
        SelectionProvider detailLayoutSelectionProvider = createLayoutSelectionProvider();
        formBuilder.configSelectionProvider(detailLayoutSelectionProvider, "detailLayout");

        pageConfigurationForm = formBuilder.build();
        EditPage edit = new EditPage();
        edit.id = page.getId();
        edit.description = page.getDescription();
        edit.subtreeRoot = page.isSubtreeRoot();
        edit.layout = getLayoutJsp(page.getLayout());
        edit.detailLayout = getLayoutJsp(page.getDetailLayout());
        pageConfigurationForm.readFromObject(edit);
        title = page.getTitle();

        if(script == null) {
            prepareScript();
        }
    }

    private SelectionProvider createLayoutSelectionProvider() {
        String warRealPath =
                portofinoConfiguration.getString(
                        PortofinoProperties.WAR_REAL_PATH);
        File webappFile = new File(warRealPath);

        File layoutsDir = new File(new File(warRealPath, "layouts"), "portlet");
        File[] files = layoutsDir.listFiles();
        DefaultSelectionProvider selectionProvider = new DefaultSelectionProvider("jsp");
        visitJspFiles(webappFile, files, selectionProvider);

        layoutsDir = new File(application.getAppDir(), "layouts");
        if(layoutsDir.isDirectory()) {
            files = layoutsDir.listFiles();
            visitJspFiles(webappFile, files, selectionProvider);
        }
        return selectionProvider;
    }

    private void visitJspFiles(File root, File[] files,
                               DefaultSelectionProvider selectionProvider) {
        for(File file : files) {
            if(file.isFile() && file.getName().endsWith(".jsp")) {
                String path = File.separator + com.manydesigns.portofino.util.FileUtils.getRelativePath(root, file);
                String name = file.getName();
                if(DEFAULT_LAYOUT.equals(path)) {
                    name += " (default)";
                }
                selectionProvider.appendRow(path, name, true);
            } else if(file.isDirectory()) {
                visitJspFiles(root, file.listFiles(), selectionProvider);
            }
        }
    }

    protected void readPageConfigurationFromRequest() {
        pageConfigurationForm.readFromRequest(context.getRequest());
        title = context.getRequest().getParameter("title");
    }

    protected boolean validatePageConfiguration() {
        boolean valid = true;
        title = StringUtils.trimToNull(title);
        if (title == null) {
            SessionMessages.addErrorMessage(getMessage("commons.configuration.titleEmpty"));
            valid = false;
        }

        valid = pageConfigurationForm.validate() && valid;

        return valid;
    }

    protected boolean updatePageConfiguration() {
        EditPage edit = new EditPage();
        pageConfigurationForm.writeToObject(edit);
        Page page = pageInstance.getPage();
        page.setTitle(title);
        page.setDescription(edit.description);
        page.getLayout().setLayout(edit.layout);
        page.getDetailLayout().setLayout(edit.detailLayout);
        try {
            DispatcherLogic.savePage(pageInstance.getDirectory(), page);
        } catch (Exception e) {
            logger.error("Couldn't save page", e);
            return false; //TODO handle return value + script + session msg
        }
        updateScript();
        return true;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    //**************************************************************************
    // Selection Providers
    //**************************************************************************

    protected DefaultSelectionProvider createSelectionProvider
            (String name, int fieldCount, Class[] fieldTypes, Collection<Object[]> objects) {
        DefaultSelectionProvider selectionProvider = new DefaultSelectionProvider(name, fieldCount);
        for (Object[] valueAndLabel : objects) {
            Object[] values = new Object[fieldCount];
            String[] labels = new String[fieldCount];

            for (int j = 0; j < fieldCount; j++) {
                Class valueType = fieldTypes[j];
                values[j] = OgnlUtils.convertValue(valueAndLabel[j * 2], valueType);
                labels[j] = OgnlUtils.convertValueToString(valueAndLabel[j*2+1]);
            }

            boolean active = true;
            if(valueAndLabel.length > 2 * fieldCount) {
                active =
                        valueAndLabel[2 * fieldCount] instanceof Boolean &&
                        (Boolean) valueAndLabel[2 * fieldCount];
            }

            selectionProvider.appendRow(values, labels, active);
        }
        return selectionProvider;
    }

    protected DefaultSelectionProvider createSelectionProvider(
            String name,
            Collection objects,
            PropertyAccessor[] propertyAccessors,
            TextFormat[] textFormats
    ) {
        int fieldsCount = propertyAccessors.length;
        DefaultSelectionProvider selectionProvider = new DefaultSelectionProvider(name, propertyAccessors.length);
        for (Object current : objects) {
            boolean active = true;
            if(current instanceof Object[]) {
                Object[] valueAndActive = (Object[]) current;
                if(valueAndActive.length > 1) {
                    active = valueAndActive[1] instanceof Boolean && (Boolean) valueAndActive[1];
                }
                if(valueAndActive.length > 0) {
                    current = valueAndActive[0];
                } else {
                    throw new IllegalArgumentException("Invalid selection provider query result - sp: " + name);
                }
            }
            Object[] values = new Object[fieldsCount];
            String[] labels = new String[fieldsCount];
            int j = 0;
            for (PropertyAccessor property : propertyAccessors) {
                Object value = property.get(current);
                values[j] = value;
                if (textFormats == null || textFormats[j] == null) {
                    String label = OgnlUtils.convertValueToString(value);
                    labels[j] = label;
                } else {
                    TextFormat textFormat = textFormats[j];
                    labels[j] = textFormat.format(current);
                }
                j++;
            }
            selectionProvider.appendRow(values, labels, active);
        }
        return selectionProvider;
    }

    protected DefaultSelectionProvider createSelectionProvider
            (String name, Collection objects, Class objectClass,
             TextFormat[] textFormats, String[] propertyNames) {
        ClassAccessor classAccessor =
                JavaClassAccessor.getClassAccessor(objectClass);
        PropertyAccessor[] propertyAccessors =
                new PropertyAccessor[propertyNames.length];
        for (int i = 0; i < propertyNames.length; i++) {
            String currentName = propertyNames[i];
            try {
                PropertyAccessor propertyAccessor =
                        classAccessor.getProperty(currentName);
                propertyAccessors[i] = propertyAccessor;
            } catch (Throwable e) {
                String msg = MessageFormat.format(
                        "Could not access property: {0}", currentName);
                logger.warn(msg, e);
                throw new IllegalArgumentException(msg, e);
            }
        }
        return createSelectionProvider(name, objects, propertyAccessors, textFormats);
    }

    //--------------------------------------------------------------------------
    // Scripting
    //--------------------------------------------------------------------------

    protected void prepareScript() {
        String pageId = pageInstance.getPage().getId();
        File file = ScriptingUtil.getGroovyScriptFile(pageInstance.getDirectory(), "action");
        if(file.exists()) {
            FileReader fr = null;
            try {
                fr = new FileReader(file);
                script = IOUtils.toString(fr);
            } catch (Exception e) {
                logger.warn("Couldn't load script for page " + pageId, e);
            } finally {
                IOUtils.closeQuietly(fr);
            }
        } else {
            String template = getScriptTemplate();
            String className = pageId;
            if(Character.isDigit(className.charAt(0))) {
                className = "_" + className;
            }
            if(template != null) {
                script = template.replace("__CLASS_NAME__", className);
            }
        }
    }

    public String getScriptTemplate() {
        throw new UnsupportedOperationException("Unknown script template for " + getClass());
    }

    protected void updateScript() {
        File directory = pageInstance.getDirectory();
        File groovyScriptFile = ScriptingUtil.getGroovyScriptFile(directory, "action");
        try {
            Class<?> scriptClass = DispatcherLogic.setActionClass(directory, script);
            if(scriptClass == null) {
                SessionMessages.addErrorMessage(getMessage("script.class.invalid"));
                return;
            }
            if(this instanceof GroovyObject) {
                //Attempt to remove old instance of custom action bean
                //not guaranteed to work
                try {
                    ModelActionResolver actionResolver =
                            (ModelActionResolver) StripesFilter.getConfiguration().getActionResolver();
                    actionResolver.removeActionBean(getClass());
                } catch (Exception e) {
                    logger.warn("Couldn't remove action bean " + this, e);
                }
            }
        } catch (IOException e) {
            logger.error("Error writing script to " + groovyScriptFile, e);
            String msg = MessageFormat.format(getMessage("script.write.failed"), groovyScriptFile.getAbsolutePath());
            SessionMessages.addErrorMessage(msg);
        } catch (Exception e) {
            String pageId = pageInstance.getPage().getId();
            logger.warn("Couldn't compile script for page " + pageId, e);
            SessionMessages.addErrorMessage(getMessage("script.compile.failed"));
        }
    }

    protected String getMessage(String key) {
        Locale locale = context.getLocale();
        ResourceBundle resourceBundle = application.getBundle(locale);
        return resourceBundle.getString(key);
    }

    public Map getOgnlContext() {
        return ElementsThreadLocals.getOgnlContext();
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

}