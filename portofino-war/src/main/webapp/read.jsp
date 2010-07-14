<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="mdes" uri="/manydesigns-elements-struts2" %>
<s:include value="/header.jsp"/>
<s:form method="post">
    <div class="buttons-bar">
        <s:submit method="edit" value="Edit" theme="simple"/>
        <s:submit method="delete" value="Delete" theme="simple"/>
    </div>
    <h1>Read: <s:property value="table.qualifiedName"/></h1>
    <mdes:write value="form"/>
    <s:iterator var="current" value="relatedTableFormList">
        <s:set name="rel" value="#current.relationship"/>
        <h2><s:property value="#rel.relationshipName"/></h2>
        <mdes:write value="#current.tableForm"/>
    </s:iterator>
</s:form>
<s:include value="/footer.jsp"/>