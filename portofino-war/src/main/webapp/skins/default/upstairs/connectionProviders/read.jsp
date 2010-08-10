<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="mdes" uri="/manydesigns-elements-struts2" %>
<s:include value="/skins/default/header.jsp"/>
<s:form method="post">
    <s:include value="/skins/default/upstairs/connectionProviders/readButtonsBar.jsp"/>
    <h1>Connection provider: <s:property value="databaseName"/></h1>
    <mdes:write value="form"/>
    <h2>Detected values</h2>
    <mdes:write value="detectedValuesForm"/>
    <h2>Supported column types</h2>
    <mdes:write value="typesTableForm"/>
    <s:hidden name="databaseName" value="%{databaseName}"/>
    <s:include value="/skins/default/upstairs/connectionProviders/readButtonsBar.jsp"/>
</s:form>
<s:include value="/skins/default/upstairs/upstairsFooter.jsp"/>