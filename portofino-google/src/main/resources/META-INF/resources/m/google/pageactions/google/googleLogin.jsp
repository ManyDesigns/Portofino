<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="mde" uri="/manydesigns-elements" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="portofino" %>
<%@ page import="com.manydesigns.portofino.shiro.ShiroUtils" %>
<stripes:layout-render name="/theme/templates/dialog/modal.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.google.GoogleLoginAction"/>

    <stripes:layout-component name="pageTitle">
        <fmt:message key="login.to">
            <fmt:param value="${actionBean.applicationName}" />
        </fmt:message>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <stripes:form action="${actionBean.context.actionPath}" method="post" class="dont-prompt-on-page-abandon">
            <div class="form-group">
                <label for="userName"><fmt:message key='user.name'/></label>
                <stripes:text name="userName" id="userName" class="form-control"/>
            </div>
            <div class="form-group">
                <label for="pwd"><fmt:message key='password'/></label>
                <stripes:password name="pwd" id="pwd" class="form-control"/>
            </div>
            <div class="form-control checkbox">
                <stripes:checkbox name="rememberMe" id="rememberMe"/>
                <label for="rememberMe">
                    <fmt:message key='remember.me.on.this.computer'/>
                </label>
            </div>
            <br/>
            <button type="submit" name="login" class="btn btn-primary">
                <fmt:message key="login" />
            </button>
            <input type="hidden" name="returnUrl" value="<c:out value="${actionBean.returnUrl}"/>"/>
            <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
        </stripes:form>
        <%
            try {
                ShiroUtils.getPortofinoRealm();
        %>
        <div class="login-links">
            <hr />
            <div class="login-link">
                <stripes:link href="${actionBean.context.actionPath}">
                    <stripes:param name="forgotPassword" value=""/>
                    <stripes:param name="returnUrl" value="${actionBean.returnUrl}"/>
                    <stripes:param name="cancelReturnUrl" value="${actionBean.cancelReturnUrl}"/>
                    <fmt:message key='forgot.your.password'/>
                </stripes:link>
            </div>
            <div class="login-link">
                <fmt:message key='dont.have.an.account'/>
                <stripes:link href="${actionBean.context.actionPath}">
                    <stripes:param name="signUp" value=""/>
                    <stripes:param name="returnUrl" value="/"/>
                    <stripes:param name="cancelReturnUrl" value="/"/>
                    <fmt:message key='sign.up.now'/>
                </stripes:link>
            </div>
        </div>
        <%
            } catch (ClassCastException e) {
                //Portofino realm not available; don't show sign up and change password links.
            }
        %>
        <hr>
        <form method="GET" action="https://accounts.google.com/o/oauth2/v2/auth">
            <input type="hidden" name="client_id" value="<c:out value="${actionBean.portofinoConfiguration.getString(\"google.client.id\")}"/>">
            <input type="hidden" name="redirect_uri" value="<c:out value="${actionBean.portofinoConfiguration.getString(\"google.redirect.uri\")}"/>">
            <input type="hidden" name="response_type" value="token">
            <input type="hidden" name="scope" value="https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile">
            <input type="hidden" name="include_granted_scopes" value="true">
            <input type="hidden" name="state" value="pass-through value">

            <button type="submit" name="google" class="btn btn-default">
                <svg aria-hidden="true" class="svg-icon iconGoogle" width="18" height="18" viewBox="0 0 18 18">
                    <path d="M16.51 8H8.98v3h4.3c-.18 1-.74 1.48-1.6 2.04v2.01h2.6a7.8 7.8 0 0 0 2.38-5.88c0-.57-.05-.66-.15-1.18z" fill="#4285F4"></path>
                    <path d="M8.98 17c2.16 0 3.97-.72 5.3-1.94l-2.6-2a4.8 4.8 0 0 1-7.18-2.54H1.83v2.07A8 8 0 0 0 8.98 17z" fill="#34A853"></path>
                    <path d="M4.5 10.52a4.8 4.8 0 0 1 0-3.04V5.41H1.83a8 8 0 0 0 0 7.18l2.67-2.07z" fill="#FBBC05"></path>
                    <path d="M8.98 4.18c1.17 0 2.23.4 3.06 1.2l2.3-2.3A8 8 0 0 0 1.83 5.4L4.5 7.49a4.77 4.77 0 0 1 4.48-3.3z" fill="#EA4335"></path>
                </svg>
                Google
            </button>
        </form>
        <style>
            .svg-icon {
                vertical-align: baseline;
                margin-top: -0.3em;
                margin-bottom: -0.3em;
            }
        </style>
        <script type="text/javascript">
            $('#userName').focus();
        </script>
    </stripes:layout-component>
</stripes:layout-render>
