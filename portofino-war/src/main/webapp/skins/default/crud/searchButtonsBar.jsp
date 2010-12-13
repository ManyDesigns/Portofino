<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<s:if test="#buttonsBarBottom">
    <div class="buttons-bar-bottom">
</s:if><s:else>
    <s:set var="buttonsBarBottom" value="true"/>
    <div class="buttons-bar-top">
</s:else>
    <s:submit id="Table_create" name="crud::create" value="Create new"/>
    <s:submit id="Table_bulkEdit" name="crud::bulkEdit" value="Edit"/>
    <s:submit id="Table_bulkDelete" name="crud::bulkDelete" value="Delete"
              onclick="return confirm ('Are you sure?');"/>
    <s:submit id="Table_print" method="print" value="Print" disabled="true"/>
    <s:submit id="Table_exportExcel" method="exportSearchExcel" value="Excel" disabled="false"/>
    <s:submit id="Table_exportPdf" method="exportSearchPdf" value="Pdf" disabled="false"/>
    <s:iterator var="button" value="buttons">
        <s:submit id="%{#button.name}" method="%{#button.actualMethod}" value="%{#button.label}"/>
    </s:iterator>
</div>