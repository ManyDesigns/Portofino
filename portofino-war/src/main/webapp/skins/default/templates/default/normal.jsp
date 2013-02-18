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
        <stripes:layout-component name="customScripts"/>
        <jsp:useBean id="actionBean" scope="request"
             type="com.manydesigns.portofino.pageactions.AbstractPageAction"/>
        <title><c:out value="${actionBean.dispatch.lastPageInstance.page.description}"/></title>
    </head>
    <body class="yui-skin-sam">
    <div id="doc3" class="yui-t2">
        <div id="hd">
            <jsp:include page="../../header.jsp"/>
        </div>
        <div id="bd">
            <div id="yui-main">
                <div id="content" class="yui-b">
                    <div class="contentHeader">
                        <stripes:layout-component name="contentHeader">
                            <div class="yui-g">
                                <div class="contentBarLeft">
                                    <stripes:form action="${actionBean.dispatch.originalPath}" method="post" enctype="multipart/form-data">
                                        <c:if test="${not empty actionBean.returnToParentTarget}">
                                            <button type="submit"
                                                    name="returnToParent"
                                                    class="contentButton">
                                                <span class="ui-button-text">&lt;&lt; <%= actionBean.getMessage("commons.returnToParent", actionBean.returnToParentTarget) %></span>
                                            </button>
                                            <% for(Map.Entry<String, String> param : actionBean.getReturnToParentParams().entrySet()) { %>
                                                <input type="hidden" name="<%= param.getKey() %>" value="<%= param.getValue() %>" />
                                            <% } %>
                                        </c:if>
                                        <jsp:include page="../../breadcrumbs.jsp" />
                                    </stripes:form>
                                </div>
                                <div class="contentBarRight">
                                    <stripes:form action="/actions/admin/page" method="post" id="pageAdminForm">
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
                        </stripes:layout-component>
                    </div>
                    <div class="contentBody">
                        <stripes:layout-component name="contentBody">
                            <mde:sessionMessages/>
                            <portofino:portlets list="default" />
                            <div class="yui-g">
                                <portofino:portlets list="contentLayoutLeft" cssClass="yui-u first" />
                                <portofino:portlets list="contentLayoutRight" cssClass="yui-u" />
                            </div>
                            <portofino:portlets list="contentLayoutBottom" />
                        </stripes:layout-component>
                    </div>
                    <div class="contentFooter">
                        <stripes:layout-component name="contentFooter">
                        </stripes:layout-component>
                    </div>
                </div>
            </div>
            <div id="sidebar" class="yui-b">
                <jsp:include page="../../navigation.jsp"/>
            </div>
            <script type="text/javascript">
                fixSideBar();
            </script>
        </div>
        <div id="ft">
            <jsp:include page="../../footer.jsp"/>
        </div>
    </div>
    </body>
    </html>
</stripes:layout-definition>