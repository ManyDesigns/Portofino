<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><jsp:include page="/skins/default/header.jsp"/>
<s:form method="post">
    <jsp:include page="/skins/default/model/tableData/bulkEditButtonsBar.jsp"/>
    <div id="inner-content">
        <h1>Bulk edit: <s:property value="qualifiedTableName"/></h1>
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
    <jsp:include page="/skins/default/model/tableData/bulkEditButtonsBar.jsp"/>
</s:form>
<jsp:include page="/skins/default/footer.jsp"/>