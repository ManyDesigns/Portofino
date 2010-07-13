<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="mdes" uri="/manydesigns-elements-struts2" %>
<s:include value="/header.jsp"/>
<h1>Search: <s:property value="table.qualifiedName"/></h1>
<mdes:write value="tableForm"/>
<s:include value="/footer.jsp"/>