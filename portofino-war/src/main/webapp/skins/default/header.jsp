<%@ page import="com.manydesigns.portofino.logic.SecurityLogic" %>
<%@ page contentType="text/html;charset=ISO-8859-1" language="java" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<%@ taglib prefix="mde" uri="/manydesigns-elements"%>
<stripes:url var="profileUrl" value="/actions/profile"/>
<jsp:useBean id="portofinoConfiguration" scope="application"
             type="org.apache.commons.configuration.Configuration"/>
<jsp:useBean id="model" scope="request"
             type="com.manydesigns.portofino.model.Model"/>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.AbstractActionBean"/>
<fmt:setLocale value="${pageContext.request.locale}"/>

<div id="globalLinks">
    <c:if test="${mde:getBoolean(portofinoConfiguration, 'user.enabled')}">
        <c:if test="${not empty userId}">
            <stripes:link href="/actions/profile"><c:out
                    value="${userName}"/></stripes:link> -
            <stripes:link
                    href="/actions/user/settings"><fmt:message key="skins.default.header.settings" /></stripes:link> -
            <stripes:link
                    href="/actions/user/help"><fmt:message key="skins.default.header.help" /></stripes:link> -
            <% if(SecurityLogic.isAdministrator(request)) { %>
                <stripes:link beanclass="com.manydesigns.portofino.actions.admin.SettingsAction"><fmt:message key="skins.default.header.administration"/></stripes:link> -
            <% } %>
            <stripes:link beanclass="com.manydesigns.portofino.actions.user.LoginAction">
                <stripes:param name="logout"/>
                <fmt:message key="skins.default.header.log_out"/>
            </stripes:link>
        </c:if><c:if test="${empty userId}">
        <stripes:link href="/actions/user/help"><fmt:message key="skins.default.header.help" /></stripes:link> -
        <stripes:link beanclass="com.manydesigns.portofino.actions.user.LoginAction">
            <stripes:param name="returnUrl" value="${actionBean.originalPath}"/>
            <fmt:message key="skins.default.header.log_in"/>
        </stripes:link>
    </c:if>
    </c:if><c:if
        test="${not mde:getBoolean(portofinoConfiguration, 'user.enabled')}">
        <stripes:link beanclass="com.manydesigns.portofino.actions.admin.SettingsAction"><fmt:message key="skins.default.header.administration"/></stripes:link> -
        <stripes:link href="/actions/user/help"><fmt:message key="skins.default.header.help"/></stripes:link>
    </c:if>
</div>
<div style="position: absolute; left: 20em;">
    <mde:sessionMessages/>
</div>
<h1><stripes:link href="/"><c:out
        value="${model.rootPage.title}"/></stripes:link></h1>
