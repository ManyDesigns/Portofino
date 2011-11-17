<%@ page import="com.manydesigns.portofino.actions.PortletAction" %>
<%@ page import="com.manydesigns.portofino.model.pages.Page" %>
<%@ page import="com.manydesigns.portofino.model.pages.Permissions" %>
<%@ page import="com.manydesigns.portofino.system.model.users.Group" %>
<%@ page import="java.util.Collections" %>
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
        <%@include file="/layouts/page/pagePermissions.jsp"%>
        <div class="horizontalSeparator"></div>
        <h3>Crud permissions</h3>
        <table>
            <tr>
                <th>Group</th>
                <th>Create</th>
                <th>Edit</th>
                <th>Delete</th>
            </tr>
            <c:forEach var="group" items="${actionBean.groups}">
                <%
                    Group group = (Group) pageContext.getAttribute("group");
                %>
                <tr>
                    <td><c:out value="${group.name}" /></td>
                    <td><input type="checkbox" name="customPermissions[create]"
                               value="${group.name}"
                               <%
                                   if(currentPage.isAllowed("create", Collections.singletonList(group.getName()))) {
                                       out.print("checked='checked'");
                                   }
                               %>/>
                    </td>
                    <td><input type="checkbox" name="customPermissions[edit]"
                               value="${group.name}"
                               <%
                                   if(currentPage.isAllowed("edit", Collections.singletonList(group.getName()))) {
                                       out.print("checked='checked'");
                                   }
                               %> />
                    </td>
                    <td><input type="checkbox" name="customPermissions[delete]"
                               value="${group.name}"
                               <%
                                   if(currentPage.isAllowed("delete", Collections.singletonList(group.getName()))) {
                                       out.print("checked='checked'");
                                   }
                               %> />
                    </td>
                </tr>
            </c:forEach>
        </table>
    </stripes:layout-component>
    <stripes:layout-component name="portletFooter"/>
    <stripes:layout-component name="contentFooter">
        <portofino:buttons list="page-permissions-edit" bean="${actionBean}" cssClass="contentButton" />
    </stripes:layout-component>
</stripes:layout-render>