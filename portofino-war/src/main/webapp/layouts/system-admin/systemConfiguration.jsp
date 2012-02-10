<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="/skins/default/header.jsp"/>
<div class="tabs-bar-top">
    <s:url var="portofinoConfigurationUrl" method="portofinoConfiguration"/>
    <s:url var="elementsConfigurationUrl" method="elementsConfiguration"/>
    <s:url var="systemConfigurationUrl" method="systemConfiguration"/>
    <ul class="tabs">
        <li><s:a href="%{#portofinoConfigurationUrl}"><fmt:message key="layouts.system-admin.portofino_properties"/></s:a></li>
        <li><s:a href="%{#elementsConfigurationUrl}"><fmt:message key="layouts.system-admin.elements_properties"/></s:a></li>
        <li class="selected"><s:a href="%{#systemConfigurationUrl}"><fmt:message key="layouts.system-admin.system_properties"/></s:a></li>
    </ul>
</div>
<div id="inner-content">
    <h1><fmt:message key="layouts.system-admin.system_properties"/></h1>
    <mdes:write value="form"/>
</div>
<jsp:include page="/skins/default/footer.jsp"/>