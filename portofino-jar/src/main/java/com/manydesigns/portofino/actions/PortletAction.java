package com.manydesigns.portofino.actions;

import com.manydesigns.elements.fields.SelectField;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.util.RandomUtil;
import com.manydesigns.elements.util.ReflectionUtil;
import com.manydesigns.portofino.ApplicationAttributes;
import com.manydesigns.portofino.actions.forms.EditPage;
import com.manydesigns.portofino.actions.forms.NewPage;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.buttons.annotations.Buttons;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.dispatcher.CrudPageInstance;
import com.manydesigns.portofino.dispatcher.Dispatch;
import com.manydesigns.portofino.dispatcher.ModelActionResolver;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.logic.PageLogic;
import com.manydesigns.portofino.logic.SecurityLogic;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.ModelObject;
import com.manydesigns.portofino.model.ModelVisitor;
import com.manydesigns.portofino.model.pages.*;
import com.manydesigns.portofino.navigation.ResultSetNavigation;
import com.manydesigns.portofino.scripting.ScriptingUtil;
import com.manydesigns.portofino.system.model.users.annotations.RequiresAdministrator;
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
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.converters.ClassConverter;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@RequiresPermissions(level = AccessLevel.VIEW)
public class PortletAction extends AbstractActionBean {
    public static final String DEFAULT_LAYOUT_CONTAINER = "default";
    public static final String[][] PAGE_CONFIGURATION_FIELDS =
            {{"id", "description", "embedInParent", "showInNavigation"}};
    public static final String[][] TOP_LEVEL_PAGE_CONFIGURATION_FIELDS =
            {{"id", "description", "showInNavigation"}};
    public static final String PAGE_PORTLET_NOT_CONFIGURED = "/layouts/portlet-not-configured.jsp";
    public static final String PORTOFINO_PORTLET_EXCEPTION = "portofino.portlet.exception";

    public static final String CONF_FORM_PREFIX = "config";

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

    //--------------------------------------------------------------------------
    // Navigation
    //--------------------------------------------------------------------------

    protected ResultSetNavigation resultSetNavigation;
    public String cancelReturnUrl;

    //-----------------------1---------------------------------------------------
    // Page crud fields
    //--------------------------------------------------------------------------

    protected static final String[][] NEW_PAGE_SETUP_FIELDS = {
            {"pageClassName", "fragment", "title", "description", "insertPositionName"}};
    protected Form newPageForm;
    protected String destinationPageId;
    protected String fragment;

    //**************************************************************************
    // Scripting
    //**************************************************************************

    protected String script;
    protected File storageDirFile;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(PortletAction.class);
    public String title;
    public Form pageConfigurationForm;

    public boolean isEmbedded() {
        return getContext().getRequest().getAttribute(
                StripesConstants.REQ_ATTR_INCLUDE_PATH) != null;
    }

    @Before
    protected void prepare() {
        storageDirFile = application.getAppStorageDir();
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
        PageInstance thisPageInstance = dispatch.getLastPageInstance();
        boolean hasPrevious = pageInstancePath.length > 1;
        returnToParentTarget = null;
        if (hasPrevious) {
            int previousPos = pageInstancePath.length - 2;
            PageInstance previousPageInstance = pageInstancePath[previousPos];
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
        } else {
            if (thisPageInstance instanceof CrudPageInstance) {
                CrudPageInstance crudPageInstance =
                        (CrudPageInstance) thisPageInstance;
                if (CrudPage.MODE_DETAIL.equals(crudPageInstance.getMode())) {
                    returnToParentTarget = "search";
                }
            }
        }
    }

    //--------------------------------------------------------------------------
    // Admin methods
    //--------------------------------------------------------------------------

    @RequiresAdministrator
    public Resolution updateLayout() {
        synchronized (application) {
            HttpServletRequest request = context.getRequest();
            Enumeration parameters = request.getParameterNames();
            while(parameters.hasMoreElements()) {
                String parameter = (String) parameters.nextElement();
                if(parameter.startsWith("portletWrapper_")) {
                    String layoutContainer = parameter.substring("portletWrapper_".length());
                    String[] portletWrapperIds = request.getParameterValues(parameter);
                    updateLayout(layoutContainer, portletWrapperIds);
                }
            }
            saveModel();
        }
        return new RedirectResolution(dispatch.getOriginalPath());
    }

    protected void saveModel() {
        model.init();
        application.saveXmlModel();
    }

    @RequiresAdministrator
    public Resolution reloadModel() {
        synchronized (application) {
            application.loadXmlModel();
            return new RedirectResolution(dispatch.getOriginalPath());
        }
    }

