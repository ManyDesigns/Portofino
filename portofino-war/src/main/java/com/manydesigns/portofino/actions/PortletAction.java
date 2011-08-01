package com.manydesigns.portofino.actions;

import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.portofino.annotations.InjectDispatch;
import com.manydesigns.portofino.dispatcher.CrudNodeInstance;
import com.manydesigns.portofino.dispatcher.Dispatch;
import com.manydesigns.portofino.dispatcher.SiteNodeInstance;
import com.manydesigns.portofino.model.site.CrudNode;
import com.manydesigns.portofino.model.site.EmbeddableNode;
import com.manydesigns.portofino.model.site.SiteNode;
import com.manydesigns.portofino.navigation.ResultSetNavigation;
import com.manydesigns.portofino.util.ShortNameUtils;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.controller.StripesConstants;

import java.util.ArrayList;
import java.util.List;

public class PortletAction extends AbstractActionBean {
    public final List<String> portlets = new ArrayList<String>();
    @InjectDispatch
    public Dispatch dispatch;
    public String returnToParentTarget;

    //--------------------------------------------------------------------------
    // Navigation
    //--------------------------------------------------------------------------

    protected ResultSetNavigation resultSetNavigation;

    public boolean isEmbedded() {
        return getContext().getRequest().getAttribute(
                StripesConstants.REQ_ATTR_INCLUDE_PATH) != null;
    }

    public void setupReturnToParentTarget() {
        SiteNodeInstance[] siteNodeInstancePath =
                dispatch.getSiteNodeInstancePath();
        SiteNodeInstance thisNodeInstance = dispatch.getLastSiteNodeInstance();
        boolean hasPrevious = siteNodeInstancePath.length > 1;
        returnToParentTarget = null;
        if (hasPrevious) {
            int previousPos = siteNodeInstancePath.length - 2;
            SiteNodeInstance previousNode = siteNodeInstancePath[previousPos];
            if (previousNode instanceof CrudNodeInstance) {
                CrudNodeInstance crudNodeInstance =
                        (CrudNodeInstance) previousNode;
                if (CrudNode.MODE_SEARCH.equals(crudNodeInstance.getMode())) {
                    returnToParentTarget = crudNodeInstance.getCrud().getName();
                } else if (CrudNode.MODE_DETAIL.equals(crudNodeInstance.getMode())) {
                    Object previousNodeObject = crudNodeInstance.getObject();
                    ClassAccessor previousNodeClassAccessor =
                            crudNodeInstance.getClassAccessor();
                    returnToParentTarget = ShortNameUtils.getName(
                            previousNodeClassAccessor, previousNodeObject);
                }
            }
        } else {
            if (thisNodeInstance instanceof CrudNodeInstance) {
                CrudNodeInstance crudNodeInstance =
                        (CrudNodeInstance) thisNodeInstance;
                if (CrudNode.MODE_DETAIL.equals(crudNodeInstance.getMode())) {
                    returnToParentTarget = "search";
                }
            }
        }
    }

    public Dispatch getDispatch() {
        return dispatch;
    }

    public String getReturnToParentTarget() {
        return returnToParentTarget;
    }

    public List<String> getPortlets() {
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

    protected void setupPortlets(String myself, SiteNodeInstance siteNodeInstance) {
        portlets.add(myself);
        for(SiteNode node : siteNodeInstance.getChildNodes()) {
            if(node instanceof EmbeddableNode) {
                portlets.add(dispatch.getOriginalPath() + "/" + node.getId());
            }
        }
    }

    protected Resolution forwardToPortletPage(String nodeJsp, SiteNodeInstance siteNodeInstance) {
        setupPortlets(nodeJsp, siteNodeInstance);
        return new ForwardResolution("/layouts/portlet-page.jsp");
    }
}