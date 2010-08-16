<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="mdes" uri="/manydesigns-elements-struts2" %>
<s:include value="/skins/default/header.jsp"/>
<div id="inner-content">
    <h1>DDL Create</h1>
    <s:iterator value="ddlsCreate">
            <s:property value="toString()"/><br/>
    </s:iterator>

    <h1>DDL Update</h1>
    <s:iterator value="ddlsUpdate">
            <s:property value="toString()"/><br/>
    </s:iterator>

</div>
<s:include value="/skins/default/footer.jsp"/>