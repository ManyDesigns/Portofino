<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><jsp:include page="/skins/default/header.jsp"/>
<s:form method="post">
    <jsp:include page="/skins/default/model/tableData/searchButtonsBar.jsp"/>
    <div id="inner-content">
        <h1>Search: <s:property value="qualifiedTableName"/></h1>
        <s:if test="!searchForm.isEmpty()">
            <div class="search_form">
                <mdes:write value="searchForm"/>
                <s:submit method="search" value="Search"/>
                <s:reset value="Reset form"/>
            </div>
        </s:if>
        <mdes:write value="tableForm"/>
        <s:if test="searchString != null">
            <s:hidden name="searchString" value="%{searchString}"/>
        </s:if>
        <s:url var="cancelReturnUrl"
               namespace="/model"
               action="%{qualifiedTableName}/TableData"
               escapeAmp="false">
            <s:param name="searchString" value="%{searchString}"/>
        </s:url>
        <s:hidden name="cancelReturnUrl" value="%{#cancelReturnUrl}"/>
    </div>
    <jsp:include page="/skins/default/model/tableData/searchButtonsBar.jsp"/>
</s:form>
<jsp:include page="/skins/default/footer.jsp"/>