<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"
%><%@ page import="com.manydesigns.portofino.PortofinoProperties"
%><%@ page import="org.apache.shiro.SecurityUtils"
%><%@ page import="org.apache.shiro.subject.Subject"
%>
<%@ taglib prefix="shiro" uri="http://shiro.apache.org/tags"
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
            <button type="button" class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <stripes:link href="/" class="brand">
                <c:out value="<%= portofinoConfiguration.getString(PortofinoProperties.APP_NAME) %>"/>
            </stripes:link>
            <div id="header-menu" class="nav-collapse collapse pull-right">
                <div class="navbar-text pull-right">

                <c:if test="${not empty actionBean.pageInstance}">
                    <form id="pageAdminForm" action="${pageContext.request.contextPath}/actions/admin/page">
                        <input type="hidden" name="originalPath" value="${actionBean.context.actionPath}" />
                    </form>
                </c:if>
                <c:set var="actionPath" value="${actionBean.context.actionPath}"/>
                <ul id="app-menu" class="nav">
                    <shiro:authenticated>
                        <%
                            Subject subject = SecurityUtils.getSubject();
                            Object principal = subject.getPrincipal();
                            pageContext.setAttribute("principal", principal);
                        %>
                        <shiro:hasRole name="administrators">
                            <li >
                                <stripes:link beanclass="com.manydesigns.portofino.actions.admin.AdminAction" class="navbar-link">
                                    <fmt:message key="administration" />
                                </stripes:link>
                            </li>
                            <li class="dropdown">
                                <a class="dropdown-toggle" data-toggle="dropdown" href="#">Page <b class="caret"></b></a>
                                <ul class="dropdown-menu">
                                    <li>
                                        <a href="javascript:portofino.enablePageActionDragAndDrop($(this), '${actionBean.context.actionPath}');">
                                            <i class="icon-file"></i> Edit layout
                                        </a>
                                    </li>
                                    <li>
                                        <stripes:link beanclass="com.manydesigns.portofino.actions.admin.page.PageAdminAction" event="pageChildren">
                                            <stripes:param name="originalPath" value="${actionBean.context.actionPath}"/>
                                            <i class="icon-folder-open"></i> Page children
                                        </stripes:link>
                                    </li>
                                    <li>
                                        <stripes:link beanclass="com.manydesigns.portofino.actions.admin.page.PageAdminAction" event="newPage">
                                            <stripes:param name="originalPath" value="${actionBean.context.actionPath}"/>
                                            <i class="icon-plus"></i> Add new page
                                        </stripes:link>
                                    </li>
                                    <li>
                                        <a href="javascript:portofino.confirmDeletePage('<c:out value="${actionBean.context.actionPath}"/>','<c:out value="${pageContext.request.contextPath}"/>');">
                                            <i class="icon-minus"></i> Delete page
                                        </a>
                                    </li>
                                    <li>
                                        <a href="javascript:portofino.showCopyPageDialog('<c:out value="${actionBean.context.actionPath}"/>','<c:out value="${pageContext.request.contextPath}"/>');">
                                            <i class="icon-file"></i> Copy page
                                        </a>
                                    </li>
                                    <li>
                                        <a href="javascript:portofino.showMovePageDialog('<c:out value="${actionBean.context.actionPath}"/>','<c:out value="${pageContext.request.contextPath}"/>');">
                                            <i class="icon-share"></i> Move page
                                        </a>
                                    </li>
                                    <li>
                                        <stripes:link beanclass="com.manydesigns.portofino.actions.admin.page.PageAdminAction" event="pagePermissions">
                                            <stripes:param name="originalPath" value="${actionBean.context.actionPath}"/>
                                            <i class="icon-user"></i> Page permissions
                                        </stripes:link>
                                    </li>
                                </ul>
                            </li>
                        </shiro:hasRole>
                        <li class="dropdown">
                            <a class="dropdown-toggle" data-toggle="dropdown" href="#">
                                <i class="icon-user icon-white"></i>
                                <c:out value="${principal.first_name} ${principal.last_name}"/> <b class="caret"></b>
                            </a>
                            <ul class="dropdown-menu">
                                <li>
                                    <stripes:link href="/profile">
                                        My profile
                                    </stripes:link>
                                </li>
                                <li>
                                    <stripes:link href="/actions/user/login">
                                        <stripes:param name="changePassword"/>
                                        <stripes:param name="returnUrl" value="${actionPath}"/>
                                        <stripes:param name="cancelReturnUrl" value="${actionPath}"/>
                                        <fmt:message key="change.password" />
                                    </stripes:link>
                                </li>
                                <li>
                                    <stripes:link href="/login">
<%--                                    <stripes:link href="/actions/user/login">--%>
                                        <stripes:param name="logout"/>
                                        <stripes:param name="returnUrl" value="${actionPath}"/>
                                        <stripes:param name="cancelReturnUrl" value="${actionPath}"/>
                                        <fmt:message key="log.out" />
                                    </stripes:link>
                                </li>
                            </ul>
                        </li>
                    </shiro:authenticated>
                    <shiro:notAuthenticated>
                        <li>
                            <stripes:link href="/login">
<%--                            <stripes:link href="/actions/user/login">--%>
                                <stripes:param name="returnUrl" value="${actionPath}"/>
                                <stripes:param name="cancelReturnUrl" value="${actionPath}"/>
                                <fmt:message key="log.in" />
                            </stripes:link>
                        </li>
                    </shiro:notAuthenticated>
                </ul>
                </div>
            </div>
        </div>
    </div>
</div>
