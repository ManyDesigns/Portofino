<%@ page import="com.manydesigns.portofino.logic.SecurityLogic" %>
<%@ page import="com.manydesigns.portofino.model.pages.Page" %>
<%@ page import="com.manydesigns.portofino.model.pages.Permissions" %>
<%@ page import="com.manydesigns.portofino.model.pages.RootPage" %>
<%@ page import="com.manydesigns.portofino.system.model.users.Group" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.List" %>
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
        <div class="yui-gb">
            <table class="yui-u first">
                <c:forEach var="group" items="${actionBean.groups}">
                    <%
                        Group group = (Group) pageContext.getAttribute("group");
                        String groupName = group.getName();
                        String permissionLevel = actionBean.getPermissionLevelName(rootPage, groupName);
                    %>
                    <tr>
                        <td>
                            <c:out value="${group.name}"/>
                        </td>
                        <td>
                            <select name="permissions[${group.name}]">
                                <option value="__inherited">
                                    Default - View
                                </option>
                                <option value="__deny"
                                        <%
                                            if (Permissions.DENY.equals(permissionLevel)) {
                                                out.print("selected='selected'");
                                            }
                                        %>>
                                    Deny
                                </option>
                                <option value="view"
                                        <%
                                            if (Permissions.VIEW.equals(permissionLevel)) {
                                                out.print("selected='selected'");
                                            }
                                        %>>
                                    View
                                </option>
                                <option value="edit"
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

                            String permissionLevel = actionBean.getEffectivePermissionLevel(rootPage, groupName);

                            out.print(permissionLevel);
                        %>
                    </div>
                </c:forEach>
            </div>
        </div>

    </stripes:layout-component>
    <stripes:layout-component name="contentFooter">
        <portofino:buttons list="root-permissions" bean="${actionBean}" cssClass="contentButton" />
    </stripes:layout-component>
</stripes:layout-render>