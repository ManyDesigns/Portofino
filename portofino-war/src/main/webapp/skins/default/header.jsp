<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"
%><%@ page import="com.manydesigns.portofino.logic.SecurityLogic"
%><%@ page import="com.manydesigns.portofino.pageactions.AbstractPageAction"
%><%@ page import="com.manydesigns.portofino.shiro.ShiroUtils"
%><%@ page import="net.sourceforge.stripes.util.UrlBuilder"
%>
<%@ page import="org.apache.shiro.SecurityUtils" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%>
<stripes:url var="profileUrl" value="/actions/profile"/>
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
            <button type="button" class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <h4 id="app-title"><stripes:link href="/"><c:out value="${app.name}"/></stripes:link></h4>
            <c:if test="<%= actionBean instanceof AbstractPageAction && ((AbstractPageAction) actionBean).getDispatch() != null %>">
                <%-- Skip in the admin section --%>
                <div id="app-menu" class="nav-collapse collapse">
                    <stripes:form action="/actions/admin/page" method="post" id="pageAdminForm" class="form-horizontal">
                        <input type="hidden" name="originalPath" value="${actionBean.dispatch.originalPath}" />
                        <!-- Admin buttons -->
                        <ul class="nav">
                            <li><portofino:page-layout-button /></li>
                            <li><portofino:page-children-button /></li>
                            <li><portofino:page-permissions-button /></li>
                            <li><portofino:page-copy-button /></li>
                            <li><portofino:page-new-button /></li>
                            <li><portofino:page-delete-button /></li>
                            <li><portofino:page-move-button /></li>
                        </ul>
                    </stripes:form>
                    <!-- End admin buttons -->
                </div>
            </c:if>
            <div id="globalLinks" class="nav-collapse collapse">
                <ul class="nav">
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
