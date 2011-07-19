<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><jsp:include page="/skins/default/header.jsp"/>
<div id="inner-content">
    <h1>DDL Create</h1>
    <s:iterator value="ddlsCreate">
            <s:property value="toString()"/><br/>
    </s:iterator>

    <h1>DDL Update</h1>
    <s:iterator value="ddlsUpdate">
            <s:property value="toString()"/><br/>
    </s:iterator>

</div>
<jsp:include page="/skins/default/footer.jsp"/>