<%
    // Avoid caching of dynamic pages
    response.setHeader("Pragma", "no-cache");
    response.addHeader("Cache-Control", "must-revalidate");
    response.addHeader("Cache-Control", "no-cache");
    response.addHeader("Cache-Control", "no-store");
    response.setDateHeader("Expires", 0);
%><%@ page contentType="text/html;charset=ISO-8859-1" language="java"
           pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html;charset=ISO-8859-1"/>
    <meta http-equiv="Content-Script-Type" content="text/javascript"/>
    <meta http-equiv="Content-Style-Type" content="text/css"/>
    <link rel="stylesheet" type="text/css"
          href="<s:url value="/yui-2.8.1/build/reset-fonts-grids/reset-fonts-grids.css"/>"/>
    <link rel="stylesheet" type="text/css"
          href="<s:url value="/yui-2.8.1/build/base/base-min.css"/>"/>
    <link rel="stylesheet" type="text/css"
          href="<s:url value="/jquery-ui-1.8.5/css/smoothness/jquery-ui-1.8.5.custom.css"/>"/>
    <link rel="stylesheet" type="text/css"
          href="<s:url value="/skins/default/portofino.css"/>"/>
    <script type="text/javascript"
            src="<s:url value="/yui-2.8.1/build/yuiloader-dom-event/yuiloader-dom-event.js"/>"></script>
    <script type="text/javascript"
            src="<s:url value="/jquery-ui-1.8.5/js/jquery-1.4.2.min.js"/>"></script>
    <script type="text/javascript"
            src="<s:url value="/jquery-ui-1.8.5/js/jquery-ui-1.8.5.custom.min.js"/>"></script>
    <script type="text/javascript"
            src="<s:url value="/jquery-treetable-2.3.0/jquery.treeTable.min.js"/>"></script>
    <script type="text/javascript"
            src="<s:url value="/elements.js"/>"></script>
    <script type="text/javascript"
            src="<s:url value="/skins/default/portofino.js"/>"></script>
    <title><s:property value="#request.navigation.selectedNavigationNode.description"/></title>

</head>
<body>
<div id="doc3" class="yui-t2">
    <s:url var="indexUrl" namespace="/" action="Index"/>
    <s:url var="profileUrl" namespace="/user" action="Profile"/>
    <s:url var="settingsUrl" namespace="/user" action="Settings"/>
    <s:url var="helpUrl" namespace="/user" action="Help"/>
    <s:url var="loginUrl" namespace="/user" action="Login"/>
    <s:url var="logoutUrl" namespace="/user" action="Login" method="logout"/>


    <div id="hd">
        
        <div style="position: absolute; left: 20em;">
            <mdes:sessionMessages/>
        </div>
        <h1><s:a href="%{#indexUrl}"><s:property value="#application.portofinoProperties['application.name']"/></s:a></h1>
    </div>
    <div id="bd">
        <div id="yui-main">
            <div id="content-login">
                <s:form method="post" namespace="/user" action="Login">



                        <div id="login">
                            <h1>Login</h1>
                            <p/>
                            <table class="details">
                                <tbody>
                                    <tr>
                                        <th><label class="field" for="userName">User name:</label></th>
                                        <td><input type="text" name="userName" id="userName" class="text" ></td>
                                    </tr>
                                    <tr>
                                        <th><label class="field" for="pwd">Password:</label></th>
                                        <td><input type="password" name="pwd" id="pwd" class="text"></td>
                                    </tr>
                                    <tr>
                                        <th></th>
                                        <td><s:submit id="loginButton" method="login" value="Login" /></td>
                                    </tr>
                                    <tr>
                                        <td colspan="2"><s:if test="recoverPwd">
                            Hai dimenticato la password? <a href="PwdRecovery.action">recupera password</a></s:if>
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                            <p/>
                        </div>
                       <s:hidden name="returnUrl" value="%{returnUrl}"/>
                </s:form>
            </div>
        </div>
        

        </div>

    <div id="ft">
        <div id="responseTime">Page response time: <s:property value="#request.stopWatch.time"/> ms. DB time: <s:property value="#application.context.dbTime"/> ms.</div>
        Powered by <a href="http://www.manydesigns.com/">ManyDesigns Portofino</a>
        <s:property value="#application.portofinoProperties['portofino.version']"/>
    </div>
</div>
<script type="text/javascript">
        $('#userName').focus();
</script>
</body>
</html>