<%
    // Avoid caching of dynamic pages
    response.setHeader("Pragma", "no-cache");
    response.addHeader("Cache-Control", "must-revalidate");
    response.addHeader("Cache-Control", "no-cache");
    response.addHeader("Cache-Control", "no-store");
    response.setDateHeader("Expires", 0);
%>
<%@ page contentType="text/html;charset=ISO-8859-1" language="java" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<%@ taglib prefix="mde" uri="/manydesigns-elements"%>
<stripes:layout-definition><%--
--%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en">
    <head>
        <jsp:include page="head.jsp"/>
        <stripes:layout-component name="customScripts"/>
        <jsp:useBean id="portofinoConfiguration" scope="application"
                     type="org.apache.commons.configuration.Configuration"/>
        <c:set var="applicationName" value="${mde:getString(portofinoConfiguration, 'application.name')}"/>
        <title>Login to <c:out value="${applicationName}"/></title>
    </head>
    <body>
    <div id="doc3">
        <div id="hd">
            <div style="position: absolute; left: 20em;">
                <mde:sessionMessages/>
            </div>
        </div>
        <div id="bd">
        <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.user.LoginAction"/>
        <stripes:form beanclass="com.manydesigns.portofino.actions.user.LoginAction" method="post">
            <div id="content-login">
                <div class="contentBody">
                    <div class="portletWrapper">
                        <div class="portlet">
                            <div class="portletHeader">
                                <div class="yui-g">
                                    <div class="portletTitle">
                                        <h1>Login to: <c:out value="${applicationName}"/></h1>
                                    </div>
                                    <div class="portletHeaderButtons">
                                        <stripes:layout-component name="portletHeaderButtons"/>
                                    </div>
                                </div>
                            </div>
                            <div class="portletBody">
                                <stripes:layout-component name="portletBody">
                                    <table class="mde-form-table loginTable">
                                        <tbody>
                                        <tr>
                                            <th><label class="mde-field-label" for="userName">User name:</label></th>
                                            <td><stripes:text name="userName" id="userName" class="mde-text-field" /></td>
                                        </tr>
                                        <tr>
                                            <th><label class="mde-field-label" for="pwd">Password:</label></th>
                                            <td><input type="password" name="pwd" id="pwd" class="mde-text-field" /></td>
                                        </tr>
                                        <tr>
                                            <th></th>
                                            <td>
                                                <stripes:submit name="login" value="Login" class="portletButton"/>
                                                <stripes:submit name="cancel" value="Cancel" class="portletButton"/>
                                            </td>
                                        </tr>
                                        <c:if test="recoverPwd">
                                            <tr>
                                                <td colspan="2">
                                                    Hai dimenticato la password? <a href="PwdRecovery.action">recupera password</a>
                                                </td>
                                            </tr>
                                        </c:if>
                                        </tbody>
                                    </table>
                                </stripes:layout-component>
                            </div>
                            <div class="portletFooter" style="border-top: 1px solid #ddd; padding-top: 0.5em">
                                Powered by <a href="http://www.manydesigns.com/">ManyDesigns Portofino</a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <script type="text/javascript">
                $('#userName').focus();
            </script>
            <stripes:hidden name="returnUrl"/>
        </stripes:form>
        </div>
    </div>
 </body>
</html>
</stripes:layout-definition>