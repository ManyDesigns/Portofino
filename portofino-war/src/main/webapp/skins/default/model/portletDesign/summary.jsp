<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="s" uri="/struts-tags"
%><%@ taglib prefix="mdes" uri="/manydesigns-elements-struts2"
%><s:include value="/skins/default/header.jsp"/>
<s:form method="post">
    <s:include value="/skins/default/model/portletDesign/summaryButtonsBar.jsp"/>
    <div id="inner-content">
        <h1>Portlet design summary: <s:property value="portlet.name"/></h1>
        <mdes:write value="form"/>
        <s:hidden name="cancelReturnUrl" value="%{cancelReturnUrl}"/>
        <mdes:write value="bla"/>
        <h2>Display paramaters</h2>
        <mdes:write value="displayForm"/>
        <s:submit name="refresh" value="Refresh"/>
    </div>
    <s:include value="/skins/default/model/portletDesign/summaryButtonsBar.jsp"/>
</s:form>
<s:include value="/skins/default/footer.jsp"/>