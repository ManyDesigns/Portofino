<%@ page import="com.manydesigns.portofino.actions.PortletAction" %>
<%@ page import="com.manydesigns.portofino.model.pages.Permissions" %>
<%@ page import="com.manydesigns.portofino.model.pages.RootPage" %>
<%@ page import="com.manydesigns.portofino.system.model.users.Group" %>
<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="portofino" uri="/manydesigns-portofino" %>
<stripes:layout-render name="/skins/default/admin-page.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.admin.RootPermissionsAction"/>
    <stripes:layout-component name="pageTitle">
        Root permission
    </stripes:layout-component>
    <stripes:layout-component name="contentHeader">
        <portofino:buttons list="root-permissions" bean="${actionBean}" cssClass="contentButton" />
    </stripes:layout-component>
    <stripes:layout-component name="portletTitle">
        Root permission
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <%
            RootPage rootPage = actionBean.getApplication().getModel().getRootPage();
        %>
        <table>
            <tr>
                <th>Group</th>
                <th>Permissions</th>
            </tr>
            <c:forEach var="group" items="${actionBean.groups}">
                <%
                    Group group = (Group) pageContext.getAttribute("group");
                    String groupId = group.getGroupId();
                    String permissionLevel = actionBean.getPermissionLevelName(rootPage, groupId);
                %>
                <tr>
                    <td>
                        <c:out value="${group.name}"/>
                    </td>
                    <td>
                        <select name="permissions[${group.groupId}]">
                            <option value="<%= Permissions.NONE %>"
                                    <%
                                        if (Permissions.NONE.equals(permissionLevel)) {
                                            out.print("selected='selected'");
                                        }
                                    %>>
                                ---
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
                            <option value="<%= PortletAction.DENY %>"
                                    <%
                                        if (PortletAction.DENY.equals(permissionLevel)) {
                                            out.print("selected='selected'");
                                        }
                                    %>>
                                Deny
                            </option>
                        </select>
                    </td>
                </tr>
            </c:forEach>
        </table>

    </stripes:layout-component>
    <stripes:layout-component name="contentFooter">
        <portofino:buttons list="root-permissions" bean="${actionBean}" cssClass="contentButton" />
    </stripes:layout-component>
</stripes:layout-render>