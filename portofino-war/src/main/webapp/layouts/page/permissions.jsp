<%@ page import="com.manydesigns.portofino.logic.SecurityLogic" %>
<%@ page import="com.manydesigns.portofino.model.pages.AccessLevel" %>
<%@ page import="com.manydesigns.portofino.model.pages.Page" %>
<%@ page import="com.manydesigns.portofino.model.pages.Permissions" %>
<%@ page import="com.manydesigns.portofino.system.model.users.Group" %>
<%@ page import="com.manydesigns.portofino.system.model.users.annotations.SupportsPermissions" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino" %>
<stripes:layout-render name="/skins/${skin}/modal-page.jsp" formActionUrl="/actions/admin/page">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.admin.PageAdminAction"/>
    <stripes:layout-component name="contentHeader">
        <portofino:buttons list="page-permissions-edit" cssClass="contentButton" />
        <div class="breadcrumbs">
            <div class="inner">
                <mde:write name="breadcrumbs"/>
            </div>
        </div>
    </stripes:layout-component>
    <stripes:layout-component name="portletTitle">
        Page permissions for: <c:out value="${actionBean.page.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <input type="hidden" name="originalPath" value="${actionBean.dispatch.originalPath}" />
        <div class="yui-gc">
            <div class="yui-u first">
                <table>
                <%
                    Page currentPage = actionBean.getPage();
                    Class<?> actualActionClass = currentPage.getActualActionClass();
                    SupportsPermissions supportsPermissions =
                            actualActionClass.getAnnotation(SupportsPermissions.class);
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
                            <%
                                Group group = (Group) pageContext.getAttribute("group");
                                String groupId = group.getGroupId();
                                AccessLevel localAccessLevel = actionBean.getLocalAccessLevel(currentPage, groupId);
                                AccessLevel parentAccessLevel = null;
                                Page parentPage = currentPage.getParent();
                                if(parentPage != null) {
                                    parentAccessLevel = parentPage.getPermissions().getActualLevels().get(groupId);
                                }
                                if(parentAccessLevel == null) {
                                    parentAccessLevel = AccessLevel.NONE;
                                }
                                Permissions permissions = currentPage.getPermissions();
                                List<String> groupIdList = Collections.singletonList(groupId);
                            %>
                            <td>
                                <c:out value="${group.name}"/>
                            </td>
                            <td>
                                <select name="accessLevels[${group.groupId}]"
                                        <%
                                            if(AccessLevel.DENY.equals(parentAccessLevel)) {
                                                out.print("disabled='disabled'");
                                            }
                                        %>>
                                    <option value="">
                                        <fmt:message key='<%= "permissions.level." + parentAccessLevel.name().toLowerCase() %>'
                                                     var="parentAccessLevelName" />
                                        <fmt:message key="permissions.level.inherited">
                                            <fmt:param value="${parentAccessLevelName}" />
                                        </fmt:message>
                                    </option>
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
                            <c:forEach var="perm" items="<%= supportedPermissions %>">
                                <td>
                                    <input type="checkbox" name="permissions[<%= groupId %>]"
                                           value="${perm}"
                                           <%
                                               if(SecurityLogic.hasPermissions(permissions, groupIdList, null,
                                                       (String) pageContext.getAttribute("perm"))) {
                                                   out.print("checked='checked'");
                                               }
                                           %>/>
                                </td>
                            </c:forEach>
                        </tr>
                    </c:forEach>
                </table>
            </div>
            <div class="yui-u">
                <h2 style="margin-top: 0;">Test a user</h2>
                Select a user and view its permissions about this page:
                <select name="testUserId">
                    <c:forEach var="user" items="${actionBean.users}">
                        <option value="${user.userId}"
                                <c:if test="${actionBean.testUserId eq user.userId}">selected="selected"</c:if>
                                ><c:out value="${user.userName}" /></option>
                    </c:forEach>
                </select>
                <portofino:buttons list="testUserPermissions" cssClass="portletButton" />
                <br /><br />
                <table id="userPermissionTestResults">
                    <c:if test="${not empty actionBean.testedAccessLevel}">
                        <tr>
                            <th>Access Level:</th>
                            <td><fmt:message key='<%= "permissions.level." + actionBean.getTestedAccessLevel().name().toLowerCase() %>' /></td>
                        </tr>
                    </c:if>
                    <c:if test="${not empty actionBean.testedPermissions}">
                        <tr>
                            <th>Permissions:</th>
                            <td>
                                <ul style="margin: 0;">
                                    <c:forEach var="perm" items="${actionBean.testedPermissions}">
                                        <li><fmt:message key="permissions.${perm}" /></li>
                                    </c:forEach>
                                </ul>
                            </td>
                        </tr>
                    </c:if>
                </table>
            </div>
        </div>
        <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletFooter"/>
    <stripes:layout-component name="contentFooter">
        <portofino:buttons list="page-permissions-edit" cssClass="contentButton" />
    </stripes:layout-component>
</stripes:layout-render>