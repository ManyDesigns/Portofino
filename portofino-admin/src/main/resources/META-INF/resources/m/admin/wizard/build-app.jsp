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
        <fmt:message key="generate.pages" />
    </stripes:layout-component>
    <stripes:layout-component name="pageHeader">
        <jsp:include page="wizard-content-header.jsp" />
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <stripes:form beanclass="com.manydesigns.portofino.actions.admin.appwizard.ApplicationWizard"
                      method="post">
            <p>
                <fmt:message key="press.finish.to.build.the.application" />
            </p>
            <div style="display: none;">
                <input type="hidden" name="generateCalendar" value="${actionBean.generateCalendar}" />
                <input type="hidden" name="generationStrategy" value="${actionBean.generationStrategy}" />
                <mde:write name="actionBean" property="rootsForm"/>
                <c:if test="${not empty actionBean.userManagementSetupForm}">
                    <mde:write name="actionBean" property="userManagementSetupForm"/>
                </c:if>
                <mde:write name="actionBean" property="userAndGroupTablesForm"/>
                <mde:write name="actionBean" property="schemasForm"/>
                <input type="hidden" name="connectionProviderType" value="${actionBean.connectionProviderType}" />
                <mde:write name="actionBean" property="connectionProviderField" />
                <mde:write name="actionBean" property="jndiCPForm"/>
                <mde:write name="actionBean" property="jdbcCPForm"/>
            </div>
            <div>
                <portofino:buttons list="build-app" />
            </div>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>