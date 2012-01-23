package com.manydesigns.portofino.actions;

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
import com.manydesigns.portofino.actions.forms.EditPage;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.dispatcher.CrudPageInstance;
import com.manydesigns.portofino.dispatcher.Dispatch;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.pages.AccessLevel;
import com.manydesigns.portofino.model.pages.CrudPage;
import com.manydesigns.portofino.model.pages.Page;
import com.manydesigns.portofino.model.pages.RootPage;
import com.manydesigns.portofino.navigation.ResultSetNavigation;
import com.manydesigns.portofino.scripting.ScriptingUtil;
import com.manydesigns.portofino.stripes.ModelActionResolver;
import com.manydesigns.portofino.system.model.users.annotations.RequiresPermissions;
import com.manydesigns.portofino.util.ShortNameUtils;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import net.sourceforge.stripes.action.Before;
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
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

@RequiresPermissions(level = AccessLevel.VIEW)
public class PortletAction extends AbstractActionBean {
    public static final String DEFAULT_LAYOUT_CONTAINER = "default";
    public static final String[][] PAGE_CONFIGURATION_FIELDS =
            {{"id", "embedInParent", "showInNavigation", "subtreeRoot", "layout", "description"}};
    public static final String[][] TOP_LEVEL_PAGE_CONFIGURATION_FIELDS =
            {{"id", "showInNavigation", "subtreeRoot", "layout", "description"}};
    public static final String PAGE_PORTLET_NOT_CONFIGURED = "/layouts/portlet-not-configured.jsp";
    public static final String PORTOFINO_PORTLET_EXCEPTION = "portofino.portlet.exception";

    public static final String CONF_FORM_PREFIX = "config";
    public static final String DEFAULT_LAYOUT = "/layouts/portlet/portlet-page-1-2-1-symmetric.jsp";

    //--------------------------------------------------------------------------
    // Properties
    //--------------------------------------------------------------------------

    @Inject(RequestAttributes.DISPATCH)
    public Dispatch dispatch;

    @Inject(RequestAttributes.PAGE_INSTANCE)
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
            LoggerFactory.getLogger(PortletAction.class);

    public boolean isEmbedded() {
        return getContext().getRequest().getAttribute(
                StripesConstants.REQ_ATTR_INCLUDE_PATH) != null;
    }

    @Before
    protected void prepare() {
        dereferencePageInstance();
    }

    protected void dereferencePageInstance() {
        if(pageInstance != null) {
            pageInstance = pageInstance.dereference();
        }
    }

    public void setupReturnToParentTarget() {
        PageInstance[] pageInstancePath =
                dispatch.getPageInstancePath();
        boolean hasPrevious = !getPage().isSubtreeRoot() && pageInstancePath.length > 1;
        returnToParentTarget = null;
        if (hasPrevious) {
            int previousPos = pageInstancePath.length - 2;
            PageInstance previousPageInstance = pageInstancePath[previousPos];
            Page previousPage = previousPageInstance.getPage();
            if(!previousPage.isShowInNavigation()) {
                return;
            }
            if (previousPageInstance instanceof CrudPageInstance) {
                CrudPageInstance crudPageInstance =
                        (CrudPageInstance) previousPageInstance;
                if(crudPageInstance.getCrud() != null) {
                    if (CrudPage.MODE_SEARCH.equals(crudPageInstance.getMode())) {
                        returnToParentTarget = crudPageInstance.getCrud().getName();
                    } else if (CrudPage.MODE_DETAIL.equals(crudPageInstance.getMode())) {
                        Object previousPageObject = crudPageInstance.getObject();
                        ClassAccessor previousPageClassAccessor =
                                crudPageInstance.getClassAccessor();
                        returnToParentTarget = ShortNameUtils.getName(
                                previousPageClassAccessor, previousPageObject);
                    }
                }
            }
        }
    }

    //--------------------------------------------------------------------------
    // Admin methods
    //--------------------------------------------------------------------------

