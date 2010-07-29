<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<div class="buttons-bar">
    <s:submit id="Table_create" method="create" value="Create new"/>
    <s:submit id="Table_bulkEdit" method="bulkEdit" value="Bulk edit" disabled="true"/>
    <s:submit id="Table_bulkDelete" method="bulkDelete" value="Delete"
              onclick="return confirm ('Are you sure?');"/>
    <s:submit id="Table_print" method="print" value="Print" disabled="true"/>
    <s:submit id="Table_export" method="export" value="Export" disabled="true"/>
</div>