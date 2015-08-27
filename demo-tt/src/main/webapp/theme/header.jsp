<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"
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
%><%@ page import="org.apache.commons.lang.StringUtils"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib prefix="shiro" uri="http://shiro.apache.org/tags"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"%>
<jsp:useBean id="portofinoConfiguration" scope="application"
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
            <c:set var="actionPath" value="${actionBean.context.actionPath}"/>
            <ul class="nav navbar-nav navbar-right">
                <shiro:user>
                    <%
                        Subject subject = SecurityUtils.getSubject();
                        Object principal = subject.getPrincipal();
                        String prettyName = ShiroUtils.getPortofinoRealm().getUserPrettyName((Serializable) principal);
                    %>
                    <shiro:hasRole name="administrators">
                        <li >
                            <stripes:link beanclass="com.manydesigns.portofino.actions.admin.AdminAction" class="navbar-link">
                                <fmt:message key="administration" />
                            </stripes:link>
                        </li>
                        <li class="dropdown">
                            <a class="dropdown-toggle" data-toggle="dropdown" href="#">Page <strong class="caret"></strong></a>
                            <ul class="dropdown-menu">
                                <li>
                                    <a href="javascript:portofino.enablePageActionDragAndDrop($(this), '${actionBean.context.actionPath}');">
                                        <em class="glyphicon glyphicon-file"></em> Edit layout
                                    </a>
                                </li>
                                <li>
                                    <stripes:link beanclass="com.manydesigns.portofino.actions.admin.page.PageAdminAction" event="pageChildren">
                                        <stripes:param name="originalPath" value="${actionBean.context.actionPath}"/>
                                        <em class="glyphicon glyphicon-folder-open"></em> Page children
                                    </stripes:link>
                                </li>
                                <li>
                                    <stripes:link beanclass="com.manydesigns.portofino.actions.admin.page.PageAdminAction" event="newPage">
                                        <stripes:param name="originalPath" value="${actionBean.context.actionPath}"/>
                                        <em class="glyphicon-plus"></em> Add new page
                                    </stripes:link>
                                </li>
                                <li>
                                    <a href="javascript:portofino.confirmDeletePage('<c:out value="${actionBean.context.actionPath}"/>','<c:out value="${pageContext.request.contextPath}"/>');">
                                        <em class="glyphicon glyphicon-minus"></em> Delete page
                                    </a>
                                </li>
                                <li>
                                    <a href="javascript:portofino.showCopyPageDialog('<c:out value="${actionBean.context.actionPath}"/>','<c:out value="${pageContext.request.contextPath}"/>');">
                                        <em class="glyphicon glyphicon-file"></em> Copy page
                                    </a>
                                </li>
                                <li>
                                    <a href="javascript:portofino.showMovePageDialog('<c:out value="${actionBean.context.actionPath}"/>','<c:out value="${pageContext.request.contextPath}"/>');">
                                        <em class="glyphicon glyphicon-share"></em> Move page
                                    </a>
                                </li>
                                <li>
                                    <stripes:link beanclass="com.manydesigns.portofino.actions.admin.page.PageAdminAction" event="pagePermissions">
                                        <stripes:param name="originalPath" value="${actionBean.context.actionPath}"/>
                                        <em class="glyphicon glyphicon-user"></em> Page permissions
                                    </stripes:link>
                                </li>
                            </ul>
                        </li>
                    </shiro:hasRole>
                    <li class="dropdown">
                        <a class="dropdown-toggle" data-toggle="dropdown" href="#">
                            <em class="glyphicon glyphicon-user"></em>
                            <%= prettyName %> <strong class="caret"></strong>
                        </a>
                        <ul class="dropdown-menu">
                            <li>
                                <stripes:link href="/profile">
                                    My profile
                                </stripes:link>
                            </li>
                            <li>
                                <stripes:link href="/login">
                                    <stripes:param name="logout"/>
                                    <stripes:param name="returnUrl" value="${actionPath}"/>
                                    <stripes:param name="cancelReturnUrl" value="${actionPath}"/>
                                    <fmt:message key="log.out" />
                                </stripes:link>
                            </li>
                        </ul>
                    </li>
                </shiro:user>
                <shiro:guest>
                    <li>
                        <stripes:link href="/login">
                            <stripes:param name="returnUrl" value="${actionPath}"/>
                            <stripes:param name="cancelReturnUrl" value="${actionPath}"/>
                            <fmt:message key="log.in" />
                        </stripes:link>
                    </li>
                </shiro:guest>
            </ul>
        </nav>
    </div>
</header>
