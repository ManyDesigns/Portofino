<%@ page import="com.manydesigns.portofino.logic.SecurityLogic" %>
<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
        %><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
        %><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
        %><%@taglib prefix="mde" uri="/manydesigns-elements"
        %><stripes:layout-render name="/skins/${skin}/content-page.jsp" >
    <jsp:useBean id="actionBean" scope="request"
                 type="com.manydesigns.portofino.actions.PortletAction"/>
    <stripes:layout-component name="contentHeader">
        <stripes:form action="${actionBean.dispatch.originalPath}" method="post" id="contentHeaderForm">
            <div class="yui-g">
                <div class="contentBarLeft">
                    <c:if test="${not empty actionBean.returnToParentTarget}">
                        <stripes:submit name="returnToParent"
                                        value="<< Return to ${actionBean.returnToParentTarget}"
                                        class="contentButton"/>
                    </c:if>
                    <div class="breadcrumbs">
                        <div class="inner">
                            <mde:write name="breadcrumbs"/>
                        </div>
                    </div>
                </div>
                <div class="contentBarRight">
                    <!-- Admin buttons -->
                    <% if(SecurityLogic.isAdministrator(request)) { %>
                        <div class="contentBarButtons">
                            <button onclick="enablePortletDragAndDrop(this); return false;"
                                    class="arrow-4">Edit page layout</button>
                            <button name="reloadModel" class="refresh">Reload model</button>
                            <button name="pagePermissions" class="person">Page permissions</button>
                            <button class="copy"
                                    onclick="showCopyPageDialog(
                                                '<%= actionBean.dispatch.getLastPageInstance().getPage().getId() %>',
                                                '<%= request.getContextPath() %>');
                                            return false;">Copy page</button>
                            <button name="newPage" class="plusthick">Add page</button>
                            <button name="deletePage" class="minusthick"
                                    onclick="confirmDeletePage(
                                                '<%= actionBean.dispatch.getLastPageInstance().getPage().getId() %>',
                                                '<%= request.getContextPath() %>');
                                            return false;">Delete page</button>
                            <button class="transferthick-e-w"
                                    onclick="showMovePageDialog(
                                                '<%= actionBean.dispatch.getLastPageInstance().getPage().getId() %>',
                                                '<%= request.getContextPath() %>');
                                            return false;">Move page</button>
                        </div>
                    <% } %>
                    <!-- End admin buttons -->
                    <c:set var="resultSetNavigation" scope="request"
                           value="${actionBean.resultSetNavigation}"/>
                    <c:if test="${not empty resultSetNavigation}">
                        <div class="resultSetNavigation">
                            <c:if test="${not empty resultSetNavigation.firstUrl}">
                                <stripes:link id="first"
                                              href="${resultSetNavigation.firstUrl}">first</stripes:link>
                                <stripes:link id="previous"
                                              href="${resultSetNavigation.previousUrl}">previous</stripes:link>
                            </c:if>
                            <c:if test="${empty resultSetNavigation.firstUrl}">
                                <span class="disabled">first</span>
                                <span class="disabled">previous</span>
                            </c:if>
                            <c:out value="${resultSetNavigation.position + 1}"/>
                            of <c:out
                                value="${resultSetNavigation.size}"/>
                            <c:if test="${not empty resultSetNavigation.lastUrl}">
                                <stripes:link id="next"
                                              href="${resultSetNavigation.nextUrl}">next</stripes:link>
                                <stripes:link id="last"
                                              href="${resultSetNavigation.lastUrl}">last</stripes:link>
                            </c:if>
                            <c:if test="${empty resultSetNavigation.lastUrl}">
                                <span class="disabled">next</span>
                                <span class="disabled">last</span>
                            </c:if>
                        </div>
                    </c:if>
                </div>
            </div>
        </stripes:form>
    </stripes:layout-component>
    <stripes:layout-component name="contentBody">
        <c:set var="pageLayout" value="${actionBean.pageInstance.page.layout}"/>
        <c:if test="${empty pageLayout}">
            <c:set var="pageLayout" value="1-2-1-symmetric.jsp"/>
        </c:if>
        <jsp:include page="/layouts/content/${pageLayout}"/>
    </stripes:layout-component>
    <stripes:layout-component name="contentFooter">
    </stripes:layout-component>
</stripes:layout-render>