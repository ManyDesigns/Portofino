<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<s:if test="#buttonsBarBottom">
    <div class="buttons-bar-bottom">
</s:if><s:else>
    <s:set var="buttonsBarBottom" value="true"/>
    <div class="buttons-bar-top">
</s:else>
    <s:submit id="UseCase_create" method="create" value="Create new"/>
    <s:submit id="UseCase_bulkEdit" method="bulkEdit" value="Edit"/>
    <s:submit id="UseCase_bulkDelete" method="bulkDelete" value="Delete"
              onclick="return confirm ('Are you sure?');"/>
    <s:submit id="UseCase_print" method="print" value="Print" disabled="true"/>
    <s:submit id="UseCase_export" method="export" value="Export" disabled="true"/>
</div>