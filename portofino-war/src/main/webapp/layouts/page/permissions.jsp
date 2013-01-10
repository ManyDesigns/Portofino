<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><%@ page import="com.manydesigns.portofino.dispatcher.PageInstance"
%><%@ page import="com.manydesigns.portofino.logic.SecurityLogic"
%><%@ page import="com.manydesigns.portofino.pages.Page"
%><%@ page import="com.manydesigns.portofino.pages.Permissions"
%><%@ page import="com.manydesigns.portofino.security.AccessLevel"
%><%@ page import="com.manydesigns.portofino.shiro.GroupPermission"
%><%@ page import="com.manydesigns.portofino.shiro.PagePermission"
%><%@ page import="java.util.Collections"
%><stripes:layout-render name="/skins/${skin}${actionBean.pageTemplate}/modal.jsp" formActionUrl="/actions/admin/page">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.admin.page.PageAdminAction"/>
    <stripes:layout-component name="contentHeader">
        <portofino:buttons list="page-permissions-edit" cssClass="contentButton" />
        <jsp:include page="/skins/${skin}/breadcrumbs.jsp" />
    </stripes:layout-component>
    <stripes:layout-component name="portletTitle">
        <fmt:message key="layouts.page.permissions.page-permissions-for" />
        <c:out value="${actionBean.page.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <input type="hidden" name="originalPath" value="${actionBean.dispatch.originalPath}" />
        <div class="yui-gc">
            <div class="yui-u first" style="width: auto;">
                <table>
                <%
                    PageInstance currentPageInstance = actionBean.getPageInstance();
                    Page currentPage = currentPageInstance.getPage();
                    String[] supportedPermissions = actionBean.getSupportedPermissions();
                    if(supportedPermissions != null) {
                %>
                    <tr>
                        <th rowspan="2"><fmt:message key="layouts.page.permissions.group" /></th>
                        <th rowspan="2"><fmt:message key="layouts.page.permissions.access-level" /></th>
                        <th colspan="<%= supportedPermissions.length %>"><fmt:message key="layouts.page.permissions.permissions" /></th>
                    </tr>
                    <tr>
                        <c:forEach var="perm" items="<%= supportedPermissions %>">
                            <th><fmt:message key="${perm}" /></th>
                        </c:forEach>
                    </tr>
                <% } else { %>
                    <tr>
                        <th><fmt:message key="layouts.page.permissions.group" /></th>
                        <th><fmt:message key="layouts.page.permissions.access-level" /></th>
                    </tr>
                <% } %>
                    <c:forEach var="group" items="${actionBean.groups}">
                        <tr>
                            <%
                                final String groupId = (String) pageContext.getAttribute("group");
                                GroupPermission groupPermission = new GroupPermission(Collections.singleton(groupId));

                                AccessLevel localAccessLevel = actionBean.getLocalAccessLevel(currentPage, groupId);
                                AccessLevel parentAccessLevel = null;
                                PageInstance parentPageInstance = currentPageInstance.getParent();
                                if(parentPageInstance != null) {
                                    Permissions parentPermissions =
                                            SecurityLogic.calculateActualPermissions(parentPageInstance);
                                    parentAccessLevel = parentPermissions.getActualLevels().get(groupId);
                                }
                                if(parentAccessLevel == null) {
                                    parentAccessLevel = AccessLevel.NONE;
                                }
                                Permissions permissions = SecurityLogic.calculateActualPermissions(currentPageInstance);
                            %>
                            <td>
                                <c:out value="${group}"/>
                            </td>
                            <td>
                                <select name="accessLevels[${group}]"
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
                            <c:forEach var="perm" items="<%= supportedPermissions %>">
                                <td>
                                    <input type="checkbox" name="permissions[<%= groupId %>]"
                                           value="${perm}"
                                           <%
                                               String testedPermission = (String) pageContext.getAttribute("perm");
                                               Permissions testConf = new Permissions();
                                               testConf.getActualPermissions()
                                                       .put(groupId, permissions.getActualPermissions().get(groupId));
                                               testConf.getActualLevels().put(groupId, AccessLevel.DEVELOP);
                                               PagePermission pagePermission =
                                                       new PagePermission
                                                               (testConf, AccessLevel.NONE, testedPermission);
                                               if(groupPermission.implies(pagePermission)) {
                                                   out.print("checked='checked'");
                                               }
                                           %>/>
                                </td>
                            </c:forEach>
                        </tr>
                    </c:forEach>
                </table>
            </div>
            <div class="yui-u" style="min-width: 20em;">
                <h2 style="margin-top: 0;"><fmt:message key="layouts.page.permissions.test-a-user" /></h2>
                <label for="testUserIdSelect"><fmt:message key="layouts.page.permissions.select-a-user" /></label>
                <c:if test="${actionBean.users != null}">
                    <select name="testUserId" id="testUserIdSelect">
                        <c:forEach var="user" items="${actionBean.users}">
                            <option value="${user}"
                                    <c:if test="${actionBean.testUserId eq user}">selected="selected"</c:if>
                                    ><c:out value="${empty user ? '(anonymous)' : user}" /></option>
                        </c:forEach>
                    </select>
                </c:if>
                <c:if test="${actionBean.users == null}">
                    <input name="testUserId" id="testUserIdSelect" type="text" value="${actionBean.testUserId}" />
                </c:if>
                <input type="hidden" name="originalPath" value="${actionBean.dispatch.originalPath}" />
                <portofino:buttons list="testUserPermissions" cssClass="portletButton" />
                <br /><br />
                <table id="userPermissionTestResults">
                    <c:if test="${not empty actionBean.testedAccessLevel}">
                        <tr>
                            <th><fmt:message key="layouts.page.permissions.access-level" />:</th>
                            <td><fmt:message key='<%= "permissions.level." + actionBean.getTestedAccessLevel().name().toLowerCase() %>' /></td>
                        </tr>
                    </c:if>
                    <c:if test="${not empty actionBean.testedPermissions}">
                        <tr>
                            <th><fmt:message key="layouts.page.permissions.permissions" />:</th>
                            <td>
                                <ul style="margin: 0;">
                                    <c:forEach var="perm" items="${actionBean.testedPermissions}">
                                        <li><fmt:message key="${perm}" /></li>
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