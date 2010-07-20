<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<div class="buttons-bar">
    <s:submit method="create" value="Create new"/>
    <s:submit method="bulkDelete" value="Delete" disabled="true"/>
    <s:submit method="bulkEdit" value="Bulk edit" disabled="true"/>
    <s:submit method="print" value="Print" disabled="true"/>
    <s:submit method="export" value="Export" disabled="true"/>
</div>