<%@ page import="com.manydesigns.portofino.logic.SecurityLogic" %>
<%@ page import="com.manydesigns.portofino.model.pages.AccessLevel" %>
<%@ page import="com.manydesigns.portofino.model.pages.Page" %>
<%@ page import="com.manydesigns.portofino.model.pages.Permissions" %>
<%@ page import="com.manydesigns.portofino.system.model.users.Group" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<stripes:layout-definition>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.PortletAction"/>
<%
    Group group = (Group) pageContext.getAttribute("group");
    Page currentPage = (Page) pageContext.getAttribute("currentPage");
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
    <c:forEach var="perm" items="${supportedPermissions}">
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
</stripes:layout-definition>