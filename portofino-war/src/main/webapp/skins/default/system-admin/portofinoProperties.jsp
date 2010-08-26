<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="s" uri="/struts-tags"
%><%@taglib prefix="mdes" uri="/manydesigns-elements-struts2"
%><s:include value="/skins/default/header.jsp"/>
<div class="tabs-bar-top">
    <s:url var="portofinoPropertiesUrl" method="portofinoProperties"/>
    <s:url var="elementsPropertiesUrl" method="elementsProperties"/>
    <s:url var="systemPropertiesUrl" method="systemProperties"/>
    <ul class="tabs">
        <li class="selected"><s:a href="%{#portofinoPropertiesUrl}">Portofino properties</s:a></li>
        <li><s:a href="%{#elementsPropertiesUrl}">Elements properties</s:a></li>
        <li><s:a href="%{#systemPropertiesUrl}">System properties</s:a></li>
    </ul>
</div>
<div id="inner-content">
    <h1>Portofino properties</h1>
    <mdes:write value="form"/>
</div>
<s:include value="/skins/default/footer.jsp"/>