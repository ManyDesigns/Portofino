<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.UseCaseAction"/>
<div class="embedded-content">
    <h1><c:out value="${actionBean.useCase.searchTitle}"/></h1>
    <div class="search_results">
        <mde:write name="actionBean" property="tableForm"/>
        &gt;&gt; Advanced search
    </div>
</div>