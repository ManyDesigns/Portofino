<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><!DOCTYPE html>
<html>
<head>
    <title>OpenID Authentication - redirecting to your Provider</title>
</head>
<body onload="document.forms['openIDForm'].submit();">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.user.LoginAction"/>
    <form name="openIDForm" action="${actionBean.openIdDestinationUrl}" method="post" accept-charset="utf-8">
        <c:forEach var="param" items="${actionBean.openIdParameterMap}">
            <input type="hidden" name="${param.key}" value="${param.value}"/>
        </c:forEach>
        <button type="submit">Redirection to the selected OpenID provider should happen automatically. If not, click on this button.</button>
    </form>
</body>
</html>