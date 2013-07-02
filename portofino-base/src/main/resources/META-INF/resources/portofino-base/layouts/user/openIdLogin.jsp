<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<stripes:layout-render name="/skins/${skin}/login.jsp">
    <stripes:layout-component name="customScripts">
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
    </stripes:layout-component>

    <stripes:layout-component name="loginForm">
        <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.user.LoginAction"/>
        <div class="row">
            <div id="content-login" class="container">
                <div class="portletHeader">
                    <h4><fmt:message key="skins.default.login.login_to"/>: <c:out value="${actionBean.applicationName}"/></h4>
                </div>
                <div class="portletBody">
                    <stripes:layout-component name="portletBody">
                        <mde:sessionMessages/>
                        <stripes:form action="<%= actionBean.getOriginalPath() %>" method="post">
                            <input type="hidden" name="cancelReturnUrl" value="${actionBean.cancelReturnUrl}" />
                            <input type="text" name="userName" id="userName" class="input-block-level"
                                   placeholder="<fmt:message key='skins.default.login.username'/>" />
                            <input type="password" name="pwd" id="pwd" class="input-block-level"
                                   placeholder="<fmt:message key='skins.default.login.password'/>" />
                            <div style="text-align: center">
                                <button type="submit" name="login" class="btn btn-large btn-primary">Login</button>
                                <button type="submit" name="cancel" class="btn btn-large">Cancel</button>
                            </div>
                            <stripes:hidden name="returnUrl"/>
                        </stripes:form>
                    </stripes:layout-component>
                </div>
                <div style="border-top: 1px solid #ddd; padding-top: 0.5em">
                    <jsp:useBean id="portofinoConfiguration" scope="application"
                                 type="org.apache.commons.configuration.Configuration"/>
                    Powered by <a href="http://www.manydesigns.com/">Portofino</a>
                    <c:out value="${mde:getString(portofinoConfiguration, 'portofino.version')}"/>
                </div>
            </div>
        </div>
        <script type="text/javascript">
            $('#userName').focus();
        </script>
    </stripes:layout-component>
</stripes:layout-render>