<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.manydesigns.portofino.logic.SecurityLogic" %>
<%@ page import="com.manydesigns.portofino.shiro.ShiroUtils" %>
<%@ page import="org.apache.shiro.SecurityUtils" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"%>
<%@ taglib prefix="mde" uri="/manydesigns-elements"%>
<stripes:url var="profileUrl" value="/actions/profile"/>
<jsp:useBean id="portofinoConfiguration" scope="application"
             type="org.apache.commons.configuration.Configuration"/>
<jsp:useBean id="model" scope="request"
             type="com.manydesigns.portofino.model.Model"/>
<jsp:useBean id="app" scope="request"
             type="com.manydesigns.portofino.application.Application"/>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.dispatcher.AbstractActionBean"/>
<fmt:setLocale value="${pageContext.request.locale}"/>

<div id="hd-title">
    <div id="globalLinks">
        <% UserService userService = UserServiceFactory.getUserService(); %>
        <c:if test="<%= SecurityUtils.getSubject().isAuthenticated() %>">
            <c:out value="<%= ShiroUtils.getPrimaryPrincipal(SecurityUtils.getSubject()) %>"/> -
            <!--<stripes:link href="/actions/profile"><c:out
                    value="${userName}"/></stripes:link> -
            <stripes:link
                    href="/actions/user/settings"><fmt:message key="skins.default.header.settings" /></stripes:link> -
            <stripes:link
                    href="/actions/user/help"><fmt:message key="skins.default.header.help" /></stripes:link> - -->
            <% if(SecurityLogic.isAdministrator(request)) { %>
                <stripes:link beanclass="com.manydesigns.portofino.actions.admin.SettingsAction"><fmt:message key="skins.default.header.administration"/></stripes:link> -
            <% } %>
            <stripes:url var="logoutUrl" beanclass="com.manydesigns.portofino.actions.user.LoginAction" event="logout" />
            <a href="<%= userService.createLogoutURL(pageContext.getAttribute("logoutUrl").toString()) %>">
                <fmt:message key="skins.default.header.log_out"/>
            </a>
        </c:if><c:if test="<%= !SecurityUtils.getSubject().isAuthenticated() %>">
            <!-- <stripes:link href="/actions/user/help"><fmt:message key="skins.default.header.help" /></stripes:link> - -->
            <a href="<%= userService.createLoginURL(actionBean.getOriginalPath()) %>">
                <fmt:message key="skins.default.header.log_in"/>
            </a>
        </c:if>
    </div>
    <h1><stripes:link href="/"><c:out value="${app.name}"/></stripes:link></h1>
</div>
