<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="mdes" uri="/manydesigns-elements-struts2" %>
<s:include value="/header.jsp"/>
<h1>Search: <s:property value="table.qualifiedName"/></h1>

<table>
    <thead>
        <tr>
            <s:iterator var="column" value="table.primaryKey.columns">
                <th><s:property value="#column.columnName"/></th>
            </s:iterator>
        </tr>
    </thead>
    <s:iterator value="objects" var="object">
    <tr>
        <s:url var="readUrl" namespace="/" action="%{table.qualifiedName}/Read?%{@com.manydesigns.portofino.methods.LinkHelper@getPk(table, #object)}"/>
        <s:iterator var="column" value="table.primaryKey.columns">
            <td><s:a href="%{readUrl}">
                <s:property value="#object[#column.columnName]"/>
            </s:a></td>
        </s:iterator>
    </tr>
    </s:iterator>

</table>

<s:include value="/footer.jsp"/>