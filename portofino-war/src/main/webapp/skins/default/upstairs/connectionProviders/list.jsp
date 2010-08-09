<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="mdes" uri="/manydesigns-elements-struts2" %>
<s:include value="/skins/default/header.jsp"/>
<s:form method="post">
    <s:include value="/skins/default/upstairs/connectionProviders/listButtonsBar.jsp"/>
    <h1>Connection providers</h1>
    <mdes:write value="tableForm"/>
    <s:include value="/skins/default/upstairs/connectionProviders/listButtonsBar.jsp"/>
</s:form>
<s:include value="/skins/default/upstairs/upstairsFooter.jsp"/>