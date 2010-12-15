<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<s:if test="#buttonsBarBottom">
    <div class="buttons-bar-bottom">
</s:if><s:else>
    <s:set var="buttonsBarBottom" value="true"/>
    <div class="buttons-bar-top">
</s:else>
    <s:set name="position" value="objects.indexOf(object)"/>
    <s:set name="size" value="objects.size()"/>
    <s:if test="#position >= 0">
        <div style="float: right;">
            <s:if test="#position > 0">
                <s:set name="firstUrl"
                       value="%{pkHelper.generateUrl(objects.get(0), searchString)}" />
                <s:a id="first" href="%{#firstUrl}">first</s:a>
                <s:set name="previousUrl"
                       value="%{pkHelper.generateUrl(objects.get(#position-1), searchString)}" />
                <s:a id="previous" href="%{#previousUrl}">previous</s:a>
            </s:if><s:else>
                <span class="disabled">first</span> <span class="disabled">previous</span>
            </s:else>
            <s:property value="#position+1"/> of <s:property value="objects.size()"/>
            <s:if test="#position < #size-1">
                <s:set name="nextUrl"
                       value="%{pkHelper.generateUrl(objects.get(#position+1), searchString)}" />
                <s:a id="next" href="%{#nextUrl}">next</s:a>
                <s:set name="lastUrl"
                       value="%{pkHelper.generateUrl(objects.get(#size - 1), searchString)}" />
                <s:a id="last" href="%{#lastUrl}">last</s:a>
            </s:if><s:else>
                <span class="disabled">next</span> <span class="disabled">last</span>
            </s:else>
        </div>
    </s:if>
    <s:submit id="Table_returnToSearch" method="returnToSearch" value="<< Return to search"/>
    <s:submit id="Table_edit" name="crud::edit" value="Edit"/>
    <s:submit id="Table_delete" name="crud::delete" value="Delete"
              onclick="return confirm ('Are you sure?');"/>
    <s:submit id="Table_duplicate" name="crud::duplicate" value="Duplicate" disabled="true"/>
    <s:submit id="Table_print" method="print" value="Print" disabled="true"/>
    <s:submit id="Table_exportExcel" method="exportReadExcel" value="Excel" disabled="false"/>
    <s:submit id="Table_exportPdf" method="exportReadPdf" value="Pdf" disabled="false"/>
    <s:iterator var="crudButton" value="crudButtons">
        <s:set var="button" value="#crudButton.button"/>
        <s:submit id="%{#button.name}"
                  method="%{#button.actualMethod}"
                  value="%{#button.label}"
                  disabled="%{!#crudButton.enabled}"/>
    </s:iterator>
</div>