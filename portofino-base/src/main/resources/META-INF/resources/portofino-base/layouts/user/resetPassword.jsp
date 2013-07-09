<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<stripes:layout-render name="/skins/${skin}/login.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.user.LoginAction"/>
    <stripes:layout-component name="pageTitle">
        Password reset
    </stripes:layout-component>
    <stripes:layout-component name="loginTitle">
        Password reset
    </stripes:layout-component>
    <stripes:layout-component name="loginBody">
        <stripes:form action="<%= actionBean.getOriginalPath() %>" method="post">
            <input type="hidden" name="token" id="token" value="${actionBean.token}" />
            <input type="password" name="newPassword" id="newPassword" class="input-block-level"
                   placeholder="<fmt:message key='skins.default.login.new.password'/>" />
            <input type="password" name="confirmNewPassword" id="confirmNewPassword" class="input-block-level"
                   placeholder="<fmt:message key='skins.default.login.confirm.new.password'/>" />
            <div class="login-buttons spacingTop">
                <button type="submit" id="resetPassword2" name="resetPassword2" class="btn btn-primary">Reset password</button>
            </div>
            <stripes:hidden name="returnUrl"/>
            <stripes:hidden name="cancelReturnUrl"/>
        </stripes:form>
        <script type="text/javascript">
            $('#newPassword').focus();
        </script>
    </stripes:layout-component>
</stripes:layout-render>