<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<stripes:layout-render name="/m/theme/templates/dialog/modal.jsp">
    <stripes:layout-component name="customScripts">
        <!-- Simple OpenID Selector -->
        <link type="text/css" rel="stylesheet" href="<stripes:url value="/m/pageactions/openid-selector/css/openid.css" />" />
        <style type="text/css">
            #openid_form { width: auto; }
            #openid_username { margin-right: .5em; }
            div#openid_highlight { padding: 0; }
        </style>
        <script type="text/javascript" src="<stripes:url value="/m/pageactions/openid-selector/js/openid-jquery.js" />"></script>
        <script type="text/javascript" src="<stripes:url value="/m/pageactions/openid-custom.js"/>"></script>
        <script type="text/javascript">
            $(document).ready(function() {
                openid.img_path = '${pageContext.request.contextPath}/m/pageactions/openid-selector/images/';
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
                            openid.signin_text + '</button>';
                    input_area.empty();
                    input_area.append(html);
                    $('#' + id).focus();
                };
                openid.init('openid_identifier');
            });
        </script>
        <!-- /Simple OpenID Selector -->
    </stripes:layout-component>
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.login.OpenIdLoginAction"/>
    <stripes:layout-component name="portletTitle">
        <fmt:message key="skins.default.login.login_to">
            <fmt:param value="${actionBean.applicationName}" />
        </fmt:message>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <stripes:form action="${actionBean.dispatch.absoluteOriginalPath}" method="post"
                      id="openid_form">
            <stripes:hidden name="returnUrl"/>
            <input type="hidden" name="cancelReturnUrl" value="${actionBean.cancelReturnUrl}" />
            <input type="hidden" name="showOpenIDForm" value="do" />
            Please choose your OpenID provider below:
            <div id="openid_choice">
                <div id="openid_btns"></div>
            </div>
            <div id="openid_input_area">
                <input id="openid_identifier" name="openIdUrl" type="text" value="http://" />
                <button id="openid_submit" type="submit" class="btn">
                    Sign in
                </button>
            </div>
        </stripes:form>
        <script type="text/javascript">
            $('#userName').focus();
        </script>
    </stripes:layout-component>
</stripes:layout-render>