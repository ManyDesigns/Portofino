<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<stripes:layout-render name="/m/theme/templates/dialog/modal.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.user.LoginAction"/>
    <stripes:layout-component name="pageTitle">
        <fmt:message key="skins.default.login.signUp"/>
    </stripes:layout-component>
    <stripes:layout-component name="dialogTitle">
        <fmt:message key="skins.default.login.signUp" />
    </stripes:layout-component>
    <stripes:layout-component name="dialogBody">
        <stripes:form id="signUpForm" action="${actionBean.originalPath}" method="post">
            <mde:write name="actionBean" property="signUpForm"/>
            <div class="control-group ${actionBean.captchaValidationFailed ? 'error' : ''}">
                <label for="captcha" class="control-label required">
                    <fmt:message key="skins.default.login.captcha" />
                </label>
                <div class="controls">
                    <div class="input-append" style="margin-top: 5px;">
                        <input id="captcha" name="captchaText" type="text" autocomplete="off" class="input-small" />
                        <a onclick="$('#captcha-image').attr('src', '${pageContext.request.contextPath}${actionBean.originalPath}?captcha=' + Math.random());"
                           class="btn" >
                            <i class="icon-refresh"></i>
                        </a>
                    </div>
                    <img alt="captcha image" id="captcha-image" src="${pageContext.request.contextPath}${actionBean.originalPath}?captcha=" />
                    <c:if test="${actionBean.captchaValidationFailed}">
                        <span class="help-inline"><fmt:message key="skins.default.login.captcha.error" /></span>
                    </c:if>
                </div>
            </div>
            <div class="login-buttons" style="margin-top: 10px;">
                <button type="submit" name="signUp2" class="btn btn-primary">
                    <fmt:message key="skins.default.login.signUp" />
                </button>
                <button type="submit" name="cancel" class="btn btn-link">
                    <fmt:message key="commons.cancel" />
                </button>
            </div>
            <input type="hidden" name="returnUrl" value="<c:out value="${actionBean.returnUrl}"/>"/>
            <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
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