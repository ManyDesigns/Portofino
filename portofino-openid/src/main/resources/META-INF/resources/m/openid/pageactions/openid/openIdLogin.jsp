<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<stripes:layout-render name="/theme/templates/dialog/modal.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.openid.OpenIdLoginAction"/>
    <stripes:layout-component name="pageTitle">
        <fmt:message key="login.to">
            <fmt:param value="${actionBean.applicationName}" />
        </fmt:message>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <!-- Simple OpenID Selector -->
        <script type="text/javascript" src="<stripes:url value="/m/openid/openid-selector/js/openid-jquery.js" />"></script>
        <script type="text/javascript" src="<stripes:url value="/m/openid/openid-custom.js"/>"></script>
        <script type="text/javascript">
            $(document).ready(function() {
                openid.img_path = '${pageContext.request.contextPath}/m/openid/openid-selector/images/';
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
        <stripes:form action="${pageContext.request.contextPath}${actionBean.context.actionPath}" method="post"
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