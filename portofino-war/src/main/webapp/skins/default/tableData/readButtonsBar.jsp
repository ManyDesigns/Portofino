<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<div class="buttons-bar">
    <s:set name="position" value="objects.indexOf(object)"/>
    <s:set name="size" value="objects.size()"/>
    <s:if test="#position >= 0">
        <div style="float: right;">
            <s:if test="#position > 0">
                <s:url var="firstUrl" namespace="/" action="%{qualifiedTableName}/TableData">
                    <s:param name="pk"
                             value="%{tableHelper.generatePkString(table, objects.get(0))}" />
                    <s:param name="searchString" value="%{searchString}" />
                </s:url>
                <s:a id="first" href="%{#firstUrl}">first</s:a>
                <s:url var="previousUrl" namespace="/" action="%{qualifiedTableName}/TableData">
                    <s:param name="pk"
                             value="%{tableHelper.generatePkString(table, objects.get(#position-1))}" />
                    <s:param name="searchString" value="%{searchString}" />
                </s:url>
                <s:a id="previous" href="%{#previousUrl}">previous</s:a>
            </s:if><s:else>
                <span class="disabled">first</span> <span class="disabled">previous</span>
            </s:else>
            <s:property value="#position+1"/> of <s:property value="objects.size()"/>
            <s:if test="#position < #size-1">
                <s:url var="nextUrl" namespace="/" action="%{qualifiedTableName}/TableData">
                    <s:param name="pk"
                             value="%{tableHelper.generatePkString(table, objects.get(#position+1))}" />
                    <s:param name="searchString" value="%{searchString}" />
                </s:url>
                <s:a id="next" href="%{#nextUrl}">next</s:a>
                <s:url var="lastUrl" namespace="/" action="%{qualifiedTableName}/TableData">
                    <s:param name="pk"
                             value="%{tableHelper.generatePkString(table, objects.get(#size - 1))}" />
                    <s:param name="searchString" value="%{searchString}" />
                </s:url>
                <s:a id="last" href="%{#lastUrl}">last</s:a>
            </s:if><s:else>
                <span class="disabled">next</span> <span class="disabled">last</span>
            </s:else>
        </div>
    </s:if>
    <s:submit id="Table_returnToSearch" method="returnToSearch" value="<< Return to search"/>
    <s:submit id="Table_edit" method="edit" value="Edit"/>
    <s:submit id="Table_delete" method="delete" value="Delete"
              onclick="return confirm ('Are you sure?');"/>
    <s:submit id="Table_duplicate" method="duplicate" value="Duplicate" disabled="true"/>
    <s:submit id="Table_print" method="print" value="Print" disabled="true"/>
    <s:submit id="Table_export" method="export" value="Export" disabled="true"/>
</div>