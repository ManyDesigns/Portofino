<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="mdes" uri="/manydesigns-elements-struts2" %>
<s:include value="/skins/default/header.jsp"/>
<s:form method="post">
    <s:include value="/skins/default/bulkEditButtonsBar.jsp"/>
    <h1>Bulk edit: <s:property value="table.qualifiedName"/></h1>
    In the first column, select the fields you want to edit. Then, fill in their values.
    <mdes:write value="form"/>
    <s:iterator var="#current" value="selection">
        <s:hidden name="selection" value="%{#current}"/>
    </s:iterator>
    <s:if test="searchString != null">
        <s:hidden name="searchString" value="%{searchString}"/>
    </s:if>
    <s:hidden name="cancelReturnUrl" value="%{cancelReturnUrl}"/>
    <s:include value="/skins/default/bulkEditButtonsBar.jsp"/>
</s:form>
<s:include value="/skins/default/footer.jsp"/>