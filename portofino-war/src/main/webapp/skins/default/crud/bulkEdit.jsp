<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="s" uri="/struts-tags"
%><%@ taglib prefix="mdes" uri="/manydesigns-elements-struts2"
%><s:include value="/skins/default/header.jsp"/>
<s:form method="post"
        enctype="%{form.multipartRequest ? 'multipart/form-data' : 'application/x-www-form-urlencoded'}">
    <s:include value="/skins/default/crud/bulkEditButtonsBar.jsp"/>
    <div id="inner-content">
        <s:if test="form != null">
            <h1><s:property value="editTitle"/></h1>
            In the first column, select the fields you want to edit. Then, fill in their values.
            <mdes:write value="form"/>
            <s:iterator var="#currentSelection" value="selection">
                <s:hidden name="selection" value="%{#currentSelection}"/>
            </s:iterator>
        </s:if>
        <s:iterator var="current" value="subCrudUnits" status="status">
            <h2><s:property value="#current.editTitle"/></h2>
            <mdes:write value="#current.form"/>
            <s:iterator var="#currentSelection" value="#current.selection">
                <s:hidden name="subCrudUnits[%{#status.index}].selection" value="%{#currentSelection}"/>
            </s:iterator>
        </s:iterator>
        <s:hidden name="pk" value="%{pk}"/>
        <s:if test="searchString != null">
            <s:hidden name="searchString" value="%{searchString}"/>
        </s:if>
        <s:hidden name="cancelReturnUrl" value="%{cancelReturnUrl}"/>
    </div>
    <s:include value="/skins/default/crud/bulkEditButtonsBar.jsp"/>
</s:form>
<s:include value="/skins/default/footer.jsp"/>