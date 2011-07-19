<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><jsp:include page="/skins/default/header.jsp"/>
<s:form method="post">
    <jsp:include page="/skins/default/model/connectionProviders/listButtonsBar.jsp"/>
    <div id="inner-content">
        <h1>Connection providers</h1>
        <mdes:write value="tableForm"/>
        <h2>Available database platforms</h2>
        <mdes:write value="databasePlatformsTableForm"/>
    </div>
    <jsp:include page="/skins/default/model/connectionProviders/listButtonsBar.jsp"/>
</s:form>
<jsp:include page="/skins/default/footer.jsp"/>