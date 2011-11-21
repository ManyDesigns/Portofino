<%@ page import="com.manydesigns.portofino.actions.PortletAction" %>
<%@ page import="com.manydesigns.portofino.model.pages.Page" %>
<%@ page import="com.manydesigns.portofino.model.pages.Permissions" %>
<%@ page import="com.manydesigns.portofino.system.model.users.Group" %>
<%@ page import="com.manydesigns.portofino.system.model.users.annotations.SupportsPermissions" %>
<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<stripes:layout-render name="/skins/${skin}/modal-page.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.PortletAction"/>
    <stripes:layout-component name="contentHeader">
        <stripes:layout-render name="/layouts/page/buttons.jsp" list="page-permissions-edit" cssClass="contentButton" />
        <div class="breadcrumbs">
            <div class="inner">
                <mde:write name="breadcrumbs"/>
            </div>
        </div>
    </stripes:layout-component>
    <stripes:layout-component name="portletTitle">
        Page permissions for: <c:out value="${actionBean.pageInstance.page.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <table>
        <%
            Page currentPage = actionBean.getPageInstance().getPage();
            SupportsPermissions supportsPermissions =
                    actionBean.getClass().getAnnotation(SupportsPermissions.class);
            String[] supportedPermissions = null;
            if(supportsPermissions != null && supportsPermissions.value().length > 0) {
                supportedPermissions = supportsPermissions.value();
            }
            if(supportedPermissions != null) {
        %>
            <tr>
                <th rowspan="2">Group</th>
                <th rowspan="2">Access level</th>
                <th colspan="<%= supportedPermissions.length %>">Permissions</th>
            </tr>
            <tr>
                <c:forEach var="perm" items="<%= supportedPermissions %>">
                    <th><fmt:message key="permissions.${perm}" /></th>
                </c:forEach>
            </tr>
        <% } else { %>
            <tr>
                <th>Group</th>
                <th>Access level</th>
            </tr>
        <% } %>
            <c:forEach var="group" items="${actionBean.groups}">
                <tr>
                    <stripes:layout-render
                            name="pagePermissionRow.jsp"
                            currentPage="<%= currentPage %>"
                            group="${group}"
                            supportedPermissions="<%= supportedPermissions %>"/>
                </tr>
            </c:forEach>
        </table>
        <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletFooter"/>
    <stripes:layout-component name="contentFooter">
        <stripes:layout-render name="/layouts/page/buttons.jsp" list="page-permissions-edit" cssClass="contentButton" />
    </stripes:layout-component>
</stripes:layout-render>