package com.manydesigns.portofino.actions;

import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.portofino.ApplicationAttributes;
import com.manydesigns.portofino.context.Application;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.dispatcher.CrudPageInstance;
import com.manydesigns.portofino.dispatcher.Dispatch;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.pages.CrudPage;
import com.manydesigns.portofino.model.pages.Page;
import com.manydesigns.portofino.navigation.ResultSetNavigation;
import com.manydesigns.portofino.system.model.users.Group;
import com.manydesigns.portofino.system.model.users.UserUtils;
import com.manydesigns.portofino.util.ShortNameUtils;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.controller.StripesConstants;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

public class PortletAction extends AbstractActionBean {

    //--------------------------------------------------------------------------
    // Properties
    //--------------------------------------------------------------------------

    @Inject(RequestAttributes.DISPATCH)
    public Dispatch dispatch;

    @Inject(RequestAttributes.PAGE_INSTANCE)
    public PageInstance pageInstance;

    @Inject(ApplicationAttributes.APPLICATION)
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
        application.getModel().init();
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
            } else {
                String pageId = current.substring(1); //current = c...
                PageInstance childPageInstance = myself.findChildPage(pageId);
                Page childPage = childPageInstance.getPage();
                childPage.setLayoutContainerInParent(layoutContainer);
                childPage.setLayoutOrderInParent(i + "");
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

    public Resolution pagePermissions() {
        List<Group> groups = new ArrayList<Group>();
        groups.add(Group.ANONYMOUS_GROUP);
        groups.add(Group.REGISTERED_GROUP);
        groups.addAll(application.getAllObjects(UserUtils.GROUPTABLE));
        allowGroups = new ArrayList<Group>();
        denyGroups = new ArrayList<Group>();
        availableGroups = new ArrayList<Group>();

        List<String> allow = pageInstance.getPage().getPermissions().getAllow();
        List<String> deny = pageInstance.getPage().getPermissions().getDeny();

        for (Group group : groups) {
            if (allow.contains(group.getName())) {
                allowGroups.add(group);
            } else if (deny.contains(group.getName())) {
                denyGroups.add(group);
            } else {
                availableGroups.add(group);
            }
        }
        return new ForwardResolution("/layouts/page/permissions.jsp");
    }

    static final String[] emptyStringArray = new String[0];

    public Resolution updatePagePermissions() {
        synchronized (application) {
            String[] allowNameArray = StringUtils.split(allowGroupNames, ',');
            if (allowNameArray == null) {
                allowNameArray = emptyStringArray;
            }
            String[] denyNameArray = StringUtils.split(denyGroupNames, ',');
            if (denyNameArray == null) {
                denyNameArray = emptyStringArray;
            }

            // clean old lists
            List<String> allow = pageInstance.getPage().getPermissions().getAllow();
            List<String> deny = pageInstance.getPage().getPermissions().getDeny();
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
            saveModel();
            SessionMessages.addInfoMessage("Page permissions saved successfully.");
        }

        return new RedirectResolution(dispatch.getOriginalPath());
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

    protected void setupPortlets(PageInstance pageInstance, String myself) {
        PortletInstance myPortletInstance = new PortletInstance("p", pageInstance.getLayoutOrder(), myself);
        portlets.put(pageInstance.getLayoutContainer(), myPortletInstance);
        for(Page page : pageInstance.getChildPages()) {
            if(page.getLayoutContainerInParent() != null) {
                PortletInstance portletInstance =
                        new PortletInstance(
                                "c" + page.getId(),
                                page.getActualLayoutOrderInParent(),
                                dispatch.getOriginalPath() + "/" + page.getId());
                portlets.put(page.getLayoutContainerInParent(), portletInstance);
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
    // Page crud
    //--------------------------------------------------------------------------


}