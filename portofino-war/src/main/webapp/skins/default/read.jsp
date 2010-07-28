<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="mdes" uri="/manydesigns-elements-struts2" %>
<s:include value="/skins/default/header.jsp"/>
<s:form method="post">
    <s:include value="/skins/default/readButtonsBar.jsp"/>
    <h1>Read: <s:property value="table.qualifiedName"/></h1>
    Position: <s:property value="objects.indexOf(object)+1"/> of <s:property value="objects.size()"/>
    <mdes:write value="form"/>
    <s:iterator var="current" value="relatedTableFormList">
        <s:set name="rel" value="#current.relationship"/>
        <h2><s:property value="#rel.relationshipName"/></h2>
        <mdes:write value="#current.tableForm"/>
    </s:iterator>
    <s:hidden name="pk" value="%{pk}"/>
    <s:if test="searchString != null">
        <s:hidden name="searchString" value="%{searchString}"/>
    </s:if>
    <s:url var="cancelReturnUrl"
           namespace="/"
           action="%{qualifiedTableName}/Table"
           escapeAmp="false">
        <s:param name="pk" value="%{pk}"/>
        <s:param name="searchString" value="%{searchString}"/>
    </s:url>
    <s:hidden name="cancelReturnUrl" value="%{#cancelReturnUrl}"/>
    <s:include value="/skins/default/readButtonsBar.jsp"/>
</s:form>
<s:include value="/skins/default/footer.jsp"/>