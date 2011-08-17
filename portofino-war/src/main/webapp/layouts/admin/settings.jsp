<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><stripes:layout-render name="/skins/default/admin-page.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.admin.SettingsAction"/>
    <stripes:layout-component name="pageTitle">
        Settings
    </stripes:layout-component>
    <stripes:layout-component name="contentHeader">
        <stripes:submit name="update" value="Update" class="contentButton"/>
    </stripes:layout-component>
    <stripes:layout-component name="contentBody">
        <mde:write name="actionBean" property="form"/>
    </stripes:layout-component>
    <stripes:layout-component name="contentFooter">
        <stripes:submit name="update" value="Update" class="contentButton"/>
    </stripes:layout-component>
</stripes:layout-render>