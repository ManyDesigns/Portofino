<%@ page import="com.manydesigns.portofino.dispatcher.PageInstance" %>
<%@ page import="com.manydesigns.portofino.pages.Page" %>
<%@ page import="com.manydesigns.portofino.security.AccessLevel" %>
<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<stripes:layout-render name="/skins/default/admin-page.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.admin.page.RootConfigurationAction"/>
    <stripes:layout-component name="pageTitle">
        <fmt:message key="layouts.admin.rootPermissions" />
    </stripes:layout-component>
    <stripes:layout-component name="contentHeader">
        <portofino:buttons list="root-permissions" cssClass="contentButton" />
    </stripes:layout-component>
    <stripes:layout-component name="portletTitle">
        <fmt:message key="layouts.admin.rootPermissions" />
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <input type="hidden" name="originalPath" value="${actionBean.dispatch.originalPath}" />
        <div class="yui-gc">
            <div class="yui-u first">
                <table>
                    <%
                        PageInstance currentPageInstance = actionBean.getPageInstance();
                        Page currentPage = currentPageInstance.getPage();
                    %>
                    <tr>
                        <th><fmt:message key="layouts.page.permissions.group" /></th>
                        <th><fmt:message key="layouts.page.permissions.access-level" /></th>
                    </tr>
                    <c:forEach var="group" items="${actionBean.groups}">
                        <tr>
                            <%
                                final String groupId = (String) pageContext.getAttribute("group");
                                AccessLevel localAccessLevel = actionBean.getLocalAccessLevel(currentPage, groupId);
                            %>
                            <td>
                                <c:out value="${group}"/>
                            </td>
                            <td>
                                <select name="accessLevels[${group}]">
                                    <option value="<%= AccessLevel.NONE.name() %>"
                                            <%
                                                if (AccessLevel.NONE.equals(localAccessLevel)) {
                                                    out.print("selected='selected'");
                                                }
                                            %>>
                                        <fmt:message key="permissions.level.none" />
                                    </option>
                                    <option value="<%= AccessLevel.VIEW.name() %>"
                                            <%
                                                if (AccessLevel.VIEW.equals(localAccessLevel)) {
                                                    out.print("selected='selected'");
                                                }
                                            %>>
                                        <fmt:message key="permissions.level.view" />
                                    </option>
                                    <option value="<%= AccessLevel.EDIT.name() %>"
                                            <%
                                                if (AccessLevel.EDIT.equals(localAccessLevel)) {
                                                    out.print("selected='selected'");
                                                }
                                            %>>
                                        <fmt:message key="permissions.level.edit" />
                                    </option>
                                    <option value="<%= AccessLevel.DEVELOP.name() %>"
                                            <%
                                                if (AccessLevel.DEVELOP.equals(localAccessLevel)) {
                                                    out.print("selected='selected'");
                                                }
                                            %>>
                                        <fmt:message key="permissions.level.develop" />
                                    </option>
                                    <option value="<%= AccessLevel.DENY.name() %>"
                                            <%
                                                if (AccessLevel.DENY.equals(localAccessLevel)) {
                                                    out.print("selected='selected'");
                                                }
                                            %>>
                                        <fmt:message key="permissions.level.deny" />
                                    </option>
                                </select>
                            </td>
                        </tr>
                    </c:forEach>
                </table>
            </div>
        </div>
    </stripes:layout-component>
    <stripes:layout-component name="contentFooter">
        <portofino:buttons list="root-permissions" cssClass="contentButton" />
    </stripes:layout-component>
</stripes:layout-render>