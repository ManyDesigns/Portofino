<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino" %>
<stripes:layout-render name="/m/admin/admin-theme/admin-page.jsp">
    <jsp:useBean id="actionBean" scope="request"
                 type="com.manydesigns.portofino.actions.admin.appwizard.ApplicationWizard"/>
    <stripes:layout-component name="pageTitle">
        <fmt:message key="customize.user.management" />
    </stripes:layout-component>
    <stripes:layout-component name="pageHeader">
        <jsp:include page="wizard-content-header.jsp" />
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <stripes:form beanclass="com.manydesigns.portofino.actions.admin.appwizard.ApplicationWizard"
                      method="post" class="form-horizontal">
            <p><fmt:message key="warning.configuring.user.management.will.overwrite" /></p>
            <p><fmt:message key="if.you.want.more.control.on.password.encryption" /></p>
            <mde:write name="actionBean" property="userManagementSetupForm"/>
            <div style="display: none;">
                <mde:write name="actionBean" property="userAndGroupTablesForm"/>
                <mde:write name="actionBean" property="schemasForm"/>
                <input type="hidden" name="connectionProviderType" value="${actionBean.connectionProviderType}" />
                <mde:write name="actionBean" property="connectionProviderField" />
                <mde:write name="actionBean" property="jndiCPForm"/>
                <mde:write name="actionBean" property="jdbcCPForm"/>
            </div>
            <div class="form-group">
                <div class="col-md-offset-2 col-md-10">
                    <portofino:buttons list="select-user-fields" />
                </div>
            </div>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>