    protected void updateLayout(String layoutContainer, String[] portletWrapperIds) {
        PageInstance myself = dispatch.getLastPageInstance();
        for(int i = 0; i < portletWrapperIds.length; i++) {
            String current = portletWrapperIds[i];
            if("p".equals(current)) {
                myself.setLayoutContainer(layoutContainer);
                myself.setLayoutOrder(i);
            } else if (current.startsWith("c")) {
                String pageId = current.substring(1); //current = c...
                PageInstance childPageInstance = myself.findChildPageByFragment(pageId);
                Page childPage = childPageInstance.getPage();
                childPage.setLayoutContainerInParent(layoutContainer);
                childPage.setLayoutOrderInParent(i + "");
            } else {
                logger.debug("Ignoring: {}", current);
            }
        }
    }

    //--------------------------------------------------------------------------
    // Page permisssions
    //--------------------------------------------------------------------------

    List<com.manydesigns.portofino.system.model.users.Group> groups;

    //<group, level>
    Map<String, String> accessLevels = new HashMap<String, String>();
    //<group, permissions>
    Map<String, List<String>> permissions = new HashMap<String, List<String>>();

    @RequiresAdministrator
    public Resolution pagePermissions() {
        Page page = pageInstance.getPage();

        setupGroups(page);

        return forwardToPagePermissions();
    }

    protected Resolution forwardToPagePermissions() {
        return new ForwardResolution("/layouts/page/permissions.jsp");
    }

    public void setupGroups(Page page) {
        Session session = application.getSession("portofino");
        Criteria criteria = session.createCriteria(SecurityLogic.GROUP_ENTITY_NAME).addOrder(Order.asc("name"));
        groups = new ArrayList(criteria.list());
    }

    @RequiresAdministrator
    @Button(list = "page-permissions-edit", key = "commons.update", order = 1)
    public Resolution updatePagePermissions() {
        Page page = pageInstance.getPage();
        synchronized (application) {
            updatePagePermissions(page);
            saveModel();
            SessionMessages.addInfoMessage("Page permissions saved successfully.");
        }

        return new RedirectResolution(dispatch.getOriginalPath())
                .addParameter("pagePermissions");
    }

    public void updatePagePermissions(Page page) {
        Permissions pagePermissions = page.getPermissions();

        pagePermissions.getGroups().clear();

        Map<String, Group> groups = new HashMap<String, Group>();

        for(Map.Entry<String, String> entry : accessLevels.entrySet()) {
            Group group = new Group();
            group.setName(entry.getKey());
            group.setAccessLevel(entry.getValue());
            groups.put(group.getName(), group);
        }

        for(Map.Entry<String, List<String>> custPerm : permissions.entrySet()) {
            String groupId = custPerm.getKey();
            Group group = groups.get(groupId);
            if(group == null) {
                group = new Group();
                group.setName(groupId);
                groups.put(groupId, group);
            }
            group.getPermissions().addAll(custPerm.getValue());
        }

        for(Group group : groups.values()) {
            pagePermissions.getGroups().add(group);
        }
    }

    public AccessLevel getLocalAccessLevel(Page currentPage, String groupId) {
        Permissions currentPagePermissions = currentPage.getPermissions();
        for(Group group : currentPagePermissions.getGroups()) {
            if(groupId.equals(group.getName())) {
                return group.getActualAccessLevel();
            }
        }
        return null;
    }

    //--------------------------------------------------------------------------
    // Getters/Setters
    //--------------------------------------------------------------------------

    public List<com.manydesigns.portofino.system.model.users.Group> getGroups() {
        return groups;
    }

    public Map<String, String> getAccessLevels() {
        return accessLevels;
    }

    public void setAccessLevels(Map<String, String> accessLevels) {
        this.accessLevels = accessLevels;
    }

