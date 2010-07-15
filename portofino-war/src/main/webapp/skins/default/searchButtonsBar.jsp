<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<div class="buttons-bar">
    <s:submit method="create" value="Create"/>
    <s:submit method="bulkEdit" disabled="true"/>
    <s:submit method="bulkDelete" disabled="true"/>
    <s:submit method="print" value="Print" disabled="true"/>
    <s:submit method="export" value="Export" disabled="true"/>
</div>