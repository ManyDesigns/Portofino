<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="mdes" uri="/manydesigns-elements-struts2" %>
<s:include value="/header.jsp"/>
<h1>Read: <s:property value="table.qualifiedName"/></h1>

<table>
    <s:iterator var="column" value="table.columns">
        <tr>
            <td>
                <s:property value="#column.columnName"/>
            </td>
            <td>
                <s:property value="object[#column.columnName]"/>
            </td>
        </tr>
    </s:iterator>

</table>

<s:iterator var="current" value="relatedObjectsList">
    <s:set name="rel" value="#current.relationship"/>
    <s:set name="fromTable" value="#rel.fromTable"/>
    <h2><s:property value="#rel.relationshipName"/></h2>
    <table>
        <thead>
            <tr>
                <s:iterator var="column" value="fromTable.primaryKey.columns">
                    <th><s:property value="#column.columnName"/></th>
                </s:iterator>
            </tr>
        </thead>
        <s:iterator var="relObject" value="#current.objects">
        <tr>
            <s:url var="readUrl" namespace="/" action="%{#fromTable.qualifiedName}/Read">
                <s:param name="pk" value="@com.manydesigns.portofino.methods.LinkHelper@getPk(#fromTable, #relObject)"/>
            </s:url>
            <s:iterator var="column" value="#fromTable.primaryKey.columns">
                <td><s:a href="%{readUrl}">
                    <s:property value="#relObject[#column.columnName]"/>
                </s:a></td>
            </s:iterator>
        </tr>
        </s:iterator>

    </table>
</s:iterator>

<s:include value="/footer.jsp"/>