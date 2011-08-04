<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><jsp:include page="/skins/default/header.jsp"/>
<div class="tabs-bar-top">
    <s:url var="portofinoConfigurationUrl" method="portofinoConfiguration"/>
    <s:url var="elementsConfigurationUrl" method="elementsConfiguration"/>
    <s:url var="systemConfigurationUrl" method="systemConfiguration"/>
    <ul class="tabs">
        <li><s:a href="%{#portofinoConfigurationUrl}">Portofino properties</s:a></li>
        <li><s:a href="%{#elementsConfigurationUrl}">Elements properties</s:a></li>
        <li class="selected"><s:a href="%{#systemConfigurationUrl}">System properties</s:a></li>
    </ul>
</div>
<div id="inner-content">
    <h1>System properties</h1>
    <mdes:write value="form"/>
</div>
<jsp:include page="/skins/default/footer.jsp"/>