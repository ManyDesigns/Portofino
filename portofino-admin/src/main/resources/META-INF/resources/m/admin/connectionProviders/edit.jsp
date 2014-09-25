<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.admin.database.ConnectionProvidersAction"/>
<stripes:layout-render name="/m/admin/admin-theme/admin-page.jsp">
    <stripes:layout-component name="pageTitle">
        <fmt:message key="edit.connection.provider"/>: <c:out value="${actionBean.databaseName}"/>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <stripes:form beanclass="com.manydesigns.portofino.actions.admin.database.ConnectionProvidersAction"
                      method="post" class="form-horizontal">
            <mde:write name="actionBean" property="form"/>
            <c:if test="${actionBean.detectedValuesForm != null}">
                <div class="horizontalSeparator"></div>
            </c:if>
            <c:if test="${actionBean.schemasForm != null}">
                <div class="horizontalSeparator"></div>
                <h4><fmt:message key="configured.schemas"/></h4>
                <mde:write name="actionBean" property="schemasForm"/>
            </c:if>
            <stripes:hidden name="databaseName" value="${actionBean.databaseName}"/>
            <div class="form-group">
                <div class="col-md-offset-2 col-md-10">
                    <portofino:buttons list="connectionProviders-edit" />
                </div>
            </div>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>