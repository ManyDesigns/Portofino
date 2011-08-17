<%@ page contentType="text/html;charset=ISO-8859-1" language="java" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<%@ taglib prefix="mde" uri="/manydesigns-elements"%>
<stripes:url var="profileUrl" value="/Profile.action"/>
<jsp:useBean id="portofinoConfiguration" scope="application"
             type="org.apache.commons.configuration.Configuration"/>
<jsp:useBean id="model" scope="request"
             type="com.manydesigns.portofino.model.Model"/>
<div id="globalLinks">
    <c:if test="${mde:getBoolean(portofinoConfiguration, 'user.enabled')}">
        <c:if test="${not empty userId}">
            <stripes:link href="/Profile.action"><c:out
                    value="${userName}"/></stripes:link> -
            <stripes:link
                    href="/user/Settings.action">Settings</stripes:link> -
            <stripes:link
                    href="/user/Help.action">Help</stripes:link> -
            <stripes:link
                    href="/admin/settings.action">Administration</stripes:link> -
            <stripes:link
                    href="/user/login.action?logout=">Log out</stripes:link>
        </c:if><c:if test="${empty userId}">
        <stripes:link href="/user/Help.action">Help</stripes:link> -
        <stripes:link
                href="/admin/settings.action">Administration</stripes:link> -
        <stripes:link
        href="/user/login.action">Log in</stripes:link>
    </c:if>
    </c:if><c:if
        test="${not mde:getBoolean(portofinoConfiguration, 'user.enabled')}">
    <stripes:link
            href="/admin/settings.action">Administration</stripes:link> -
    <stripes:link href="/user/Help.action">Help</stripes:link>
</c:if>
</div>
<div style="position: absolute; left: 20em;">
    <mde:sessionMessages/>
</div>
<h1><stripes:link href="/"><c:out
        value="${model.rootPage.title}"/></stripes:link></h1>
