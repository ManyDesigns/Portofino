<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="s" uri="/struts-tags"
%><%@ taglib prefix="mdes" uri="/manydesigns-elements-struts2"
%><s:include value="/skins/default/header.jsp"/>
<s:form method="post">
    <s:include value="/skins/default/model/tableData/bulkEditButtonsBar.jsp"/>
    <div id="inner-content">
        <h1>Bulk edit: <s:property value="qualifiedName"/></h1>
        In the first column, select the fields you want to edit. Then, fill in their values.
        <mdes:write value="form"/>
        <s:iterator var="#current" value="selection">
            <s:hidden name="selection" value="%{#current}"/>
        </s:iterator>
        <s:if test="searchString != null">
            <s:hidden name="searchString" value="%{searchString}"/>
        </s:if>
        <s:hidden name="cancelReturnUrl" value="%{cancelReturnUrl}"/>
    </div>
    <s:include value="/skins/default/model/tableData/bulkEditButtonsBar.jsp"/>
</s:form>
<s:include value="/skins/default/footer.jsp"/>