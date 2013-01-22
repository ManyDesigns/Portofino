<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<stripes:layout-render name="/skins/default/admin-page.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.admin.ConnectionProvidersAction"/>
    <stripes:layout-component name="pageTitle">
        <fmt:message key="layouts.admin.connectionProviders.edit.select_provider_type"/>: <c:out value="${actionBean.databaseName}"/>
    </stripes:layout-component>
    <stripes:layout-component name="contentHeader">
        <portofino:buttons list="connectionProviders-edit" cssClass="contentButton" />
    </stripes:layout-component>
    <stripes:layout-component name="portletTitle">
        <fmt:message key="layouts.admin.connectionProviders.edit.select_provider_type"/>: <c:out value="${actionBean.databaseName}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <mde:write name="actionBean" property="form"/>
        <c:if test="${actionBean.detectedValuesForm != null}">
            <div class="horizontalSeparator"></div>
        </c:if>
        <c:if test="${actionBean.schemasForm != null}">
            <div class="horizontalSeparator"></div>
            <h2><fmt:message key="layouts.admin.connectionProviders.read.configured_schemas"/></h2>
            <mde:write name="actionBean" property="schemasForm"/>
        </c:if>
        <stripes:hidden name="databaseName" value="${actionBean.databaseName}"/>
    </stripes:layout-component>
    <stripes:layout-component name="contentFooter">
        <portofino:buttons list="connectionProviders-edit" cssClass="contentButton" />
    </stripes:layout-component>
</stripes:layout-render>