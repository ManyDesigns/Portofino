<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<stripes:layout-render name="/skins/default/admin-page.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.admin.TablesAction"/>
    <stripes:layout-component name="pageTitle">
        Edit short name for table ${actionBean.table.qualifiedName}
    </stripes:layout-component>
    <stripes:layout-component name="contentHeader">
        <portofino:buttons list="table-short-name" cssClass="contentButton" />
    </stripes:layout-component>
    <stripes:layout-component name="portletTitle">
        Edit short name for table ${actionBean.table.qualifiedName}
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <mde:write name="actionBean" property="shortNameField" />
        <div style="visibility: hidden;">
            <mde:write name="actionBean" property="tableForm" />
            <mde:write name="actionBean" property="columnsTableForm" />
        </div>
    </stripes:layout-component>
    <stripes:layout-component name="contentFooter">
        <portofino:buttons list="table-short-name" cssClass="contentButton" />
    </stripes:layout-component>
</stripes:layout-render>