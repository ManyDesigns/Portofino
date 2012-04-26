<%@ page import="com.manydesigns.portofino.logic.SecurityLogic"
%><%@ page import="java.util.Map"
%><%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><stripes:layout-definition>
    <jsp:useBean id="actionBean" scope="request"
             type="com.manydesigns.portofino.pageactions.AbstractPageAction"/>
    <div class="contentHeader">
        <stripes:layout-component name="contentHeader">
            <div class="yui-g">
                <div class="contentBarLeft">
                    <stripes:form action="${actionBean.dispatch.originalPath}" method="post" enctype="multipart/form-data">
                        <c:if test="${not empty actionBean.returnToParentTarget}">
                            <button type="submit"
                                    name="returnToParent"
                                    class="contentButton">
                                <span class="ui-button-text">&lt;&lt; Return to ${actionBean.returnToParentTarget}</span>
                            </button>
                            <% for(Map.Entry<String, String> param : actionBean.getReturnToParentParams().entrySet()) { %>
                                <input type="hidden" name="<%= param.getKey() %>" value="<%= param.getValue() %>" />
                            <% } %>
                        </c:if>
                        <jsp:include page="breadcrumbs.jsp" />
                    </stripes:form>
                </div>
                <div class="contentBarRight">
                    <stripes:form action="/actions/admin/page" method="post" id="pageAdminForm">
                        <input type="hidden" name="originalPath" value="${actionBean.dispatch.originalPath}" />
                        <!-- Admin buttons -->
                        <% if(SecurityLogic.isAdministrator(request)) { %>
                            <div class="contentBarButtons">
                                <portofino:page-layout-button />
                                <portofino:reload-model-button />
                                <portofino:page-children-button />
                                <portofino:page-permissions-button />
                                <portofino:page-copy-button />
                                <portofino:page-new-button />
                                <portofino:page-delete-button />
                                <portofino:page-move-button />
                            </div>
                        <% } %>
                    </stripes:form>
                    <!-- End admin buttons -->
                    <portofino:result-set-navigation />
                </div>
            </div>
        </stripes:layout-component>
    </div>
    <div class="contentBody">
        <stripes:layout-component name="contentBody">
        </stripes:layout-component>
    </div>
    <div class="contentFooter">
        <stripes:layout-component name="contentFooter">
        </stripes:layout-component>
    </div>
</stripes:layout-definition>