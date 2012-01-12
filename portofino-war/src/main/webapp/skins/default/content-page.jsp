<%
    // Avoid caching of dynamic pages
    response.setHeader("Pragma", "no-cache");
    response.addHeader("Cache-Control", "must-revalidate");
    response.addHeader("Cache-Control", "no-cache");
    response.addHeader("Cache-Control", "no-store");
    response.setDateHeader("Expires", 0);
%><%@ page import="com.manydesigns.portofino.logic.SecurityLogic"
%>
<%@ page import="java.util.Map" %>
<%@ page contentType="text/html;charset=ISO-8859-1" language="java" pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><stripes:layout-definition><%--
--%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en">
    <head>
        <jsp:include page="head.jsp"/>
        <stripes:layout-component name="customScripts"/>
        <jsp:useBean id="actionBean" scope="request"
             type="com.manydesigns.portofino.actions.PortletAction"/>
        <title><c:out value="${actionBean.dispatch.lastPageInstance.page.description}"/></title>
    </head>
    <body class="yui-skin-sam">
    <div id="doc3" class="yui-t2">
        <div id="hd">
            <jsp:include page="header.jsp"/>
        </div>
        <div id="bd">
            <div id="yui-main">
                <div id="content" class="yui-b">
                    <div class="contentHeader">
                        <stripes:layout-component name="contentHeader">
                            <stripes:form action="${actionBean.dispatch.originalPath}" method="post" id="contentHeaderForm">
                                <div class="yui-g">
                                    <div class="contentBarLeft">
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
                                        <div class="breadcrumbs">
                                            <div class="inner">
                                                <mde:write name="breadcrumbs"/>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="contentBarRight">
                                        <!-- Admin buttons -->
                                        <% if(SecurityLogic.isAdministrator(request)) { %>
                                            <div class="contentBarButtons">
                                                <portofino:page-layout-button />
                                                <portofino:reload-model-button />
                                                <portofino:page-permissions-button />
                                                <portofino:page-copy-button />
                                                <portofino:page-new-button />
                                                <portofino:page-delete-button />
                                                <portofino:page-move-button />
                                            </div>
                                        <% } %>
                                        <!-- End admin buttons -->
                                        <portofino:result-set-navigation />
                                    </div>
                                </div>
                            </stripes:form>
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
                </div>
            </div>
            <div id="sidebar" class="yui-b">
                <mde:write name="navigation"/>
            </div>
            <script type="text/javascript">
                fixSideBar();
            </script>
        </div>
        <div id="ft">
            <jsp:include page="footer.jsp"/>
        </div>
    </div>
    </body>
    </html>
</stripes:layout-definition>