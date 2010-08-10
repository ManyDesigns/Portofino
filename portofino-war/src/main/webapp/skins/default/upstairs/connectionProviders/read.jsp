<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="mdes" uri="/manydesigns-elements-struts2" %>
<s:include value="/skins/default/header.jsp"/>
<s:form method="post">
    <s:include value="/skins/default/upstairs/connectionProviders/readButtonsBar.jsp"/>
    <h1>Connection provider: <s:property value="databaseName"/></h1>
    <mdes:write value="form"/>
    <s:if test="detectedValuesForm != null">
        <h2>Detected values</h2>
        <mdes:write value="detectedValuesForm"/>
    </s:if>
    <s:if test="typesTableForm != null">
        <h2>Supported column types</h2>
        <mdes:write value="typesTableForm"/>
    </s:if>
    <s:hidden name="databaseName" value="%{databaseName}"/>
    <s:include value="/skins/default/upstairs/connectionProviders/readButtonsBar.jsp"/>
</s:form>
<s:include value="/skins/default/upstairs/upstairsFooter.jsp"/>