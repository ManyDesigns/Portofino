<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><stripes:layout-render name="/skins/default/admin-page.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.admin.ConnectionProvidersAction"/>
    <stripes:layout-component name="pageTitle">
        Connection providers
    </stripes:layout-component>
    <stripes:layout-component name="contentHeader">
        <stripes:submit name="create" value="Create new" class="contentButton"/>
        <stripes:submit name="bulkDelete" value="Delete"
                        onclick="return confirm ('Are you sure?');"
                        class="contentButton"/>
        <stripes:submit name="returnToPages" value="Return to pages" class="contentButton"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletTitle">
        Connection providers
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <mde:write name="actionBean" property="tableForm"/>
        <h2>Available database platforms</h2>
        <mde:write name="actionBean" property="databasePlatformsTableForm"/>
    </stripes:layout-component>
    <stripes:layout-component name="contentFooter">
        <stripes:submit name="create" value="Create new" class="contentButton"/>
        <stripes:submit name="bulkDelete" value="Delete"
                        onclick="return confirm ('Are you sure?');"
                        class="contentButton"/>
        <stripes:submit name="returnToPages" value="Return to pages" class="contentButton"/>
    </stripes:layout-component>
</stripes:layout-render>