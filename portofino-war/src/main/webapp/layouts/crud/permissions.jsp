<%@ page import="com.manydesigns.portofino.actions.PortletAction" %>
<%@ page import="com.manydesigns.portofino.model.pages.Page" %>
<%@ page import="com.manydesigns.portofino.model.pages.Permissions" %>
<%@ page import="com.manydesigns.portofino.system.model.users.Group" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.List" %>
<%@ page import="com.manydesigns.portofino.actions.CrudAction" %>
<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="portofino" uri="/manydesigns-portofino" %>
<stripes:layout-render name="/skins/${skin}/modal-page.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.CrudAction"/>
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
        <table>
            <tr>
                <th rowspan="2">Group</th>
                <th rowspan="2">Page permissions</th>
                <th rowspan="2">Effective permissions</th>
                <th colspan="3">Crud permissions</th>
            </tr>
            <tr>
                <th>Create</th>
                <th>Edit</th>
                <th>Delete</th>
            </tr>
            <c:forEach var="group" items="${actionBean.groups}">
                <%
                    Group group = (Group) pageContext.getAttribute("group");
                    List<String> groups = Collections.singletonList(group.getName());
                %>
                <tr>
                    <stripes:layout-render
                            name="/layouts/page/pagePermissionRow.jsp"
                            currentPage="<%= currentPage %>"
                            group="${group}"/>
                    <td><input type="checkbox" name="customPermissions[crud-create]"
                               value="${group.name}"
                               <%
                                   if(currentPage.isAllowed(CrudAction.PERMISSION_CREATE, groups)) {
                                       out.print("checked='checked'");
                                   }
                               %>/>
                    </td>
                    <td><input type="checkbox" name="customPermissions[crud-edit]"
                               value="${group.name}"
                               <%
                                   if(currentPage.isAllowed(CrudAction.PERMISSION_EDIT, groups)) {
                                       out.print("checked='checked'");
                                   }
                               %> />
                    </td>
                    <td><input type="checkbox" name="customPermissions[crud-delete]"
                               value="${group.name}"
                               <%
                                   if(currentPage.isAllowed(CrudAction.PERMISSION_DELETE, groups)) {
                                       out.print("checked='checked'");
                                   }
                               %> />
                    </td>
                </tr>
            </c:forEach>
        </table>
        <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletFooter"/>
    <stripes:layout-component name="contentFooter">
        <portofino:buttons list="page-permissions-edit" bean="${actionBean}" cssClass="contentButton" />
    </stripes:layout-component>
</stripes:layout-render>