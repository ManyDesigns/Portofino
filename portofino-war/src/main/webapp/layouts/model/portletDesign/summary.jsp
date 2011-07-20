<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><jsp:include page="/skins/default/header.jsp"/>
<s:form method="post">
    <jsp:include page="/skins/default/model/portletDesign/summaryButtonsBar.jsp"/>
    <div id="inner-content">
        <h1>Portlet design summary: <s:property value="portlet.name"/></h1>
        <mdes:write value="form"/>
        <s:hidden name="cancelReturnUrl" value="%{cancelReturnUrl}"/>
        <mdes:write value="bla"/>
        <h2>Display paramaters</h2>
        <mdes:write value="displayForm"/>
        <s:submit name="refresh" value="Refresh"/>
    </div>
    <jsp:include page="/skins/default/model/portletDesign/summaryButtonsBar.jsp"/>
</s:form>
<jsp:include page="/skins/default/footer.jsp"/>