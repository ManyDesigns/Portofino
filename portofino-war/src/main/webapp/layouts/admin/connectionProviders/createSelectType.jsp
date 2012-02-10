<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino" %>

<stripes:layout-render name="/skins/default/admin-page.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.admin.ConnectionProvidersAction"/>
    <stripes:layout-component name="pageTitle">
        <fmt:message key="layouts.admin.connectionProviders.createSelectType.select_provider_type"/>
    </stripes:layout-component>
    <stripes:layout-component name="contentHeader">
        <portofino:buttons list="connectionProviders-select-type-content-buttons"
                           cssClass="contentButton" />
    </stripes:layout-component>
    <stripes:layout-component name="portletTitle">
        <fmt:message key="layouts.admin.connectionProviders.createSelectType.select_provider_type"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <stripes:select name="connectionType">
            <stripes:option value="" label="-- Select a connection type --" />
            <stripes:option value="JDBC" label="JDBC" />
            <stripes:option value="JNDI" label="JNDI" />
        </stripes:select>
        <portofino:buttons list="connectionProviders-select-type"
                           cssClass="portletButton" />
        <stripes:hidden name="databaseName" value="${actionBean.databaseName}"/>
    </stripes:layout-component>
    <stripes:layout-component name="contentFooter">
        <portofino:buttons list="connectionProviders-select-type-content-buttons"
                           cssClass="contentButton" />
    </stripes:layout-component>
</stripes:layout-render>