    protected void saveModel() {
        model.init();
        application.saveXmlModel();
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
        PortletInstance myPortletInstance = new PortletInstance("p", pageInstance.getLayoutOrder(), myself);
        String layoutContainer = pageInstance.getLayoutContainer();
        if (layoutContainer == null) {
            layoutContainer = DEFAULT_LAYOUT_CONTAINER;
        }
        portlets.put(layoutContainer, myPortletInstance);
        for(Page page : pageInstance.getChildPages()) {
            String layoutContainerInParent = page.getLayoutContainerInParent();
            if(layoutContainerInParent != null) {
                PortletInstance portletInstance =
                        new PortletInstance(
                                "c" + page.getFragment(),
                                page.getActualLayoutOrderInParent(),
                                dispatch.getOriginalPath() + "/" + page.getFragment());

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
        return new ForwardResolution(getLayout());
    }

    protected String getLayout() {
        String layout = pageInstance.getPage().getLayout();
        if(StringUtils.isBlank(layout)) {
            layout = DEFAULT_LAYOUT;
        }
        return layout;
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

        boolean isTopLevelPage = pageInstance.getPage().getParent() instanceof RootPage;
        FormBuilder formBuilder = new FormBuilder(EditPage.class)
                .configPrefix(CONF_FORM_PREFIX)
                .configFields(isTopLevelPage ? TOP_LEVEL_PAGE_CONFIGURATION_FIELDS : PAGE_CONFIGURATION_FIELDS)
                .configFieldSetNames("Page");

        SelectionProvider layoutSelectionProvider = createLayoutSelectionProvider();
        formBuilder.configSelectionProvider(layoutSelectionProvider, "layout");

        pageConfigurationForm = formBuilder.build();
        EditPage edit = new EditPage();
        edit.id = page.getId();
        edit.description = page.getDescription();
        edit.embedInParent = page.getLayoutContainerInParent() != null;
        edit.showInNavigation = page.isShowInNavigation();
        edit.subtreeRoot = page.isSubtreeRoot();
        edit.layout = getLayout();
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

    protected void updatePageConfiguration() {
        EditPage edit = new EditPage();
        pageConfigurationForm.writeToObject(edit);
        Page page = pageInstance.getPage();
        page.setTitle(title);
        page.setDescription(edit.description);
        if(edit.embedInParent) {
            if(page.getLayoutContainerInParent() == null) {
                page.setLayoutContainerInParent(DEFAULT_LAYOUT_CONTAINER);
            }
        } else {
            page.setLayoutContainerInParent(null);
            page.setLayoutOrderInParent(null);
        }
        page.setShowInNavigation(edit.showInNavigation);
        page.setSubtreeRoot(edit.subtreeRoot);
        if(!edit.embedInParent && !edit.showInNavigation) {
            SessionMessages.addWarningMessage(getMessage("page.warnNotShowInNavigationNotEmbedded"));
        }
        page.setLayout(edit.layout);

        updateScript();
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
        File file = ScriptingUtil.getGroovyScriptFile(application.getAppScriptsDir(), pageId);
        if(file.exists()) {
            try {
                FileReader fr = new FileReader(file);
                script = IOUtils.toString(fr);
                IOUtils.closeQuietly(fr);
            } catch (Exception e) {
                logger.warn("Couldn't load script for page " + pageId, e);
            }
        } else {
            String template = getScriptTemplate();
            String className = pageId;
            if(Character.isDigit(className.charAt(0))) {
                className = "_" + className;
            }
            if(template != null) {
                try {
                    script = template.replace("__CLASS_NAME__", className);
                } catch (Exception e) {
                    logger.warn("Invalid default script template: " + template, e);
                }
            }
        }
    }

    public String getScriptTemplate() {
        return null;
    }

    protected void updateScript() {
        File groovyScriptFile =
                ScriptingUtil.getGroovyScriptFile(application.getAppScriptsDir(), pageInstance.getPage().getId());
        if(!StringUtils.isBlank(script)) {
            FileWriter fw = null;
            try {
                fw = new FileWriter(groovyScriptFile);
                fw.write(script);
                try {
                    GroovyClassLoader loader = new GroovyClassLoader();
                    loader.parseClass(script, groovyScriptFile.getAbsolutePath());
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
                } catch (Exception e) {
                    String pageId = pageInstance.getPage().getId();
                    logger.warn("Couldn't compile script for page " + pageId, e);
                    String key = "script.compile.failed";
                    SessionMessages.addErrorMessage(getMessage(key));
                }
            } catch (IOException e) {
                logger.error("Error writing script to " + groovyScriptFile, e);
            } finally {
                IOUtils.closeQuietly(fw);
            }
        } else {
            groovyScriptFile.delete();
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