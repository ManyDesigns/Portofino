<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.CrudAction"/>
<div class="embedded-content">
    <h1><c:out value="${actionBean.crud.searchTitle}"/></h1>
    <div class="search_results">
        <mde:write name="actionBean" property="tableForm"/>
        <stripes:link href="${actionBean.dispatch.originalPath}">&gt;&gt; Advanced search</stripes:link>
    </div>
</div>