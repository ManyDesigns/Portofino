<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="s" uri="/struts-tags"
%><%@ taglib prefix="mdes" uri="/manydesigns-elements-struts2"
%><s:include value="/skins/default/header.jsp"/>
<s:form method="post"
        enctype="%{form.multipartRequest ? 'multipart/form-data' : 'application/x-www-form-urlencoded'}">
    <s:include value="/skins/default/model/tableData/createButtonsBar.jsp"/>
    <div id="inner-content">
        <h1>Create: <s:property value="qualifiedTableName"/></h1>
        <s:if test="form.requiredFieldsPresent">
            Fields marked with a "*" are required.
        </s:if>
        <mdes:write value="form"/>
        <s:hidden name="cancelReturnUrl" value="%{cancelReturnUrl}"/>
    </div>
    <s:include value="/skins/default/model/tableData/createButtonsBar.jsp"/>
</s:form>
<s:include value="/skins/default/footer.jsp"/>