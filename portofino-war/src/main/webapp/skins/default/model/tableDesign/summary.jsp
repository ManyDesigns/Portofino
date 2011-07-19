<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><jsp:include page="/skins/default/header.jsp"/>
<s:form method="post">
    <jsp:include page="/skins/default/model/tableDesign/summaryButtonsBar.jsp"/>
    <div id="inner-content">
        <h1>Table design summary: <s:property value="table.qualifiedName"/></h1>
        <mdes:write value="form"/>
        <h2>Columns</h2>
        <mdes:write value="columnTableForm"/>
        <s:hidden name="cancelReturnUrl" value="%{cancelReturnUrl}"/>
    </div>
    <jsp:include page="/skins/default/model/tableDesign/summaryButtonsBar.jsp"/>
</s:form>
<jsp:include page="/skins/default/footer.jsp"/>