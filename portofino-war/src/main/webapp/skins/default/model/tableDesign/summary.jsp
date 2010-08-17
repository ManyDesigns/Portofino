<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="s" uri="/struts-tags"
%><%@ taglib prefix="mdes" uri="/manydesigns-elements-struts2"
%><s:include value="/skins/default/header.jsp"/>
<s:form method="post">
    <s:include value="/skins/default/model/tableDesign/summaryButtonsBar.jsp"/>
    <div id="inner-content">
        <h1>Table design summary: <s:property value="table.qualifiedName"/></h1>
        <mdes:write value="form"/>
        <h2>Columns</h2>
        <mdes:write value="columnTableForm"/>
        <s:hidden name="cancelReturnUrl" value="%{cancelReturnUrl}"/>
    </div>
    <s:include value="/skins/default/model/tableDesign/summaryButtonsBar.jsp"/>
</s:form>
<s:include value="/skins/default/footer.jsp"/>