    public Map<String, List<String>> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<String, List<String>> permissions) {
        this.permissions = permissions;
    }

    public Dispatch getDispatch() {
        return dispatch;
    }

    public String getReturnToParentTarget() {
        return returnToParentTarget;
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
        request.setAttribute("cancelReturnUrl", cancelReturnUrl);
        return new ForwardResolution("/layouts/portlet-page.jsp");
    }

    @Buttons({
        @Button(list = "page-permissions-edit", key = "commons.cancel", order = 99),
        @Button(list = "configuration", key = "commons.cancel", order = 99)
    })
    public Resolution cancel() {
        if (StringUtils.isEmpty(cancelReturnUrl)) {
            String url = dispatch.getOriginalPath();
            return new RedirectResolution(url);
        } else {
            return new RedirectResolution(cancelReturnUrl, false);
        }
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
        if (cancelReturnUrl == null) {
            return (String) context.getRequest().getAttribute("cancelReturnUrl");
        } else {
            return cancelReturnUrl;
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
        pageConfigurationForm = new FormBuilder(EditPage.class)
                .configPrefix(CONF_FORM_PREFIX)
                .configFields(isTopLevelPage ? TOP_LEVEL_PAGE_CONFIGURATION_FIELDS : PAGE_CONFIGURATION_FIELDS)
                .configFieldSetNames("Page")
                .build();

        EditPage edit = new EditPage();
        edit.id = page.getId();
        edit.description = page.getDescription();
        edit.embedInParent = page.getLayoutContainerInParent() != null;
        edit.showInNavigation = page.isShowInNavigation();
        pageConfigurationForm.readFromObject(edit);
        title = page.getTitle();

        if(script == null) {
            prepareScript();
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
            SessionMessages.addErrorMessage("Title cannot be empty");
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
        if(!edit.embedInParent && !edit.showInNavigation) {
            SessionMessages.addWarningMessage(
                    "The page is not embedded and not included in navigation - it will only be reachable by URL or explicit linking.");
        }

        updateScript();
    }

    //--------------------------------------------------------------------------
    // Page crud
    //--------------------------------------------------------------------------

    @RequiresAdministrator
    public Resolution newPage() {
        prepareNewPageForm();
        return new ForwardResolution("/layouts/page-crud/new-page.jsp");
    }

    @RequiresAdministrator
    public Resolution createPage() {
        try {
            return doCreateNewPage();
        } catch (Exception e) {
            logger.error("Error creating page", e);
            SessionMessages.addErrorMessage("Error creating page: " + e.getMessage() + " (see the logs for more details)");
            return new ForwardResolution("/layouts/page-crud/new-page.jsp");
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    protected Resolution portletError(Throwable e) {
        context.getRequest().setAttribute(PORTOFINO_PORTLET_EXCEPTION, e);
        if(isEmbedded()) {
            return new ForwardResolution("/layouts/portlet-error.jsp");
        } else {
            return forwardToPortletPage("/layouts/portlet-error.jsp");
        }
    }

    public static enum InsertPosition {
        TOP, CHILD, SIBLING
    }

    private Resolution doCreateNewPage() throws IllegalAccessException, InvocationTargetException {
        prepareNewPageForm();
        newPageForm.readFromRequest(context.getRequest());
        if(newPageForm.validate()) {
            NewPage newPage = new NewPage();
            newPageForm.writeToObject(newPage);
            InsertPosition insertPosition =
                    InsertPosition.valueOf(newPage.getInsertPositionName());
            String pageClassName = newPage.getPageClassName();
            Page page = (Page) ReflectionUtil.newInstance(pageClassName);
            copyModelObject(newPage, page);
            String pageId = RandomUtil.createRandomId();
            page.setId(pageId);
            page.setLayoutContainer(DEFAULT_LAYOUT_CONTAINER);
            page.setLayoutOrder("0");
            String configurePath;
            synchronized (application) {
                switch (insertPosition) {
                    case TOP:
                        model.getRootPage().addChild(page);
                        configurePath = "";
                        break;
                    case CHILD:
                        dispatch.getLastPageInstance().addChild(page);
                        configurePath = dispatch.getOriginalPath();
                        break;
                    case SIBLING:
                        dispatch.getPageInstance(-2).addChild(page);
                        configurePath = dispatch.getParentPathUrl();
                        break;
                    default:
                        throw new IllegalStateException("Don't know how to add page " + page + " at position " + insertPosition);
                }
                saveModel();
            }
            SessionMessages.addInfoMessage("Page created successfully. You should now configure it.");
            return new RedirectResolution(configurePath + "/" + page.getFragment()).addParameter("configure");
        } else {
            return new ForwardResolution("/layouts/page-crud/new-page.jsp");
        }
    }

    protected void copyModelObject(ModelObject src, ModelObject dest) throws IllegalAccessException, InvocationTargetException {
        //To handle actualActionClass = null, ClassConverter must have a null return value
        BeanUtilsBean.getInstance().getConvertUtils().register(new ClassConverter(null), Class.class);
        BeanUtils.copyProperties(dest, src);
    }

    @RequiresAdministrator
    public Resolution deletePage() {
        PageInstance parentPageInstance = dispatch.getParentPageInstance();
        Page page = dispatch.getLastPageInstance().getPage();
        synchronized (application) {
            if(page.getParent() == null) {
                SessionMessages.addErrorMessage("You can't delete the root page!");
            }  else if(PageLogic.isLandingPage(model.getRootPage(), page)) {
                SessionMessages.addErrorMessage("You can't delete the landing page!");
            } else {
                parentPageInstance.removeChild(page);
                saveModel();
                return new RedirectResolution(dispatch.getParentPathUrl());
            }
        }
        return new RedirectResolution(dispatch.getOriginalPath());
    }

    @RequiresAdministrator
    public Resolution movePage() {
        if(destinationPageId == null) {
            SessionMessages.addErrorMessage("You must select a destination");
            return new RedirectResolution(dispatch.getOriginalPath());
        }
        Page page = dispatch.getLastPageInstance().getPage();
        synchronized (application) {
            if(page.getParent() == null) {
                SessionMessages.addErrorMessage("You can't move the root page!");
            } else {
                boolean detail = destinationPageId.endsWith("-detail");
                if(detail) {
                    destinationPageId = destinationPageId.substring(0, destinationPageId.length() - 7);
                }
                Page newParent = model.getRootPage().findDescendantPageById(destinationPageId);
                if(newParent != null) {
                    page.getParent().removeChild(page);
                    if(detail) {
                        ((CrudPage) newParent).addDetailChild(page);
                    } else {
                        newParent.addChild(page);
                    }
                    saveModel();
                    return new RedirectResolution(""); //PageLogic.getPagePath(page)); TODO
                } else {
                    SessionMessages.addErrorMessage("Invalid destination: " + destinationPageId);
                }
            }
        }
        return new RedirectResolution(dispatch.getOriginalPath());
    }

    @RequiresAdministrator
    public Resolution copyPage() {
        if(destinationPageId == null) {
            SessionMessages.addErrorMessage("You must select a destination");
            return new RedirectResolution(dispatch.getOriginalPath());
        }
        if(fragment == null) {
            SessionMessages.addErrorMessage("You must select a fragment");
            return new RedirectResolution(dispatch.getOriginalPath());
        }
        Page page = dispatch.getLastPageInstance().getPage();
        synchronized (application) {
            if(page.getParent() == null) {
                SessionMessages.addErrorMessage("You can't copy the root page!");
            } else {
                boolean detail = destinationPageId.endsWith("-detail");
                if(detail) {
                    int length = "-detail".length();
                    destinationPageId = destinationPageId.substring(0, destinationPageId.length() - length);
                }
                final Page newParent = model.getRootPage().findDescendantPageById(destinationPageId);
                if(newParent != null) {
                    Page newPage;
                    /*try {
                        newPage = page.getClass().newInstance();
                        copyModelObject(page, newPage);
                        //String pageId = RandomUtil.createRandomId();
                        //newPage.setId(pageId);
                        newPage.setLayoutContainer(DEFAULT_LAYOUT_CONTAINER);
                        newPage.setLayoutOrder("0");
                    } catch (Exception e) {
                        SessionMessages.addErrorMessage("Error copying page");
                        logger.error("Error copying page", e);
                        return new RedirectResolution(dispatch.getOriginalPath());
                    }*/
                    Model tmpModel = new Model();
                    tmpModel.setRootPage(new RootPage());
                    tmpModel.getRootPage().getChildPages().add(page);
                    try {
                        JAXBContext jc = JAXBContext.newInstance(Model.JAXB_MODEL_PACKAGES);
                        Marshaller marshaller = jc.createMarshaller();
                        StringWriter sw = new StringWriter();
                        marshaller.marshal(tmpModel, sw);
                        Unmarshaller unmarshaller = jc.createUnmarshaller();
                        tmpModel = (Model) unmarshaller.unmarshal(new StringReader(sw.toString()));
                    } catch (JAXBException e) {
                        SessionMessages.addErrorMessage("Error copying page");
                        logger.error("Error copying page", e);
                        return new RedirectResolution(dispatch.getOriginalPath());
                    }
                    newPage = tmpModel.getRootPage().getChildPages().get(0);
                    newPage.setFragment(fragment);
                    new CopyVisitor().visit(newPage);
                    if(detail) {
                        ((CrudPage) newParent).addDetailChild(newPage);
                    } else {
                        newParent.addChild(newPage);
                    }
                    saveModel();
                    return new RedirectResolution(""); //PageLogic.getPagePath(page)); TODO
                } else {
                    SessionMessages.addErrorMessage("Invalid destination: " + destinationPageId);
                }
            }
        }
        return new RedirectResolution(dispatch.getOriginalPath());
    }

    protected class CopyVisitor extends ModelVisitor {

        @Override
        public void visitNodeBeforeChildren(ModelObject node) {
            if(node instanceof Page) {
                Page page = (Page) node;
                final String oldPageId = page.getId();
                final String pageId = RandomUtil.createRandomId();
                page.setId(pageId);
                File storageDirFile = application.getAppStorageDir();
                File[] resources = storageDirFile.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.startsWith(oldPageId + ".");
                    }
                });
                for(File res : resources) {
                    File dest = new File(storageDirFile, pageId + res.getName().substring(oldPageId.length()));
                    FileInputStream fis = null;
                    FileOutputStream fos = null;
                    try {
                        fis = new FileInputStream(res);
                        fos = new FileOutputStream(dest);
                        IOUtils.copy(fis, fos);
                    } catch (IOException e) {
                        logger.error("Couldn't copy resource file " + res + " to " + dest, e);
                    } finally {
                        IOUtils.closeQuietly(fis);
                        IOUtils.closeQuietly(fos);
                    }
                }
            }
        }

    }

/*
    //TODO copiare risorse (file)
    protected class CopyVisitor extends ModelVisitor {

        protected Deque<ModelObject> parents = new LinkedList<ModelObject>();

        public CopyVisitor(ModelObject root) {
            parents.push(root);
        }

        @Override
        public void visitNodeBeforeChildren(ModelObject node) {
            ModelObject newNode = (ModelObject) ReflectionUtil.newInstance(node.getClass());
            try {
                copyModelObject(node, newNode);
                newNode.afterUnmarshal(null, parents.element());
                parents.push(newNode);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void visitNodeAfterChildren(ModelObject node) {
            parents.pop();
        }
    }
    */

    private void prepareNewPageForm() {
        SelectionProvider classSelectionProvider =
                DefaultSelectionProvider.create("pageClassName",
                        new String[] {
                                CrudPage.class.getName(), ChartPage.class.getName(),
                                TextPage.class.getName(), JspPage.class.getName(),
                                /*PageReference.class.getName()*/ },
                        new String[] { "Crud", "Chart", "Text", "JSP", /*"Reference to another page"*/ });
        //root + at least 1 child
        boolean includeSiblingOption = dispatch.getPageInstancePath().length > 2;
        int fieldCount = includeSiblingOption ? 3 : 2;
        String[] insertPositions = new String[fieldCount];
        String[] labels =  new String[fieldCount];
        insertPositions[0] =  InsertPosition.TOP.name();
        labels[0] = "at the top level";
        insertPositions[1] = InsertPosition.CHILD.name();
        labels[1] = "as a child of " + dispatch.getLastPageInstance().getPage().getTitle();
        if(includeSiblingOption) {
            insertPositions[2] = InsertPosition.SIBLING.name();
            labels[2] = "as a sibling of " + dispatch.getLastPageInstance().getPage().getTitle();
        }
        SelectionProvider insertPositionSelectionProvider =
                DefaultSelectionProvider.create("insertPositionName", insertPositions, labels);
        newPageForm = new FormBuilder(NewPage.class)
                .configFields(NEW_PAGE_SETUP_FIELDS)
                .configFieldSetNames("Page setup")
                .configSelectionProvider(classSelectionProvider, "pageClassName")
                .configSelectionProvider(insertPositionSelectionProvider, "insertPositionName")
                .build();
        ((SelectField) newPageForm.findFieldByPropertyName("insertPositionName")).setValue(InsertPosition.CHILD.name());
    }

    public Form getNewPageForm() {
        return newPageForm;
    }

    public String getDestinationPageId() {
        return destinationPageId;
    }

    public void setDestinationPageId(String destinationPageId) {
        this.destinationPageId = destinationPageId;
    }

    public String getFragment() {
        return fragment;
    }

    public void setFragment(String fragment) {
        this.fragment = fragment;
    }

    //--------------------------------------------------------------------------
    // Scripting
    //--------------------------------------------------------------------------

    protected void prepareScript() {
        String pageId = pageInstance.getPage().getId();
        File file = ScriptingUtil.getGroovyScriptFile(storageDirFile, pageId);
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
                ScriptingUtil.getGroovyScriptFile(storageDirFile, pageInstance.getPage().getId());
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
                    String msg = "Couldn't compile script - see logs for details";
                    SessionMessages.addErrorMessage(msg);
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

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

}