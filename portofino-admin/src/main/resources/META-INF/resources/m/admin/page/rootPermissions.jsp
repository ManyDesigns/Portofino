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
<stripes:layout-render name="/m/admin/admin-theme/admin-page.jsp">
    <jsp:useBean id="actionBean" scope="request"
                 type="com.manydesigns.portofino.actions.admin.page.RootConfigurationAction"/>
    <stripes:layout-component name="pageTitle">
        <fmt:message key="root.permissions" />
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <style type="text/css">
            select {
                margin-bottom: 0 !important;
            }
        </style>
        <stripes:form beanclass="com.manydesigns.portofino.actions.admin.page.RootPermissionsAction"
                      method="post" enctype="multipart/form-data">
            <input type="hidden" name="originalPath" value="${actionBean.originalPath}" />
            <table class="table table-striped">
                <%
                    PageInstance currentPageInstance = actionBean.getPageInstance();
                    Page currentPage = currentPageInstance.getPage();
                %>
                <tr>
                    <th><fmt:message key="group" /></th>
                    <th><fmt:message key="access.level" /></th>
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
                                    <fmt:message key="none" />
                                </option>
                                <option value="<%= AccessLevel.VIEW.name() %>"
                                        <%
                                            if (AccessLevel.VIEW.equals(localAccessLevel)) {
                                                out.print("selected='selected'");
                                            }
                                        %>>
                                    <fmt:message key="view" />
                                </option>
                                <option value="<%= AccessLevel.EDIT.name() %>"
                                        <%
                                            if (AccessLevel.EDIT.equals(localAccessLevel)) {
                                                out.print("selected='selected'");
                                            }
                                        %>>
                                    <fmt:message key="edit" />
                                </option>
                                <option value="<%= AccessLevel.DEVELOP.name() %>"
                                        <%
                                            if (AccessLevel.DEVELOP.equals(localAccessLevel)) {
                                                out.print("selected='selected'");
                                            }
                                        %>>
                                    <fmt:message key="develop" />
                                </option>
                                <option value="<%= AccessLevel.DENY.name() %>"
                                        <%
                                            if (AccessLevel.DENY.equals(localAccessLevel)) {
                                                out.print("selected='selected'");
                                            }
                                        %>>
                                    <fmt:message key="deny" />
                                </option>
                            </select>
                        </td>
                    </tr>
                </c:forEach>
            </table>
            <div class="form-group">
                <portofino:buttons list="root-permissions" />
            </div>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>