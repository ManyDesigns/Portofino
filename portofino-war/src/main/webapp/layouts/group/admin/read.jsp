<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><jsp:include page="/skins/default/header.jsp"/>
<s:form method="post">
    <jsp:include page="/skins/default/model/tableData/readButtonsBar.jsp"/>
    <div id="inner-content">
        <h1>Read: <s:property value="qualifiedTableName"/></h1>
        <mdes:write value="form"/>
        <s:iterator var="current" value="relatedTableFormList">
            <s:set name="rel" value="#current.relationship"/>
            <h2><s:property value="#rel.relationshipName"/></h2>
            <mdes:write value="#current.tableForm"/>
        </s:iterator>
        <s:hidden name="pk" value="%{pk}"/>
        <s:if test="searchString != null">
            <s:hidden name="searchString" value="%{searchString}"/>
        </s:if>
        <s:url var="cancelReturnUrl"
               namespace="/model"
               action="%{qualifiedTableName}/TableData"
               escapeAmp="false">
            <s:param name="pk" value="%{pk}"/>
            <s:param name="searchString" value="%{searchString}"/>
        </s:url>
        <s:hidden name="cancelReturnUrl" value="%{#cancelReturnUrl}"/>
    </div>
    <jsp:include page="/skins/default/model/tableData/readButtonsBar.jsp"/>
</s:form>
<jsp:include page="/skins/default/footer.jsp"/>