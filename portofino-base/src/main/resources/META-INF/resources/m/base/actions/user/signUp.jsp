<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<stripes:layout-render name="/theme/templates/dialog/modal.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.user.LoginAction"/>
    <stripes:layout-component name="pageTitle">
        <fmt:message key="sign.up" />
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <stripes:form id="signUpForm" action="${actionBean.context.actionPath}" method="post">
            <mde:write name="actionBean" property="signUpForm"/>
            <div class="form-group ${actionBean.captchaValidationFailed ? 'has-error' : ''}">
                <label for="captcha" class="control-label required">
                    <fmt:message key="please.type.the.text.shown.in.the.image" />
                </label>
                <div class="input-group" >
                    <input id="captcha" name="captchaText" type="text" autocomplete="off" class="form-control input-sm" />
                    <span class="input-group-btn">
                        <a onclick="$('#captcha-image').attr('src', '${pageContext.request.contextPath}${actionBean.context.actionPath}?captcha=' + Math.random());"
                           class="btn btn-default btn-sm" >
                            <em class="glyphicon glyphicon-refresh"></em>
                        </a>
                    </span>
                </div>
            </div>
            <div class="form-group">
                <img alt="captcha image" id="captcha-image" src="${pageContext.request.contextPath}${actionBean.context.actionPath}?captcha=" />
                <c:if test="${actionBean.captchaValidationFailed}">
                    <span class="help-inline"><fmt:message key="wrong.text" /></span>
                </c:if>
            </div>
            <div style="margin-top: 10px;">
                <button type="submit" name="signUp2" class="btn btn-primary">
                    <fmt:message key="sign.up" />
                </button>
                <button type="submit" name="cancel" class="btn btn-default">
                    <fmt:message key="cancel" />
                </button>
            </div>
            <input type="hidden" name="returnUrl" value="<c:out value="${actionBean.returnUrl}"/>"/>
            <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
        </stripes:form>
        <script type="text/javascript">
            $('#signUpForm input:first').focus()
        </script>
        <div>
            <hr />
            If you don't receive an email from us within a few minutes,
            please check your spam filter.
            We send you emails from the following address:

            <!-- TODO: put text in portofino messages -->

        </div>
    </stripes:layout-component>
</stripes:layout-render>