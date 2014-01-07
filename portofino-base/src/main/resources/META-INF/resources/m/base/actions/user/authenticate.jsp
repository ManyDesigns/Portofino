<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"%>
<%@ taglib prefix="mde" uri="/manydesigns-elements"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="portofino" %>
<stripes:layout-render name="/theme/templates/dialog/modal.jsp">
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
        <stripes:form action="${actionBean.context.actionPath}" method="post" class="dont-prompt-on-page-abandon">
            <div id="userName" class="input-block-level value"><c:out value="${actionBean.userName}" /></div>
            <input type="hidden" name="userName" value="${actionBean.userName}" />
            <label for="pwd"><fmt:message key='password'/></label>
            <stripes:password name="pwd" id="pwd" class="input-block-level"/>
            <input type="hidden" name="rememberMe" value="${actionBean.rememberMe}" />
            <div class="login-buttons">
                <button type="submit" name="login" class="btn btn-primary">
                    <fmt:message key="login" />
                </button>
            </div>
            <input type="hidden" name="returnUrl" value="<c:out value="${actionBean.returnUrl}"/>"/>
            <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
        </stripes:form>
        <script type="text/javascript">
            $('#pwd').focus();
        </script>
    </stripes:layout-component>
</stripes:layout-render>