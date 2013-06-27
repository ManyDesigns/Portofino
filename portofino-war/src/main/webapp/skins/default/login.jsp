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

            body {
                padding-top: 40px;
                padding-bottom: 40px;
                background-color: #f5f5f5;
            }

            #content-login {
                padding: 19px 29px 29px;
                margin: 0 auto 20px;
                background-color: #ffffff;
                border: 1px solid #e5e5e5;
                max-width: 400px;
                -webkit-border-radius: 5px;
                   -moz-border-radius: 5px;
                        border-radius: 5px;
                -webkit-box-shadow: 0 1px 2px rgba(0,0,0,.05);
                   -moz-box-shadow: 0 1px 2px rgba(0,0,0,.05);
                        box-shadow: 0 1px 2px rgba(0,0,0,.05);
            }

            #content-login input[type="text"],
            #content-login input[type="password"] {
                font-size: 16px;
                height: auto;
                margin-bottom: 15px;
                padding: 7px 9px;
            }
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
                            '<button id="openid_submit" type="submit" class="btn">' +
                            openid.signin_text + '/button>';
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
        <title><fmt:message key="skins.default.login.login_to"/> <c:out value="${app.name}"/></title>
    </head>
    <body>
    <div class="container">
        <stripes:layout-component name="loginForm" />
    </div>
 </body>
</html>
</stripes:layout-definition>