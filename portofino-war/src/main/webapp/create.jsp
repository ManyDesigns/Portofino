<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="mdes" uri="/manydesigns-elements-struts2" %>
<s:include value="/header.jsp"/>
<s:form method="post">
    <s:include value="/createButtonsBar.jsp"/>
    <h1>Create: <s:property value="table.qualifiedName"/></h1>
    <mdes:write value="form"/>
    <s:hidden name="pk" value="%{pk}"/>
    <s:include value="/createButtonsBar.jsp"/>
</s:form>
<s:include value="/footer.jsp"/>