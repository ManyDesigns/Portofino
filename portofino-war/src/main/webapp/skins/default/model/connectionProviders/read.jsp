<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><jsp:include page="/skins/default/header.jsp"/>
<s:form method="post">
    <jsp:include page="/skins/default/model/connectionProviders/readButtonsBar.jsp"/>
    <div id="inner-content">
        <h1>Connection provider: <s:property value="databaseName"/></h1>
        <mdes:write value="form"/>
        <s:if test="detectedValuesForm != null">
            <h2>Detected values</h2>
            <mdes:write value="detectedValuesForm"/>
        </s:if>
        <s:if test="typesTableForm != null">
            <h2>Supported column types</h2>
            <mdes:write value="typesTableForm"/>
        </s:if>
        <s:hidden name="databaseName" value="%{databaseName}"/>
    </div>
    <jsp:include page="/skins/default/model/connectionProviders/readButtonsBar.jsp"/>
</s:form>
<jsp:include page="/skins/default/footer.jsp"/>