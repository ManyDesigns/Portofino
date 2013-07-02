<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<stripes:layout-render name="/skins/${skin}/login.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.user.LoginAction"/>
    <stripes:layout-component name="loginTitle">
        Sign up
    </stripes:layout-component>
    <stripes:layout-component name="loginBody">
        <stripes:form id="signUpForm" action="<%= actionBean.getOriginalPath() %>" method="post">
            <mde:write name="actionBean" property="signUpForm"/>
            <div class="login-buttons marginTop20px">
                <button type="submit" name="signUp2" class="btn btn-primary">Sign up</button>
                <button type="submit" name="cancel" class="btn btn-link">Cancel</button>
            </div>
            <stripes:hidden name="returnUrl"/>
            <stripes:hidden name="cancelReturnUrl"/>
        </stripes:form>
        <script type="text/javascript">
            $('#signUpForm input:first').focus()
        </script>
        <div class="spacingWithDividerTop">
            If you don't receive an email from us within a few minutes,
            please check your spam filter.
            We send you emails from the following address:
        </div>
    </stripes:layout-component>
</stripes:layout-render>