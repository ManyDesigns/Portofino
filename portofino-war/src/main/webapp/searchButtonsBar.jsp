<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<div class="buttons-bar">
    <s:submit method="create" value="Create" theme="simple"/>
    <s:submit method="bulkEdit" value="Bulk edit" theme="simple"/>
    <s:submit method="print" value="Print" theme="simple"/>
    <s:submit method="export" value="Export" theme="simple"/>
</div>