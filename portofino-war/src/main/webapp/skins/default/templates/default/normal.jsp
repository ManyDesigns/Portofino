<%@ page import="java.util.Map"
%><%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><stripes:layout-definition><%--
--%><!DOCTYPE html>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en">
    <head>
        <jsp:include page="../../head.jsp"/>
        <!--<link rel="stylesheet" type="text/css"
              href="<stripes:url value="/skins/${skin}/templates/site/site.css"/>"/>-->
        <stripes:layout-component name="customScripts"/>
        <jsp:useBean id="actionBean" scope="request"
                     type="com.manydesigns.portofino.pageactions.AbstractPageAction"/>
        <title><c:out value="${actionBean.dispatch.lastPageInstance.page.description}"/></title>
    </head>
    <body class="yui-skin-sam">
    <div class="container">
        <div class="row">
            <jsp:include page="../../header.jsp"/>
        </div>
        <div class="row">
            <div class="span2 portofino-sidebar">
                <portofino:portlets list="aboveNavigation" />
                <div id="navigation">
                    <jsp:include page="../../navigation.jsp" />
                </div>
                <portofino:portlets list="belowNavigation" />
            </div>
            <div id="content" class="span10">
                <stripes:layout-component name="contentHeader">
                    <div class="contentHeader row-fluid">
                        <div class="span6">
                            <stripes:form action="${actionBean.dispatch.originalPath}" method="post" enctype="multipart/form-data" class="form-horizontal">
                                <c:if test="${not empty actionBean.returnToParentTarget}">
                                    <button type="submit" name="returnToParent" class="contentButton btn">
                                        <span>&lt;&lt; <%= actionBean.getMessage("commons.returnToParent", actionBean.returnToParentTarget) %></span>
                                    </button>
                                    <% for(Map.Entry<String, String> param : actionBean.getReturnToParentParams().entrySet()) { %>
                                        <input type="hidden" name="<%= param.getKey() %>" value="<%= param.getValue() %>" />
                                    <% } %>
                                </c:if>
                                <jsp:include page="../../breadcrumbs.jsp" />
                            </stripes:form>
                        </div>
                        <div class="span6">
                            <div class="text-right">
                                <stripes:form action="/actions/admin/page" method="post" id="pageAdminForm" class="form-horizontal">
                                    <input type="hidden" name="originalPath" value="${actionBean.dispatch.originalPath}" />
                                    <!-- Admin buttons -->
                                    <div class="contentBarButtons">
                                        <portofino:page-layout-button />
                                        <portofino:page-children-button />
                                        <portofino:page-permissions-button />
                                        <portofino:page-copy-button />
                                        <portofino:page-new-button />
                                        <portofino:page-delete-button />
                                        <portofino:page-move-button />
                                    </div>
                                </stripes:form>
                                <!-- End admin buttons -->
                                <portofino:result-set-navigation />
                            </div>
                        </div>
                    </div>
                </stripes:layout-component>
                <div class="contentBody">
                    <stripes:layout-component name="contentBody">
                        <mde:sessionMessages/>
                        <portofino:portlets list="default" cssClass="row-fluid" />
                        <div class="row-fluid">
                            <portofino:portlets list="contentLayoutLeft" cssClass="span6" />
                            <portofino:portlets list="contentLayoutRight" cssClass="span6" />
                        </div>
                        <portofino:portlets list="contentLayoutBottom" cssClass="row-fluid" />
                    </stripes:layout-component>
                </div>
                <div class="contentFooter">
                    <stripes:layout-component name="contentFooter">
                    </stripes:layout-component>
                </div>
            </div>
        </div>
        <div class="row">
            <jsp:include page="../../footer.jsp"/>
        </div>
    </div>
    </body>
    </html>
</stripes:layout-definition>