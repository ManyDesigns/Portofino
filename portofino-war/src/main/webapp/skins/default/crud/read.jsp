<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="s" uri="/struts-tags"
%><%@ taglib prefix="mdes" uri="/manydesigns-elements-struts2"
%><s:include value="/skins/default/header.jsp"/>
<s:form method="post">
    <s:include value="/skins/default/crud/readButtonsBar.jsp"/>
    <div id="inner-content">
        <h1><s:property value="readTitle"/></h1>
        <mdes:write value="form"/>
        <s:iterator var="current" value="subCrudUnits">
            <h2><s:property value="#current.searchTitle"/></h2>
            <mdes:write value="#current.tableForm"/>
        </s:iterator>
        <s:hidden name="pk" value="%{pk}"/>
        <s:if test="searchString != null">
            <s:hidden name="searchString" value="%{searchString}"/>
        </s:if>
        <s:set name="cancelReturnUrl"
               value="%{pkHelper.generateUrl(object, searchString)}"/>
        <s:hidden name="cancelReturnUrl" value="%{#cancelReturnUrl}"/>
    </div>
    <s:include value="/skins/default/crud/readButtonsBar.jsp"/>
</s:form>
<s:include value="/skins/default/footer.jsp"/>