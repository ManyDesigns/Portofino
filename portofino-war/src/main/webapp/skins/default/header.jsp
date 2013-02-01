<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%@ page import="com.manydesigns.portofino.logic.SecurityLogic" %>
<%@ page import="com.manydesigns.portofino.shiro.ShiroUtils" %>
<%@ page import="net.sourceforge.stripes.util.UrlBuilder" %>
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
        <%
            String loginLink = ShiroUtils.getLoginLink(
                    app, request.getContextPath(), actionBean.getOriginalPath(), actionBean.getOriginalPath());
            String logoutLink = ShiroUtils.getLogoutLink(app, request.getContextPath());
            pageContext.setAttribute("loginLink", new UrlBuilder(request.getLocale(), loginLink, true).toString());
            pageContext.setAttribute("logoutLink", new UrlBuilder(request.getLocale(), logoutLink, true).toString());
        %>
        <c:if test="<%= SecurityUtils.getSubject().isAuthenticated() %>">
            <c:out value="<%= ShiroUtils.getPrimaryPrincipal(SecurityUtils.getSubject()) %>"/> -
            <% if(SecurityLogic.isAdministrator(request)) { %>
                <stripes:link beanclass="com.manydesigns.portofino.actions.admin.SettingsAction"><fmt:message key="skins.default.header.administration"/></stripes:link> -
            <% } %>
            <a href="${logoutLink}"><fmt:message key="skins.default.header.log_out"/></a>
        </c:if><c:if test="<%= !SecurityUtils.getSubject().isAuthenticated() %>">
            <a href="${loginLink}"><fmt:message key="skins.default.header.log_in"/></a>
        </c:if>
    </div>
    <h1><stripes:link href="/"><c:out value="${app.name}"/></stripes:link></h1>
</div>
