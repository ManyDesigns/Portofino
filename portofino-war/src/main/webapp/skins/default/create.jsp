<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="mdes" uri="/manydesigns-elements-struts2" %>
<s:include value="/skins/default/header.jsp"/>
<s:form method="post">
    <s:include value="/skins/default/createButtonsBar.jsp"/>
    <h1>Create: <s:property value="table.qualifiedName"/></h1>
    <s:if test="form.requiredFieldsPresent">
        Fields marked with a "*" are required.
    </s:if>
    <mdes:write value="form"/>
    <s:hidden name="pk" value="%{pk}"/>
    <s:include value="/skins/default/createButtonsBar.jsp"/>
</s:form>
<s:include value="/skins/default/footer.jsp"/>