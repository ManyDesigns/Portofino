<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="mdes" uri="/manydesigns-elements-struts2" %>
<s:include value="/skins/default/header.jsp"/>
<div id="inner-content">
    <h1>Portofino properties</h1>
    <mdes:write value="portofinoForm"/>
    <h1>Elements properties</h1>
    <mdes:write value="elementsForm"/>
</div>
<s:include value="/skins/default/upstairs/upstairsFooter.jsp"/>