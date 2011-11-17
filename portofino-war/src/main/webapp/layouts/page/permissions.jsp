<%@ page import="com.manydesigns.portofino.model.pages.Page" %>
<%@ page import="com.manydesigns.portofino.model.pages.Permissions" %>
<%@ page import="com.manydesigns.portofino.system.model.users.Group" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.List" %>
<%@ page import="com.manydesigns.portofino.actions.PortletAction" %>
<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="portofino" uri="/manydesigns-portofino" %>
<stripes:layout-render name="/skins/${skin}/modal-page.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.PortletAction"/>
    <stripes:layout-component name="contentHeader">
        <portofino:buttons list="page-permissions-edit" bean="${actionBean}" cssClass="contentButton" />
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
        <%
            Page currentPage = actionBean.getPageInstance().getPage();
        %>
        <div class="yui-gb">
            <table class="yui-u first">
                <c:forEach var="group" items="${actionBean.groups}">
                    <%
                        Group group = (Group) pageContext.getAttribute("group");
                        String groupName = group.getName();
                        String permissionLevel = actionBean.getPermissionLevelName(currentPage, groupName);
                        String parentPermissionLevel = null;
                        Page parentPage = currentPage.getParent();
                        while(parentPermissionLevel == null && parentPage != null) {
                            parentPermissionLevel = actionBean.getEffectivePermissionLevel(parentPage, groupName);
                            parentPage = parentPage.getParent();
                        }
                    %>
                    <tr>
                        <td>
                            <c:out value="${group.name}"/>
                        </td>
                        <td>
                            <select name="permissions[${group.name}]">
                                <option value="__inherited">
                                    Inherited - <%= parentPermissionLevel %>
                                </option>
                                <option value="<%= PortletAction.DENY %>"
                                        <%
                                            if (PortletAction.DENY.equals(permissionLevel)) {
                                                out.print("selected='selected'");
                                            }
                                        %>>
                                    Deny
                                </option>
                                <option value="<%= Permissions.VIEW %>"
                                        <%
                                            if (Permissions.VIEW.equals(permissionLevel)) {
                                                out.print("selected='selected'");
                                            }
                                        %>>
                                    View
                                </option>
                                <option value="<%= Permissions.EDIT %>"
                                        <%
                                            if (Permissions.EDIT.equals(permissionLevel)) {
                                                out.print("selected='selected'");
                                            }
                                        %>>
                                    Edit
                                </option>
                            </select>
                        </td>
                    </tr>
                </c:forEach>
            </table>

            <div class="yui-u">
                <h3>Effective permissions</h3>
                <c:forEach var="group" items="${actionBean.groups}">
                    <div>
                        <c:out value="${group.name}"/>: <%
                            Group group = (Group) pageContext.getAttribute("group");
                            String groupName = group.getName();

                            String permissionLevel = actionBean.getEffectivePermissionLevel(currentPage, groupName);

                            out.print(permissionLevel);
                        %>
                    </div>
                </c:forEach>
            </div>
        </div>

        <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletFooter"/>
    <stripes:layout-component name="contentFooter">
        <portofino:buttons list="page-permissions-edit" bean="${actionBean}" cssClass="contentButton" />
    </stripes:layout-component>
</stripes:layout-render>