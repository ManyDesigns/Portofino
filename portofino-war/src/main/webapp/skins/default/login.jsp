<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><stripes:layout-definition><%--
--%><!doctype html>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en">
    <head>
        <jsp:include page="head.jsp"/>
        <stripes:layout-component name="customScripts"/>
        <!-- Simple OpenID Selector -->
        <link type="text/css" rel="stylesheet" href="<stripes:url value="/openid-selector/css/openid.css" />" />
        <style type="text/css">
            #openid_form { width: auto; }
            #openid_username { margin-right: .5em; }
            div#openid_highlight { padding: 0; }
        </style>
        <script type="text/javascript" src="<stripes:url value="/openid-selector/js/openid-jquery.js" />"></script>
        <script type="text/javascript" src="<stripes:url value="/skins/${skin}/openid-custom.js"/>"></script>
        <script type="text/javascript">
            $(document).ready(function() {
                openid.img_path = '${pageContext.request.contextPath}/openid-selector/images/';
                openid.useInputBox = function(provider) {
                    var input_area = $('#openid_input_area');
                    var html = '';
                    var id = 'openid_username';
                    var value = '';
                    var label = provider['label'];
                    var style = '';
                    if (label) {
                        html = '<p>' + label + '</p>';
                    }
                    if (provider['name'] == 'OpenID') {
                        id = this.input_id;
                        value = 'http://';
                        style = 'background: #FFF url(' + openid.img_path + 'openid-inputicon.gif) no-repeat scroll 0 50%; padding-left:18px;';
                    }
                    html += '<input id="' + id + '" type="text" style="' + style + '" name="' + id + '" value="' + value + '" />' +
                            '<button id="openid_submit" type="submit" class="ui-button ui-widget ui-state-default ui-corner-all portletButton ui-button-text-only">' +
                            '<span class="ui-button-text">' + openid.signin_text + '</span></button>';
                    input_area.empty();
                    input_area.append(html);
                    $('#' + id).focus();
                };
                openid.init('openid_identifier');
            });
        </script>
        <!-- /Simple OpenID Selector -->
        <jsp:useBean id="model" scope="request"
                     type="com.manydesigns.portofino.model.Model"/>
        <jsp:useBean id="app" scope="request"
                     type="com.manydesigns.portofino.application.Application"/>
        <c:set var="applicationName" value="${app.name}"/>
        <title><fmt:message key="skins.default.login.login_to"/> <c:out value="${applicationName}"/></title>
    </head>
    <body>
    <div id="doc3">
        <div id="hd"></div>
        <div id="bd">
        <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.user.LoginAction"/>
        <div id="content-login">
            <div class="contentBody">
                <div class="portletWrapper noSpacing">
                    <div class="portlet">
                        <div class="portletHeader">
                            <div>
                                <div class="portletTitle">
                                    <h1><fmt:message key="skins.default.login.login_to"/>: <c:out value="${applicationName}"/></h1>
                                </div>
                                <div class="portletHeaderButtons">
                                    <stripes:layout-component name="portletHeaderButtons">
                                    </stripes:layout-component>
                                </div>
                            </div>
                            <div class="portletHeaderSeparator"></div>
                        </div>
                        <div class="portletBody">
                            <stripes:layout-component name="portletBody">
                                <mde:sessionMessages/>
                                <stripes:form beanclass="com.manydesigns.portofino.actions.user.LoginAction" method="post">
                                    <input type="hidden" name="cancelReturnUrl" value="${actionBean.cancelReturnUrl}" />
                                    <table class="mde-form-table loginTable">
                                        <tbody>
                                        <tr>
                                            <th><label class="mde-field-label" for="userName"><fmt:message key="skins.default.login.username"/>:</label></th>
                                            <td><stripes:text name="userName" id="userName" class="mde-text-field" /></td>
                                        </tr>
                                        <tr>
                                            <th><label class="mde-field-label" for="pwd">Password:</label></th>
                                            <td><input type="password" name="pwd" id="pwd" class="mde-text-field" /></td>
                                        </tr>
                                        <tr>
                                            <th></th>
                                            <td>
                                                <portofino:buttons list="login-buttons" cssClass="portletButton" />
                                            </td>
                                        </tr>
                                        <!--<c:if test="recoverPwd">
                                            <tr>
                                                <td colspan="2">
                                                    <fmt:message key="skins.default.login.forgot_pwd"/> <a href="PwdRecovery.action"><fmt:message key="skins.default.login.retrieve_pwd"/></a>
                                                </td>
                                            </tr>
                                        </c:if>-->
                                        </tbody>
                                    </table>
                                    <stripes:hidden name="returnUrl"/>
                                </stripes:form>
                                <c:if test="${actionBean.openIdEnabled}">
                                    <div style="border-top: 1px solid #ddd; padding-top: 0.5em">
                                        <fmt:message key="skins.default.login.openId"/>:
                                        <stripes:form beanclass="com.manydesigns.portofino.actions.user.LoginAction" method="post"
                                                      id="openid_form">
                                            <stripes:hidden name="returnUrl"/>
                                            <input type="hidden" name="cancelReturnUrl" value="${actionBean.cancelReturnUrl}" />
                                            <input type="hidden" name="showOpenIDForm" value="do" />
                                            <div id="openid_choice">
                                                <div id="openid_btns"></div>
                                            </div>
                                            <div id="openid_input_area">
                                                <input id="openid_identifier" name="openIdUrl" type="text" value="http://" />
                                                <button id="openid_submit" type="submit"
                                                        class="ui-button ui-widget ui-state-default ui-corner-all portletButton ui-button-text-only">
                                                    <span class="ui-button-text">Sign in</span>
                                                </button>
                                            </div>
                                        </stripes:form>
                                    </div>
                                </c:if>
                                </stripes:layout-component>
                            </div>
                            <div class="portletFooter" style="border-top: 1px solid #ddd; padding-top: 0.5em">
                                <jsp:useBean id="portofinoConfiguration" scope="application"
                                             type="org.apache.commons.configuration.Configuration"/>
                                Powered by <a href="http://www.manydesigns.com/">Portofino</a>
                                <c:out value="${mde:getString(portofinoConfiguration, 'portofino.version')}"/>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <script type="text/javascript">
                $('#userName').focus();
            </script>
        </div>
    </div>
 </body>
</html>
</stripes:layout-definition>