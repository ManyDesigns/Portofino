<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ page import="com.manydesigns.portofino.shiro.ShiroUtils"
%><stripes:layout-render name="/theme/templates/dialog/modal.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.user.LoginAction"/>
    <stripes:layout-component name="pageTitle">
        <fmt:message key="login.to">
            <fmt:param value="${actionBean.applicationName}" />
        </fmt:message>
    </stripes:layout-component>
    <stripes:layout-component name="pageTitle">
        <fmt:message key="login.to">
            <fmt:param value="${actionBean.applicationName}" />
        </fmt:message>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <stripes:form action="${actionBean.context.actualServletPath}" method="post" class="dont-prompt-on-page-abandon">
            <input type="text" name="userName" id="userName" class="input-block-level"
                   placeholder="<fmt:message key='user.name'/>" />
            <input type="password" name="pwd" id="pwd" class="input-block-level"
                   placeholder="<fmt:message key='password'/>" />
            <div class="login-buttons">
                <button type="submit" name="login" class="btn btn-primary">Log in</button>
            </div>
            <input type="hidden" name="returnUrl" value="<c:out value="${actionBean.returnUrl}"/>"/>
            <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
        </stripes:form>
        <%
        try {
            ShiroUtils.getPortofinoRealm();
        %>  <div class="login-links">
                <hr />
                <div class="login-link">
                    <stripes:link href="${actionBean.context.actualServletPath}">
                        <stripes:param name="forgotPassword" value=""/>
                        <stripes:param name="returnUrl" value="${actionBean.returnUrl}"/>
                        <stripes:param name="cancelReturnUrl" value="${actionBean.cancelReturnUrl}"/>
                        <fmt:message key='forgot.your.password'/>
                    </stripes:link>
                </div>
                <div class="login-link">
                    Don't have an account?
                    <stripes:link href="${actionBean.context.actualServletPath}">
                        <stripes:param name="signUp" value=""/>
                        <stripes:param name="returnUrl" value="/"/>
                        <stripes:param name="cancelReturnUrl" value="/"/>
                        Sign up now
                    </stripes:link>
                </div>
            </div><%
        } catch (ClassCastException e) {
            //Portofino realm not available; don't show sign up and change password links.
        }
        %>
        <script type="text/javascript">
            $('#userName').focus();
        </script>
    </stripes:layout-component>
</stripes:layout-render>