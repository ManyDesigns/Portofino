<%@ page import="com.manydesigns.portofino.PortofinoProperties"
%><%@ page import="com.manydesigns.portofino.menu.*"
%><%@ page import="com.manydesigns.portofino.modules.AdminModule"
%><%@ page import="com.manydesigns.portofino.modules.ModuleRegistry"
%><%@ page import="net.sourceforge.stripes.controller.ActionResolver"
%><%@ page import="org.apache.commons.lang.StringUtils"
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
%>
<%@ page import="java.io.Serializable" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%>
<%@ taglib prefix="shiro" uri="http://shiro.apache.org/tags" %>
<stripes:layout-definition><%--
--%><!doctype html>
    <html lang="<%= request.getLocale() %>">
    <jsp:include page="/theme/head.jsp">
        <jsp:param name="pageTitle" value="${pageTitle}" />
    </jsp:include>
    <body>
    <div id="wrapper">
    <jsp:useBean id="portofinoConfiguration" scope="application" type="org.apache.commons.configuration.Configuration"/>
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
                <img src="${ portofinoConfiguration.getString(PortofinoProperties.APP_LOGO) }" width=32px />
                <c:out value="<%= portofinoConfiguration.getString(PortofinoProperties.APP_NAME) %>"/>
            </stripes:link>
        </div>

            <nav id="header-menu" class="navbar-collapse collapse" role="navigation">
            <ul class="nav navbar-nav navbar-right">
                <%
                    String loginPage = portofinoConfiguration.getString(PortofinoProperties.LOGIN_PAGE);
                    String actionPath = (String) request.getAttribute(ActionResolver.RESOLVED_ACTION);
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
                            UrlBuilder changePasswordUrlBuilder = new UrlBuilder(request.getLocale(), loginPage, true);
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
                            UrlBuilder logoutUrlBuilder = new UrlBuilder(request.getLocale(), loginPage, true);
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
                    </ul>
                </li>
                </shiro:user>
            </ul>
        </nav>

    </div>
    </header>
    <div id="content" class="container">
        <div class="row">
            <div class="col-md-2">
                <div class="navigation">
                    <ul class="nav nav-list portofino-sidenav">
                        <%
                            //Admin menu
                            MenuBuilder adminMenuBuilder =
                                    (MenuBuilder) application.getAttribute(AdminModule.ADMIN_MENU);
                            Menu adminMenu = adminMenuBuilder.build();
                            for(MenuItem item : adminMenu.items) {
                                if(item instanceof MenuGroup) {
                                    %><li class="nav-header"><c:out value="<%= item.label %>"/></li><%
                                    for(MenuLink link : ((MenuGroup) item).menuLinks) {
                                        String adminLinkClass = getLinkClass(link, request);
                                        %>
                                        <li class="<%= adminLinkClass %>">
                                            <stripes:link href='<%= StringUtils.defaultString(link.link, "#") %>'>
                                                <c:out value="<%= link.label %>"/>
                                            </stripes:link>
                                        </li>
                                        <%
                                    }
                                } else {
                                    MenuLink link = (MenuLink) item;
                                    String adminLinkClass = getLinkClass(link, request);
                                    %>
                                    <li class="<%= adminLinkClass %>">
                                        <stripes:link href='<%= StringUtils.defaultString(link.link, "#") %>'>
                                            <c:out value="<%= link.label %>"/>
                                        </stripes:link>
                                    </li>
                                    <%
                                }
                            }
                        %>
                    </ul>
                </div>
            </div>
            <div class="content col-md-10">
                <div class="row">
                    <mde:sessionMessages/>
                    <stripes:layout-component name="pageHeader">
                        <h1 style="border-bottom: 1px solid #E5E5E5">
                            <stripes:layout-component name="pageTitle" />
                        </h1>
                    </stripes:layout-component>
                    <stripes:layout-component name="pageBody" />
                </div>
            </div>
        </div>
    </div>
    <footer>
        <div class="container">
            Powered by <a href="http://portofino.manydesigns.com/">Portofino</a>
            <c:out value="<%= ModuleRegistry.getPortofinoVersion() %>"/>
        </div>
    </footer>
        </div>
    </body>
    </html>
</stripes:layout-definition>
<%!
    protected String getLinkClass(MenuLink link, HttpServletRequest request) {
        String adminLinkClass = "";
        String actionPath = (String) request.getAttribute(ActionResolver.RESOLVED_ACTION);
        if(!"/".equals(link.link) && actionPath.startsWith(link.link)) {
            adminLinkClass = "active";
        }
        return adminLinkClass;
    }
%>