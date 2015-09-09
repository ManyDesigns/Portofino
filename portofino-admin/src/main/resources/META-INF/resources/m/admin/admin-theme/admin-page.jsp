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
        <jsp:include page="/theme/header.jsp">
            <jsp:param name="pageTitle" value="${pageTitle}" />
        </jsp:include>

        <div id="content" class="container">
            <div class="row">
                <div class="col-md-offset-1 col-md-12">
                    <div class="dropdown mobile">
                        <button class="btn btn-default dropdown-toggle" type="button" id="dropdownMenu1" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">
                            Menu
                            <span class="caret"></span>
                        </button>
                        <ul class="dropdown-menu" aria-labelledby="dropdownMenu3">
                            <%
                                //Admin menu
                                MenuBuilder adminMenuBuilder =(MenuBuilder) application.getAttribute(AdminModule.ADMIN_MENU);
                                Menu adminMenu = adminMenuBuilder.build();
                                for(MenuItem item : adminMenu.items) {
                                    if(item instanceof MenuGroup) {
                            %><li class="dropdown-header"><c:out value="<%= item.label %>"/></li><%
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
            </div>

            <div class="row">
                <div class="col-md-2">
                    <div class="navigation">
                        <ul class="nav nav-list portofino-sidenav">
                            <%
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