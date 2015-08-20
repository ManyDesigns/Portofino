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
%><%@ page import="org.apache.commons.lang.StringUtils"
%><%@ page import="org.apache.shiro.SecurityUtils"
%><%@ page import="org.apache.shiro.subject.Subject"
%><%@ page import="java.io.Serializable"
%><%@ page import="java.util.Locale"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib prefix="shiro" uri="http://shiro.apache.org/tags"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><jsp:useBean id="portofinoConfiguration" scope="application"
             type="org.apache.commons.configuration.Configuration"/>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.stripes.AbstractActionBean"/>
<fmt:setLocale value="${pageContext.request.locale}"/>
<header class="navbar navbar-inverse navbar-static-top">
    <div class="container">
        <div class="navbar-header">
            <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
                <span class="sr-only">Toggle navigation</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <stripes:link href="/" class="navbar-brand">
                <% if(!StringUtils.isEmpty(portofinoConfiguration.getString(PortofinoProperties.APP_LOGO))) { %>
                <stripes:url var="logoUrl" value="<%= portofinoConfiguration.getString(PortofinoProperties.APP_LOGO) %>"/>
                <img src="${logoUrl}" width="32px" alt='<c:out value="<%= portofinoConfiguration.getString(PortofinoProperties.APP_NAME) %>"/>' />
                <% } %>
                <c:out value="<%= portofinoConfiguration.getString(PortofinoProperties.APP_NAME) %>"/>
            </stripes:link>
        </div>
        <nav id="header-menu" class="navbar-collapse collapse">
            <form id="pageAdminForm" action="${pageContext.request.contextPath}/actions/admin/page">
                <input type="hidden" name="originalPath" value="${actionBean.context.actionPath}" />
            </form>
            <ul class="nav navbar-nav navbar-right">
                <%
                    String loginPage = portofinoConfiguration.getString(PortofinoProperties.LOGIN_PAGE);
                    String actionPath = actionBean.getContext().getActionPath();
                %>
                <shiro:user>
                <%
                    Subject subject = SecurityUtils.getSubject();
                    Object principal = subject.getPrincipal();
                    String prettyName = ShiroUtils.getPortofinoRealm().getUserPrettyName((Serializable) principal);
                %>
                <li class="dropdown">
                    <a class="dropdown-toggle" data-toggle="dropdown" href="#">
                        <em class="glyphicon glyphicon-user"></em>
                        <%= prettyName %> <strong class="caret"></strong>
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
                                <em class="glyphicon glyphicon-file"></em> Edit layout
                            </a>
                        </li>
                        <li>
                            <%
                                UrlBuilder urlBuilder = new UrlBuilder(request.getLocale(), PageAdminAction.class, true);
                                urlBuilder.addParameter("originalPath", pageAction.getContext().getActionPath());
                                urlBuilder.setEvent("pageChildren");
                            %>
                            <a href="<%= request.getContextPath() + urlBuilder %>">
                                <em class="glyphicon glyphicon-folder-open"></em> Page children
                            </a>
                        </li>
                        <li>
                            <%
                                urlBuilder = new UrlBuilder(request.getLocale(), PageAdminAction.class, true);
                                urlBuilder.addParameter("originalPath", pageAction.getContext().getActionPath());
                                urlBuilder.setEvent("newPage");
                            %>
                            <a href="<%= request.getContextPath() + urlBuilder %>">
                                <em class="glyphicon glyphicon-plus"></em> Add new page
                            </a>
                        </li>
                        <%
                            String jsArgs = "('" +
                                    pageAction.getContext().getActionPath() + "', '" +
                                    request.getContextPath() + "');";

                        %>
                        <li>
                            <a href="javascript:portofino.confirmDeletePage<%= jsArgs %>">
                                <em class="glyphicon glyphicon-minus"></em> Delete page
                            </a>
                        </li>
                        <li>
                            <a href="javascript:portofino.showCopyPageDialog<%= jsArgs %>">
                                <em class="glyphicon glyphicon-file"></em> Copy page
                            </a>
                        </li>
                        <li>
                            <a href="javascript:portofino.showMovePageDialog<%= jsArgs %>">
                                <em class="glyphicon glyphicon-share"></em> Move page
                            </a>
                        </li>
                        <%
                            if(SecurityLogic.hasPermissions(
                                    portofinoConfiguration, pageAction.getPageInstance(),
                                    subject, AccessLevel.DEVELOP)) {
                                urlBuilder = new UrlBuilder(Locale.getDefault(), PageAdminAction.class, true);
                                urlBuilder.addParameter("originalPath", pageAction.getContext().getActionPath());
                                urlBuilder.setEvent("pagePermissions");
                        %>
                        <li>
                            <a href="<%= request.getContextPath() + urlBuilder %>">
                                <em class="glyphicon glyphicon-user"></em> Page permissions
                            </a>
                        </li>
                        <% }}} %>
                    </ul>
                </li>
                </shiro:user>
                <shiro:guest>
                <%
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
                </shiro:guest>
            </ul>
        </nav>
    </div>
</header>
