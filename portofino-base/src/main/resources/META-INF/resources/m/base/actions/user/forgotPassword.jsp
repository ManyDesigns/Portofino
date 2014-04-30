<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<stripes:layout-render name="/theme/templates/dialog/modal.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.user.LoginAction"/>
    <stripes:layout-component name="pageTitle">
        Forgot your password?
    </stripes:layout-component>
    <stripes:layout-component name="pageTitle">
        Forgot your password?
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        Enter your email address below and we'll send you password reset instructions.
        <stripes:form action="${actionBean.context.actionPath}" method="post" class="spacingTop">
            <div class="form-group">
                <input type="text" name="email" id="email" class="form-control"
                       placeholder="<fmt:message key='email'/>" />
            </div>
            <div class="marginTop20px">
                <button type="submit" name="forgotPassword2" class="btn btn-primary">Next</button>
                <button type="submit" name="cancel" class="btn btn-link">Cancel</button>
            </div>
            <input type="hidden" name="returnUrl" value="<c:out value="${actionBean.returnUrl}"/>"/>
            <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
        </stripes:form>
        <script type="text/javascript">
            $('#email').focus();
        </script>
        <div>
            <hr />
            If you don't receive an email from us within a few minutes,
            please check your spam filter.
            We send you emails from the following address:
        </div>
    </stripes:layout-component>
</stripes:layout-render>