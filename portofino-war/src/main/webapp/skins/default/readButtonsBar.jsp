<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<div class="buttons-bar">
    <s:submit method="returnToSearch" value="<< Return to search"/>
    <s:submit method="edit" value="Edit"/>
    <s:submit method="delete" value="Delete" />
    <s:submit method="duplicate" value="Duplicate" disabled="true"/>
    <s:submit method="print" value="Print" disabled="true"/>
    <s:submit method="export" value="Export" disabled="true"/>
</div>