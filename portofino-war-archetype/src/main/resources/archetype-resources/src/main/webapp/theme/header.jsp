<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"
%><%@ page import="com.manydesigns.elements.util.Util"
%><%@ page import="com.manydesigns.portofino.PortofinoProperties"
%><%@ page import="com.manydesigns.portofino.actions.admin.AdminAction"
%><%@ page import="com.manydesigns.portofino.actions.admin.page.PageAdminAction"
%><%@ page import="com.manydesigns.portofino.dispatcher.PageAction"
%><%@ page import="com.manydesigns.portofino.logic.SecurityLogic"
%><%@ page import="com.manydesigns.portofino.security.AccessLevel"
%><%@ page import="com.manydesigns.portofino.shiro.ShiroUtils"
%><%@ page import="net.sourceforge.stripes.util.UrlBuilder"
%><%@ page import="org.apache.shiro.SecurityUtils"
%><%@ page import="org.apache.shiro.subject.Subject"
%><%@ page import="java.io.Serializable"
%><%@ page import="java.util.Locale"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><stripes:url var="profileUrl" value="/actions/profile"/>
<jsp:useBean id="portofinoConfiguration" scope="application"
             type="org.apache.commons.configuration.Configuration"/>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.stripes.AbstractActionBean"/>
<fmt:setLocale value="${pageContext.request.locale}"/>
<div class="navbar navbar-inverse navbar-fixed-top">
    <div class="navbar-inner">
        <div class="container">
            <h4 class="pull-left">
                <stripes:link href="/">
                    <c:out value="<%= portofinoConfiguration.getString(PortofinoProperties.APP_NAME) %>"/>
                </stripes:link>
            </h4>
            <button type="button" class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <div class="header-menu nav-collapse collapse">
                <c:if test="${not empty actionBean.pageInstance}">
                    <form id="pageAdminForm" action="${pageContext.request.contextPath}/actions/admin/page">
                        <input type="hidden" name="originalPath" value="${actionBean.context.actionPath}" />
                    </form>
                </c:if>
                <ul class="nav">
                    <%
                        Subject subject = SecurityUtils.getSubject();
                        String actionPath = actionBean.getContext().getactionPath();
                        String loginPage = portofinoConfiguration.getString(PortofinoProperties.LOGIN_PAGE);
                        if(subject.isAuthenticated()) {
                            Object principal = subject.getPrincipal();
                            String prettyName = ShiroUtils.getPortofinoRealm().getUserPrettyName((Serializable) principal);
                    %>
                    <li class="dropdown">
                        <a class="dropdown-toggle" data-toggle="dropdown" href="#">
                            <i class="icon-user icon-white"></i>
                            <%= prettyName %> <b class="caret"></b>
                        </a>
                        <ul class="dropdown-menu">
                            <%

                                UrlBuilder changePasswordUrlBuilder =
                                        new UrlBuilder(request.getLocale(), loginPage, true);
                                changePasswordUrlBuilder.addParameter("returnUrl", actionPath);
                                changePasswordUrlBuilder.addParameter("cancelReturnUrl", actionPath);
                                changePasswordUrlBuilder.addParameter("changePassword");
                                String changePasswordUrl = Util.getAbsoluteUrl(changePasswordUrlBuilder.toString());
                            %>
                            <li>
                                <a href="<%= changePasswordUrl %>">
                                    <fmt:message key="change.password" />
                                </a>
                            </li>
                            <%
                                if(SecurityLogic.isAdministrator(request)) {
                                    UrlBuilder urlBuilder = new UrlBuilder(request.getLocale(), AdminAction.class, true);
                                    String adminUrl = Util.getAbsoluteUrl(urlBuilder.toString());
                                    %>
                            <li>
                                <a href="<%= adminUrl %>">
                                    <fmt:message key="administration" />
                                </a>
                            </li>
                            <%
                                }
                                UrlBuilder logoutUrlBuilder =
                                        new UrlBuilder(request.getLocale(), loginPage, true);
                                logoutUrlBuilder.addParameter("returnUrl", actionPath);
                                logoutUrlBuilder.addParameter("cancelReturnUrl", actionPath);
                                logoutUrlBuilder.addParameter("logout");
                                String logoutUrl = Util.getAbsoluteUrl(logoutUrlBuilder.toString());
                            %>
                            <li>
                                <a href="<%= logoutUrl %>">
                                    <fmt:message key="log.out" />
                                </a>
                            </li>
                            <%
                                if(request.getAttribute("actionBean") instanceof PageAction) {
                                    PageAction pageAction = (PageAction) request.getAttribute("actionBean");
                                    if(pageAction.getPageInstance() != null &&
                                       SecurityLogic.hasPermissions(
                                               portofinoConfiguration, pageAction.getPageInstance(),
                                               subject, AccessLevel.EDIT)) {%>
                            <li class="divider"></li>
                            <li>
                                <a href="javascript:portofino.enablePageActionDragAndDrop($(this), '${actionBean.context.actionPath}');">
                                    <i class="icon-file"></i> Edit layout
                                </a>
                            </li>
                            <li>
                                <%
                                    UrlBuilder urlBuilder = new UrlBuilder(request.getLocale(), PageAdminAction.class, true);
                                    urlBuilder.addParameter("originalPath", pageAction.getContext().getactionPath());
                                    urlBuilder.setEvent("pageChildren");
                                %>
                                <a href="<%= request.getContextPath() + urlBuilder %>">
                                    <i class="icon-folder-open"></i> Page children
                                </a>
                            </li>
                            <li>
                                <%
                                    urlBuilder = new UrlBuilder(request.getLocale(), PageAdminAction.class, true);
                                    urlBuilder.addParameter("originalPath", pageAction.getContext().getactionPath());
                                    urlBuilder.setEvent("newPage");
                                %>
                                <a href="<%= request.getContextPath() + urlBuilder %>">
                                    <i class="icon-plus"></i> Add new page
                                </a>
                            </li>
                            <%
                                String jsArgs = "('" +
                                        pageAction.getContext().getactionPath() + "', '" +
                                        request.getContextPath() + "');";

                            %>
                            <li>
                                <a href="javascript:portofino.confirmDeletePage<%= jsArgs %>">
                                    <i class="icon-minus"></i> Delete page
                                </a>
                            </li>
                            <li>
                                <a href="javascript:portofino.showCopyPageDialog<%= jsArgs %>">
                                    <i class="icon-file"></i> Copy page
                                </a>
                            </li>
                            <li>
                                <a href="javascript:portofino.showMovePageDialog<%= jsArgs %>">
                                    <i class="icon-share"></i> Move page
                                </a>
                            </li>
                            <%
                                if(SecurityLogic.hasPermissions(
                                        portofinoConfiguration, pageAction.getPageInstance(),
                                        subject, AccessLevel.DEVELOP)) {
                                    urlBuilder = new UrlBuilder(Locale.getDefault(), PageAdminAction.class, true);
                                    urlBuilder.addParameter("originalPath", pageAction.getContext().getactionPath());
                                    urlBuilder.setEvent("pagePermissions");
                            %>
                            <li>
                                <a href="<%= request.getContextPath() + urlBuilder %>">
                                    <i class="icon-user"></i> Page permissions
                                </a>
                            </li>
                            <% }}} %>
                        </ul>
                    </li>
                    <% } else {
                        UrlBuilder loginUrlBuilder =
                                new UrlBuilder(request.getLocale(), loginPage, false);
                        loginUrlBuilder.addParameter("returnUrl", actionPath);
                        loginUrlBuilder.addParameter("cancelReturnUrl", actionPath);
                        String loginUrl = Util.getAbsoluteUrl(loginUrlBuilder.toString());
                        %>
                        <li>
                            <a href="<%= loginUrl %>">
                                <fmt:message key="log.in" />
                            </a>
                        </li>
                    <% } %>
                </ul>
            </div>
        </div>
    </div>
</div>
