<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="mdes" uri="/manydesigns-elements-struts2" %>
<s:include value="/skins/default/header.jsp"/>
<s:form method="post">
    <s:include value="/skins/default/searchButtonsBar.jsp"/>
    <h1>Search: <s:property value="table.qualifiedName"/></h1>
    <mdes:write value="tableForm"/>
    <s:url var="cancelReturnUrl" namespace="/" action="%{qualifiedTableName}/Table">
    </s:url>
    <s:hidden name="cancelReturnUrl" value="%{#cancelReturnUrl}"/>
    <s:include value="/skins/default/searchButtonsBar.jsp"/>
</s:form>
<s:include value="/skins/default/footer.jsp"/>