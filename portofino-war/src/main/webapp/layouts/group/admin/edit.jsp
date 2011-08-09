<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><jsp:include page="/skins/default/header.jsp"/>
<s:form method="post">
    <jsp:include page="/skins/default/model/tableData/editButtonsBar.jsp"/>
    <div id="inner-content">
        <h1>Edit: <s:property value="qualifiedTableName"/></h1>
        <s:if test="form.requiredFieldsPresent">
            Fields marked with a "*" are required.
        </s:if>
        <mdes:write value="form"/>
        <s:hidden name="pk" value="%{pk}"/>
        <s:if test="searchString != null">
            <s:hidden name="searchString" value="%{searchString}"/>
        </s:if>
        <s:hidden name="cancelReturnUrl" value="%{cancelReturnUrl}"/>
    </div>
    <jsp:include page="/skins/default/model/tableData/editButtonsBar.jsp"/>
</s:form>
<jsp:include page="/skins/default/footer.jsp"/>