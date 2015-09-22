<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><%@ page import="com.manydesigns.portofino.shiro.ShiroUtils"
%><stripes:layout-render name="/theme/templates/dialog/modal.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.user.LoginAction"/>

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
        %>  <div class="login-links">
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