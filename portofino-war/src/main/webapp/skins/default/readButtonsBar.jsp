<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<div class="buttons-bar">
    <s:submit id="Table_returnToSearch" method="returnToSearch" value="<< Return to search"/>
    <s:submit id="Table_edit" method="edit" value="Edit"/>
    <s:submit id="Table_delete" method="delete" value="Delete" />
    <s:submit id="Table_duplicate" method="duplicate" value="Duplicate" disabled="true"/>
    <s:submit id="Table_print" method="print" value="Print" disabled="true"/>
    <s:submit id="Table_export" method="export" value="Export" disabled="true"/>
</div>