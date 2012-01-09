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
                        <button type="submit"
                                name="returnToParent"
                                class="contentButton">
                            <span class="ui-button-text">&lt;&lt; Return to ${actionBean.returnToParentTarget}</span>
                        </button>
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
                                    type="submit"
                                    class="arrow-4 ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only"
                                    role="button" aria-disabled="false"
                                    title="Edit page layout">
                                <span class="ui-button-icon-primary ui-icon ui-icon-arrow-4"></span>
                                <span class="ui-button-text">Edit page layout</span>
                            </button>

                            <button name="reloadModel"
                                    type="submit"
                                    class="refresh ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only"
                                    role="button" aria-disabled="false"
                                    title="Reload model">
                                <span class="ui-button-icon-primary ui-icon ui-icon-refresh"></span>
                                <span class="ui-button-text">Reload model</span>
                            </button>

                            <button name="pagePermissions"
                                    type="submit"
                                    class="person ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only"
                                    role="button" aria-disabled="false"
                                    title="Page permissions">
                                <span class="ui-button-icon-primary ui-icon ui-icon-person"></span>
                                <span class="ui-button-text">Page permissions</span>
                            </button>

                            <button onclick="showCopyPageDialog(
                                                '<%= actionBean.dispatch.getLastPageInstance().getPage().getId() %>',
                                                '<%= request.getContextPath() %>');
                                            return false;"
                                    type="submit"
                                    class="copy ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only"
                                    role="button" aria-disabled="false"
                                    title="Copy page">
                                <span class="ui-button-icon-primary ui-icon ui-icon-copy"></span>
                                <span class="ui-button-text">Copy page</span>
                            </button>

                            <button name="newPage"
                                    type="submit"
                                    class="plusthick ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only"
                                    role="button" aria-disabled="false"
                                    title="Add page">
                                <span class="ui-button-icon-primary ui-icon ui-icon-plusthick"></span>
                                <span class="ui-button-text">Add page</span>
                            </button>

                            <button name="deletePage"
                                    onclick="confirmDeletePage(
                                                '<%= actionBean.dispatch.getLastPageInstance().getPage().getId() %>',
                                                '<%= request.getContextPath() %>');
                                            return false;"
                                    type="submit"
                                    class="minusthick ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only"
                                    role="button" aria-disabled="false"
                                    title="Delete page">
                                <span class="ui-button-icon-primary ui-icon ui-icon-minusthick"></span>
                                <span class="ui-button-text">Delete page</span>
                            </button>

                            <button onclick="showMovePageDialog(
                                                '<%= actionBean.dispatch.getLastPageInstance().getPage().getId() %>',
                                                '<%= request.getContextPath() %>');
                                            return false;"
                                    type="submit"
                                    class="transferthick-e-w ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only"
                                    role="button" aria-disabled="false"
                                    title="Move page">
                                <span class="ui-button-icon-primary ui-icon ui-icon-transferthick-e-w"></span>
                                <span class="ui-button-text">Move page</span>
                            </button>
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