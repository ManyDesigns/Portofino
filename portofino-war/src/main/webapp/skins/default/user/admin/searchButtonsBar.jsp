<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<s:if test="#buttonsBarBottom">
    <div class="buttons-bar-bottom">
</s:if><s:else>
    <s:set var="buttonsBarBottom" value="true"/>
    <div class="buttons-bar-top">
</s:else>
    <s:submit id="Table_create" method="create" value="Create new"/>
     <s:submit id="Table_bulkDelete" method="bulkDelete" value="Delete"
              onclick="return confirm ('Are you sure?');"/>
    <s:submit id="Table_print" method="print" value="Print" disabled="true"/>
    <s:submit id="Table_export" method="exportSearch" value="Export" disabled="false"/>
</div>