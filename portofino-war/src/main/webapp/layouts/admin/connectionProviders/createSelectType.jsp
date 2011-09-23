<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><stripes:layout-render name="/skins/default/admin-page.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.admin.ConnectionProvidersAction"/>
    <stripes:layout-component name="pageTitle">
        Select connection provider type
    </stripes:layout-component>
    <stripes:layout-component name="contentHeader">
        <stripes:submit name="returnToList" value="<< Return to list" class="contentButton"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletTitle">
        Select connection provider type
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <stripes:select name="connectionType">
            <stripes:option value="" label="-- Select a connection type --" />
            <stripes:option value="JDBC" label="JDBC" />
            <stripes:option value="JNDI" label="JNDI" />
        </stripes:select>
        <stripes:submit name="create" value="Select" class="portletButton"/>
        <stripes:hidden name="databaseName" value="${actionBean.databaseName}"/>
    </stripes:layout-component>
    <stripes:layout-component name="contentFooter">
        <stripes:submit name="returnToList" value="<< Return to list" class="contentButton"/>
    </stripes:layout-component>
</stripes:layout-render>