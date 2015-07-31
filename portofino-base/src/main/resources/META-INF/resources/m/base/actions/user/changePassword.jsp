<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<stripes:layout-render name="/theme/templates/dialog/modal.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.user.LoginAction"/>
    <stripes:layout-component name="pageTitle">
        Password change
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <stripes:form action="${actionBean.context.actionPath}" method="post">
            <div class="form-group">
                <label for="pwd"><fmt:message key='current.password'/></label>
                <stripes:password name="pwd" id="pwd" class="form-control"/>
            </div>
            <div class="form-group">
                <label for="newPassword"><fmt:message key='new.password'/></label>
                <stripes:password name="newPassword" id="newPassword" class="form-control"/>
            </div>
            <div class="form-group">
                <label for="confirmNewPassword"><fmt:message key='confirm.new.password'/></label>
                <stripes:password name="confirmNewPassword" id="confirmNewPassword" class="form-control"/>
            </div>
            <div class="spacingTop">
                <button type="submit" id="changePassword2" name="changePassword2" class="btn btn-primary">Change password</button>
                <button type="submit" name="cancel" class="btn btn-default">Cancel</button>
            </div>
            <input type="hidden" name="returnUrl" value="<c:out value="${actionBean.returnUrl}"/>"/>
            <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
        </stripes:form>
        <script type="text/javascript">
            $('#pwd').focus();
        </script>
    </stripes:layout-component>
</stripes:layout-render>