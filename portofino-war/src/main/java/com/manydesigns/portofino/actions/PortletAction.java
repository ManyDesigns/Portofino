package com.manydesigns.portofino.actions;

import com.manydesigns.elements.fields.SelectField;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.util.ReflectionUtil;
import com.manydesigns.portofino.ApplicationAttributes;
import com.manydesigns.portofino.actions.model.NewPage;
import com.manydesigns.portofino.context.Application;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.dispatcher.CrudPageInstance;
import com.manydesigns.portofino.dispatcher.Dispatch;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.pages.ChartPage;
import com.manydesigns.portofino.model.pages.CrudPage;
import com.manydesigns.portofino.model.pages.Page;
import com.manydesigns.portofino.navigation.ResultSetNavigation;
import com.manydesigns.portofino.util.ShortNameUtils;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.controller.StripesConstants;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

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

    public Resolution newPage() {
        prepareNewPageForm();
        return new ForwardResolution("/layouts/page-crud/new-page.jsp");
    }

    public Resolution createPage() {
        prepareNewPageForm();
        newPageForm.readFromRequest(context.getRequest());
        if(newPageForm.validate()) {
            NewPage newPage = new NewPage();
            newPageForm.writeToObject(newPage);
            newPage.init(model);
            String pageClassName = newPage.getPageClassName();
            Page page = (Page) ReflectionUtil.newInstance(pageClassName);
            try {
                BeanUtils.copyProperties(page, newPage);
            } catch (Exception e) {
                throw new RuntimeException(e); //TODO
            }
            System.out.println(page);
            return new ForwardResolution("/layouts/page-crud/new-page.jsp");
        } else {
            return new ForwardResolution("/layouts/page-crud/new-page.jsp");
        }
    }

    private void prepareNewPageForm() {
        SelectionProvider classSelectionProvider =
                DefaultSelectionProvider.create("pageClassName",
                        new String[] { CrudPage.class.getName(), ChartPage.class.getName() },
                        new String[] { "Crud", "Chart" });
        boolean includeSiblingOption = dispatch.getPageInstancePath().length > 1;
        int fieldCount = includeSiblingOption ? 3 : 2;
        NewPage.InsertPosition[] insertPositions = new NewPage.InsertPosition[fieldCount];
        String[] labels =  new String[fieldCount];
        insertPositions[0] =  NewPage.InsertPosition.TOP;
        labels[0] = "At the top level";
        insertPositions[1] = NewPage.InsertPosition.CHILD;
        labels[1] = "As a child of " + dispatch.getLastPageInstance().getPage().getTitle();
        if(includeSiblingOption) {
            insertPositions[2] = NewPage.InsertPosition.SIBLING;
            labels[2] = "As a sibling of " + dispatch.getLastPageInstance().getPage().getTitle();
        }
        SelectionProvider insertPositionSelectionProvider =
                DefaultSelectionProvider.create("insertPositionName", insertPositions, labels);
        newPageForm = new FormBuilder(NewPage.class)
                .configFields(NEW_PAGE_SETUP_FIELDS)
                .configFieldSetNames("Page setup", "Page position")
                .configSelectionProvider(classSelectionProvider, "pageClassName")
                .configSelectionProvider(insertPositionSelectionProvider, "insertPositionName")
                .build();
        //((SelectField) newPageForm.findFieldByPropertyName("insertPositionName")).setValue(NewPage.InsertPosition.CHILD);
    }

    public Form getNewPageForm() {
        return newPageForm;
    }

    //--------------------------------------------------------------------------
    // Page crud fields
    //--------------------------------------------------------------------------

    protected static final String[][] NEW_PAGE_SETUP_FIELDS = {
            {"pageClassName", "id", "title", "description"},
            {"insertPositionName"}};
    protected Form newPageForm;

}