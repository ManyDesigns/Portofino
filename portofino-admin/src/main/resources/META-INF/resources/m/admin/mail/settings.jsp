<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.admin.mail.MailSettingsAction"/>
<stripes:layout-render name="/m/admin/admin-theme/admin-page.jsp">
    <stripes:layout-component name="pageTitle">
        Mail settings
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">

        <div class="alert alert-info" role="alert"><p><fmt:message key="note.restart.application" /></p> </div>
        <div class="alert alert-warning" role="alert"><p><fmt:message key="warning.change.mail.queue" /></p> </div>

        <stripes:form beanclass="com.manydesigns.portofino.actions.admin.mail.MailSettingsAction"
                      method="post" class="form-horizontal">
            <mde:write name="actionBean" property="form"/>
            <div class="form-group">
                <div class="col-md-offset-2 col-md-10">
                    <portofino:buttons list="settings" />
                </div>
            </div>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>