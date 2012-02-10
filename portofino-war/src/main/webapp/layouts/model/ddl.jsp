<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="/skins/default/header.jsp"/>
<div id="inner-content">
    <h1><fmt:message key="layouts.model.ddl.create"/></h1>
    <s:iterator value="ddlsCreate">
            <s:property value="toString()"/><br/>
    </s:iterator>

    <h1><fmt:message key="layouts.model.ddl.update"/></h1>
    <s:iterator value="ddlsUpdate">
            <s:property value="toString()"/><br/>
    </s:iterator>

</div>
<jsp:include page="/skins/default/footer.jsp"/>