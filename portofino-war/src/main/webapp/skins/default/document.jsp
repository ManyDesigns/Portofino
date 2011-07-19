<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><jsp:include page="/skins/default/header.jsp"/>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.DocumentAction"/>
<div id="inner-content">
    <c:out value="${actionBean.content}" escapeXml="false"/>
</div>
<jsp:include page="/skins/default/footer.jsp"/>