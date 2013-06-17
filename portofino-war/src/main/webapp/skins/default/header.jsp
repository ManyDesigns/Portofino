<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"
%><%@ page import="com.manydesigns.portofino.logic.SecurityLogic"
%><%@ page import="com.manydesigns.portofino.pageactions.AbstractPageAction"
%><%@ page import="com.manydesigns.portofino.security.AccessLevel"
%><%@ page import="com.manydesigns.portofino.shiro.ShiroUtils"
%><%@ page import="net.sourceforge.stripes.util.UrlBuilder"
%><%@ page import="org.apache.shiro.SecurityUtils"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><stripes:url var="profileUrl" value="/actions/profile"/>
<jsp:useBean id="portofinoConfiguration" scope="application"
             type="org.apache.commons.configuration.Configuration"/>
<jsp:useBean id="model" scope="request"
             type="com.manydesigns.portofino.model.Model"/>
<jsp:useBean id="app" scope="request"
             type="com.manydesigns.portofino.application.Application"/>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.dispatcher.AbstractActionBean"/>
<fmt:setLocale value="${pageContext.request.locale}"/>
<div class="navbar navbar-inverse navbar-fixed-top">
    <div class="navbar-inner">
        <div class="container">
            <h4 id="app-title" class="pull-left"><stripes:link href="/"><c:out value="${app.name}"/></stripes:link></h4>
            <button type="button" class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <div id="header-menu" class="nav-collapse collapse">
                <c:if test="<%= actionBean instanceof AbstractPageAction && ((AbstractPageAction) actionBean).getDispatch() != null %>"><%
                        AbstractPageAction pageAction = (AbstractPageAction) actionBean;
                        pageContext.setAttribute("pageAction", pageAction);
                    %>
                    <!-- Admin buttons -->
                    <ul id="page-menu" class="nav">
                        <li class="dropdown">
                             <a class="dropdown-toggle" data-toggle="dropdown" href="#">
                                 Page <b class="caret"></b>
                             </a>
                            <ul class="dropdown-menu">
                                <% if(SecurityLogic.hasPermissions(pageAction.getPageInstance(), SecurityUtils.getSubject(), AccessLevel.EDIT)) { %>
                                <li>
                                    <a onclick="portofino.enablePortletDragAndDrop(this); return false;" title="Edit page layout">
                                        <i class="icon-move icon-white"></i> Edit page layout
                                    </a>
                                </li>
                                <li>
                                    <stripes:link href="/actions/admin/page" event="pageChildren" title="Page children">
                                        <stripes:param name="originalPath" value="${pageAction.dispatch.originalPath}" />
                                        <i class="icon-folder-open icon-white"></i> Page children
                                    </stripes:link>
                                </li>
                                <li>
                                    <stripes:link href="/actions/admin/page" event="newPage" title="Add new page">
                                        <stripes:param name="originalPath" value="${pageAction.dispatch.originalPath}" />
                                        <i class="icon-plus icon-white"></i> Add new page
                                    </stripes:link>
                                </li>
                                <li>
                                    <a onclick="confirmDeletePage(
                                            '<%= pageAction.getDispatch().getLastPageInstance().getPathFromRoot() %>',
                                            '<%= request.getContextPath() %>');
                                            return false;"
                                    title="Delete page">
                                        <i class="icon-minus icon-white"></i> Delete page
                                    </a>
                                </li>
                                <li>
                                    <a onclick="showCopyPageDialog(
                                            '<%= pageAction.getDispatch().getLastPageInstance().getPathFromRoot() %>',
                                            '<%= request.getContextPath() %>');
                                        return false;"
                                    title="Copy page">
                                        <i class="icon-file icon-white"></i> Copy page
                                    </a>
                                </li>
                                <li>
                                    <a onclick="showMovePageDialog(
                                            '<%= pageAction.getDispatch().getLastPageInstance().getPathFromRoot() %>',
                                            '<%= request.getContextPath() %>');
                                            return false;"
                                    title="Move page">
                                        <i class="icon-share icon-white"></i> Move page
                                    </a>
                                </li>
                                <% } %>
                                <% if(SecurityLogic.hasPermissions(pageAction.getPageInstance(), SecurityUtils.getSubject(), AccessLevel.DEVELOP)) { %>
                                <li>
                                    <stripes:link href="/actions/admin/page" event="pagePermissions" title="Page permissions">
                                        <stripes:param name="originalPath" value="${pageAction.dispatch.originalPath}" />
                                        <i class="icon-user icon-white"></i> Page permissions
                                    </stripes:link>
                                </li>
                                <% } %>
                            </ul>
                        </li>
                    </ul>
                    <!-- End admin buttons -->
                </c:if>
                <ul id="user-menu" class="nav">
                    <%
                        String loginLink = ShiroUtils.getLoginLink(
                                app, request.getContextPath(), actionBean.getOriginalPath(), actionBean.getOriginalPath());
                        String logoutLink = ShiroUtils.getLogoutLink(app, request.getContextPath());
                        pageContext.setAttribute("loginLink", new UrlBuilder(request.getLocale(), loginLink, true).toString());
                        pageContext.setAttribute("logoutLink", new UrlBuilder(request.getLocale(), logoutLink, true).toString());
                    %>
                    <c:if test="<%= SecurityUtils.getSubject().isAuthenticated() %>">
                        <li>
                            <a href="#">
                                <i class="icon-user icon-white"></i><c:out value="<%= ShiroUtils.getPrimaryPrincipal(SecurityUtils.getSubject()) %>"/>
                            </a>
                        </li>
                        <li>
                            <% if(SecurityLogic.isAdministrator(request)) { %>
                                <stripes:link beanclass="com.manydesigns.portofino.actions.admin.SettingsAction"><fmt:message key="skins.default.header.administration"/></stripes:link>
                            <% } %>
                        </li>
                        <li>
                            <a href="<c:out value='${logoutLink}' />"><fmt:message key="skins.default.header.log_out"/></a>
                        </li>
                    </c:if>
                    <c:if test="<%= !SecurityUtils.getSubject().isAuthenticated() %>">
                        <li>
                            <a href="<c:out value='${loginLink}' />"><fmt:message key="skins.default.header.log_in"/></a>
                        </li>
                    </c:if>
                </ul>
            </div>
        </div>
    </div>
</div>
