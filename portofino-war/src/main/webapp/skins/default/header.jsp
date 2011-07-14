<%
    // Avoid caching of dynamic pages
    response.setHeader("Pragma", "no-cache");
    response.addHeader("Cache-Control", "must-revalidate");
    response.addHeader("Cache-Control", "no-cache");
    response.addHeader("Cache-Control", "no-store");
    response.setDateHeader("Expires", 0);
%><%@ page contentType="text/html;charset=ISO-8859-1" language="java"
           pageEncoding="ISO-8859-1"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@ taglib prefix="mdes" uri="/manydesigns-elements-struts2"
%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html;charset=ISO-8859-1"/>
    <meta http-equiv="Content-Script-Type" content="text/javascript"/>
    <meta http-equiv="Content-Style-Type" content="text/css"/>
    <link rel="stylesheet" type="text/css"
          href="<stripes:url value="/yui-2.8.1/build/reset-fonts-grids/reset-fonts-grids.css"/>"/>
    <link rel="stylesheet" type="text/css"
          href="<stripes:url value="/yui-2.8.1/build/base/base-min.css"/>"/>
    <link rel="stylesheet" type="text/css"
          href="<stripes:url value="/jquery-ui-1.8.9/css/smoothness/jquery-ui-1.8.9.custom.css"/>"/>
    <link rel="stylesheet" type="text/css"
          href="<stripes:url value="/skins/default/portofino.css"/>"/>
    <script type="text/javascript"
            src="<stripes:url value="/yui-2.8.1/build/yuiloader-dom-event/yuiloader-dom-event.js"/>"></script>
    <script type="text/javascript"
            src="<stripes:url value="/jquery-ui-1.8.9/js/jquery-1.4.4.min.js"/>"></script>
    <script type="text/javascript"
            src="<stripes:url value="/jquery-ui-1.8.9/js/jquery-ui-1.8.9.custom.min.js"/>"></script>
    <script type="text/javascript"
            src="<stripes:url value="/jquery-treetable-2.3.0/jquery.treeTable.min.js"/>"></script>
    <script type="text/javascript"
            src="<stripes:url value="/elements.js"/>"></script>
    <script type="text/javascript"
            src="<stripes:url value="/skins/default/portofino.js"/>"></script>
    <jsp:useBean id="dispatch" class="com.manydesigns.portofino.dispatcher.Dispatch" scope="request"/>
    <title>${dispatch.lastSiteNodeInstance.siteNode.description}</title>
</head>
<body>
<div id="doc3" class="yui-t2">
    <stripes:url var="indexUrl" value="/Index.action"/>
    <stripes:url var="profileUrl" value="/Profile.action"/>
    <stripes:url var="settingsUrl" value="/user/Settings.action"/>
    <stripes:url var="helpUrl" value="/user/Help.action"/>
    <stripes:url var="loginUrl" value="/user/Login.action"/>
    <stripes:url var="logoutUrl" value="/user/Login.action?logout="/>


    <div id="hd">
        <div id="globalLinks">

            <s:if test="#application.portofinoProperties['user.enabled'].equals('true') 
            && #session.get('userId') != null">
                Welcome, <s:a href="%{#profileUrl}">
                <s:property value="#session.get('userName')"/></s:a> -
            </s:if>
            <s:a href="%{#settingsUrl}">Settings</s:a> -
            <s:a href="%{#helpUrl}">Help</s:a>
            <s:if test="#application.portofinoProperties['user.enabled'].equals('true')
            && #session.get('userId') == null">
              - <s:a href="%{#loginUrl}">Log in</s:a>
            </s:if>
            <s:if test="#application.portofinoProperties['user.enabled'].equals('true')
            && #session.get('userId') != null">
                - <s:a href="%{#logoutUrl}">Log out</s:a>    
            </s:if>

        </div>
        <div style="position: absolute; left: 20em;">
            <mdes:sessionMessages/>
        </div>
        <h1><s:a href="%{#indexUrl}"><s:property value="#application.portofinoProperties['application.name']"/></s:a></h1>
    </div>
    <div id="bd">
        <div id="yui-main">
            <div id="content" class="yui-b">