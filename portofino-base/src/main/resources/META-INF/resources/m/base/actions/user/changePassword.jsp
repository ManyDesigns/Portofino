<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<stripes:layout-render name="/m/theme/templates/dialog/modal.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.user.LoginAction"/>
    <stripes:layout-component name="dialogTitle">
        Password change
    </stripes:layout-component>
    <stripes:layout-component name="dialogBody">
        <stripes:form action="<%= actionBean.getOriginalPath() %>" method="post">
            <input type="password" name="pwd" id="pwd" class="input-block-level"
                   placeholder="<fmt:message key='skins.default.login.current.password'/>" />
            <input type="password" name="newPassword" id="newPassword" class="input-block-level"
                   placeholder="<fmt:message key='skins.default.login.new.password'/>" />
            <input type="password" name="confirmNewPassword" id="confirmNewPassword" class="input-block-level"
                   placeholder="<fmt:message key='skins.default.login.confirm.new.password'/>" />
            <div class="login-buttons spacingTop">
                <button type="submit" id="changePassword2" name="changePassword2" class="btn btn-primary">Change password</button>
                <button type="submit" name="cancel" class="btn btn-link">Cancel</button>
            </div>
            <input type="hidden" name="returnUrl" value="<c:out value="${actionBean.returnUrl}"/>"/>
            <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
        </stripes:form>
        <script type="text/javascript">
            $('#pwd').focus();
        </script>
    </stripes:layout-component>
</stripes:layout-render>