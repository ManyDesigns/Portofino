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

<s:include value="/footer.jsp"/>