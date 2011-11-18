<%@ page import="com.manydesigns.portofino.actions.PortletAction" %>
<%@ page import="com.manydesigns.portofino.model.pages.Page" %>
<%@ page import="com.manydesigns.portofino.model.pages.Permissions" %>
<%@ page import="com.manydesigns.portofino.system.model.users.Group" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<stripes:layout-definition>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.PortletAction"/>
<%
    Group group = (Group) pageContext.getAttribute("group");
    Page currentPage = (Page) pageContext.getAttribute("currentPage");
    String groupId = group.getGroupId();
    String permissionLevel = actionBean.getPermissionLevelName(currentPage, groupId);
    String effectivePermissionLevel = actionBean.getEffectivePermissionLevel(currentPage, groupId);
    if(effectivePermissionLevel == null) {
        effectivePermissionLevel = "None";
    }
    String parentPermissionLevel = null;
    Page parentPage = currentPage.getParent();
    while(parentPermissionLevel == null && parentPage != null) {
        parentPermissionLevel = actionBean.getEffectivePermissionLevel(parentPage, groupId);
        parentPage = parentPage.getParent();
    }
    if(parentPermissionLevel == null) {
        parentPermissionLevel = "None";
    }
%>
    <td>
        <c:out value="${group.name}"/>
    </td>
    <td>
        <select name="permissions[${group.groupId}]"
                <%
                    if("Deny".equals(parentPermissionLevel)) {
                        out.print("disabled='disabled'");
                    }
                %>>
            <option value="__inherited">
                Inherited (<%= parentPermissionLevel %>)
            </option>
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
    <td><%= effectivePermissionLevel %></td>
</stripes:layout-definition>