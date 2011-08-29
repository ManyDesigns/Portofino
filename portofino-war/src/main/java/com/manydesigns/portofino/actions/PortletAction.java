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
import com.manydesigns.portofino.context.Application;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.dispatcher.CrudPageInstance;
import com.manydesigns.portofino.dispatcher.Dispatch;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.logic.PageLogic;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.pages.*;
import com.manydesigns.portofino.navigation.ResultSetNavigation;
import com.manydesigns.portofino.system.model.users.Group;
import com.manydesigns.portofino.system.model.users.UserUtils;
import com.manydesigns.portofino.util.ShortNameUtils;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.controller.StripesConstants;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class PortletAction extends AbstractActionBean {
    public static final String DEFAULT_LAYOUT_CONTAINER = "default";
    public static final String[][] PAGE_CONFIGURATION_FIELDS =
            {{"description", "embedInParent"}};
    public static final String[][] TOP_LEVEL_PAGE_CONFIGURATION_FIELDS =
            {{"description"}};
    public static final String PAGE_PORTLET_NOT_CONFIGURED = "/layouts/portlet-not-configured.jsp";
    public static final String PORTOFINO_PORTLET_EXCEPTION = "portofino.portlet.exception";

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

    public Resolution cancelLayout() {
        return new RedirectResolution(dispatch.getOriginalPath());
    }

    public Resolution reloadModel() {
        application.reloadXmlModel();
        return new RedirectResolution(dispatch.getOriginalPath());
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

    List<Group> allowGroups;
    List<Group> denyGroups;
    List<Group> availableGroups;

    String allowGroupNames;
    String denyGroupNames;
    String availableGroupNames;

    List<Page> inheritedPages;

    public Resolution pagePermissions() {
        Page page = pageInstance.getPage();

        setupGroups(page);

        inheritedPages = new ArrayList<Page>();
        Page current = page.getParent();
        while (current != null) {
            inheritedPages.add(current);
            current = current.getParent();
        }
        Collections.reverse(inheritedPages);
        return new ForwardResolution("/layouts/page/permissions.jsp");
    }

    public void setupGroups(Page page) {
        List<Group> groups = new ArrayList<Group>();
        groups.add(Group.ANONYMOUS_GROUP);
        groups.add(Group.REGISTERED_GROUP);
        groups.addAll(application.getAllObjects(UserUtils.GROUPTABLE));
        allowGroups = new ArrayList<Group>();
        denyGroups = new ArrayList<Group>();
        availableGroups = new ArrayList<Group>();

        List<String> allow = page.getPermissions().getAllow();
        List<String> deny = page.getPermissions().getDeny();

        for (Group group : groups) {
            if (allow.contains(group.getName())) {
                allowGroups.add(group);
            } else if (deny.contains(group.getName())) {
                denyGroups.add(group);
            } else {
                availableGroups.add(group);
            }
        }
    }

    static final String[] emptyStringArray = new String[0];

    public Resolution updatePagePermissions() {
        Page page = pageInstance.getPage();
        synchronized (application) {
            updatePagePermissions(page);
            saveModel();
            SessionMessages.addInfoMessage("Page permissions saved successfully.");
        }

        return new RedirectResolution(dispatch.getOriginalPath());
    }

    public void updatePagePermissions(Page page) {
        String[] allowNameArray = StringUtils.split(allowGroupNames, ',');
        if (allowNameArray == null) {
            allowNameArray = emptyStringArray;
        }
        String[] denyNameArray = StringUtils.split(denyGroupNames, ',');
        if (denyNameArray == null) {
            denyNameArray = emptyStringArray;
        }

        // clean old lists
        List<String> allow = page.getPermissions().getAllow();
        List<String> deny = page.getPermissions().getDeny();
        allow.clear();
        deny.clear();

        List<Group> groups = new ArrayList<Group>();
        groups.add(Group.ANONYMOUS_GROUP);
        groups.add(Group.REGISTERED_GROUP);
        groups.addAll(application.getAllObjects(UserUtils.GROUPTABLE));

        for (Group group : groups) {
            String groupName = group.getName();
            String comparableName = "group_" + groupName;
            if (ArrayUtils.contains(allowNameArray, comparableName)) {
                allow.add(groupName);
            } else if (ArrayUtils.contains(denyNameArray, comparableName)) {
                deny.add(groupName);
            }
        }
    }

    public List<Group> getAllowGroups() {
        return allowGroups;
    }

    public List<Group> getDenyGroups() {
        return denyGroups;
    }

    public List<Group> getAvailableGroups() {
        return availableGroups;
    }

    public String getAllowGroupNames() {
        return allowGroupNames;
    }

    public void setAllowGroupNames(String allowGroupNames) {
        this.allowGroupNames = allowGroupNames;
    }

    public String getDenyGroupNames() {
        return denyGroupNames;
    }

    public void setDenyGroupNames(String denyGroupNames) {
        this.denyGroupNames = denyGroupNames;
    }

    public String getAvailableGroupNames() {
        return availableGroupNames;
    }

    public void setAvailableGroupNames(String availableGroupNames) {
        this.availableGroupNames = availableGroupNames;
    }

    public List<Page> getInheritedPages() {
        return inheritedPages;
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
        boolean isTopLevelPage = pageInstance.getPage().getParent() instanceof RootPage;
        pageConfigurationForm = new FormBuilder(EditPage.class)
                .configFields(isTopLevelPage ? TOP_LEVEL_PAGE_CONFIGURATION_FIELDS : PAGE_CONFIGURATION_FIELDS)
                .configFieldSetNames("Page")
                .build();
    }

    protected void setupPageConfiguration() {
        Page page = pageInstance.getPage();
        prepareConfigurationForms();
        EditPage edit = new EditPage();
        edit.setDescription(page.getDescription());
        edit.setEmbedInParent(page.getLayoutContainerInParent() != null);
        pageConfigurationForm.readFromObject(edit);
        title = page.getTitle();
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
        page.setDescription(edit.getDescription());
        if(edit.isEmbedInParent()) {
            if(page.getLayoutContainerInParent() == null) {
                page.setLayoutContainerInParent(DEFAULT_LAYOUT_CONTAINER);
            }
        } else {
            page.setLayoutContainerInParent(null);
            page.setLayoutOrderInParent(null);
        }
    }

    //--------------------------------------------------------------------------
    // Page crud
    //--------------------------------------------------------------------------

    public Resolution newPage() {
        prepareNewPageForm();
        return new ForwardResolution("/layouts/page-crud/new-page.jsp");
    }

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
            BeanUtils.copyProperties(page, newPage);
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

    public Resolution deletePage() {
        PageInstance pageInstance = dispatch.getParentPageInstance();
        Page page = dispatch.getLastPageInstance().getPage();
        synchronized (application) {
            if(page.getParent() == null) {
                SessionMessages.addErrorMessage("You can't delete the root page!");
            }  else if(PageLogic.isLandingPage(model.getRootPage(), page)) {
                SessionMessages.addErrorMessage("You can't delete the landing page!");
            } else {
                pageInstance.removeChild(page);
                saveModel();
                return new RedirectResolution(dispatch.getParentPathUrl());
            }
        }
        return new RedirectResolution(dispatch.getOriginalPath());
    }

    public Resolution movePage() {
        if(movePageDestination == null) {
            SessionMessages.addErrorMessage("You must select a destination");
            return new RedirectResolution(dispatch.getOriginalPath());
        }
        Page page = dispatch.getLastPageInstance().getPage();
        synchronized (application) {
            if(page.getParent() == null) {
                SessionMessages.addErrorMessage("You can't move the root page!");
            } else {
                boolean detail = movePageDestination.endsWith("-detail");
                if(detail) {
                    movePageDestination = movePageDestination.substring(0, movePageDestination.length() - 7);
                }
                Page newParent = model.getRootPage().findDescendantPageById(movePageDestination);
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
                    SessionMessages.addErrorMessage("Invalid destination: " + movePageDestination);
                }
            }
        }
        return new RedirectResolution(dispatch.getOriginalPath());
    }

    private void prepareNewPageForm() {
        SelectionProvider classSelectionProvider =
                DefaultSelectionProvider.create("pageClassName",
                        new String[] {
                                CrudPage.class.getName(), ChartPage.class.getName(),
                                TextPage.class.getName(), JspPage.class.getName() },
                        new String[] { "Crud", "Chart", "Text", "JSP" });
        boolean includeSiblingOption = dispatch.getPageInstancePath().length > 1;
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

    public String getMovePageDestination() {
        return movePageDestination;
    }

    public void setMovePageDestination(String movePageDestination) {
        this.movePageDestination = movePageDestination;
    }

    //--------------------------------------------------------------------------
    // Page crud fields
    //--------------------------------------------------------------------------

    protected static final String[][] NEW_PAGE_SETUP_FIELDS = {
            {"pageClassName", "fragment", "title", "description", "insertPositionName"}};
    protected Form newPageForm;
    protected String movePageDestination;

}