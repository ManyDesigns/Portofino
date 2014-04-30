<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<stripes:layout-render name="/m/admin/admin-theme/admin-page.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.admin.database.TablesAction"/>
    <stripes:layout-component name="pageTitle">
        <fmt:message key="layouts.admin.tables.title">
            <fmt:param value="${actionBean.table.qualifiedName}" />
        </fmt:message>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <style type="text/css">
            #sortable {
                margin-left: 0;
                width: 30%;
            }

            #sortableContainer {
                display: none;
            }

            #sortable li {
                /*font-size: 1.4em;*/
                list-style: none;
                height: 18px;
                margin: 0 3px 3px;
                padding: 0.4em 0.4em 0.4em 1.5em;
            }
        </style>
        <script type="text/javascript">
            $(function() {
                <c:if test="${not empty actionBean.selectedTabId}">
                    $("#tabs a[href=#<c:out value="${actionBean.selectedTabId}" />]").tab("show");
                </c:if>
                var theSortable = $("#sortable");
                theSortable.sortable();
                theSortable.disableSelection();
                var clonedSortable = theSortable.clone();
                function toggleSortable(theSortable) {
                    $("#sortableContainer").toggle();
                    $("#columns").toggle();
                    $(".sortButton").toggle();
                }
                $(".sortButton").click(function() {
                    toggleSortable(theSortable);
                });
                $(".cancelSortButton").click(function() {
                    toggleSortable(theSortable);
                    theSortable.replaceWith(clonedSortable);
                    theSortable = clonedSortable;
                    theSortable.sortable();
                    theSortable.disableSelection();
                    clonedSortable = theSortable.clone();
                });
                $(".confirmSortButton").click(function() {
                    toggleSortable(theSortable);
                    clonedSortable = theSortable.clone();
                });
            });
        </script>
        <stripes:form action="${actionBean.actionPath}" method="post" class="form-horizontal">
            <ul id="tabs" class="nav nav-tabs">
                <li class="active"><a data-toggle="tab" href="#tab-table-columns">
                    <fmt:message key="table.and.columns" />
                </a></li>
                <li><a data-toggle="tab" href="#tab-fk-sp">
                    <fmt:message key="foreign.keys.and.selection.providers" />
                </a></li>
            </ul>
            <div class="tab-content">
                <div id="tab-table-columns" class="tab-pane fade in active">
                    <div style="visibility: hidden; min-height: 0; max-height: 0;">
                        <portofino:buttons list="table-edit" /><%-- So pressing Enter calls Save--%>
                    </div>
                    <br />
                    <mde:write name="actionBean" property="tableForm" />
                    <div class="tableForm">
                        <fieldset>
                            <legend>
                                <fmt:message key="columns" />
                                <button class="btn btn-default btn-sm sortButton"
                                    type="button" role="button" aria-disabled="false">
                                    <fmt:message key="change.order" />
                                </button>
                            </legend>
                            <div style="margin-top: 1em; margin-bottom: 2em;">
                                <div id="columns">
                                    <mde:write name="actionBean" property="columnsTableForm" />
                                </div>
                                <div id="sortableContainer">
                                    <fmt:message key="drag.the.columns.to.change.their.order.then.push.ok" />
                                    <br /><br />
                                    <button class="btn btn-default btn-sm confirmSortButton"
                                            type="button" role="button" aria-disabled="false">
                                        <fmt:message key="ok" />
                                    </button>
                                    <button class="btn  btn-default btn-sm cancelSortButton"
                                            type="button" role="button" aria-disabled="false">
                                        <fmt:message key="cancel" />
                                    </button>
                                    <ul id="sortable">
                                        <c:forEach var="col" items="${actionBean.decoratedColumns}" varStatus="status">
                                            <li class="ui-state-default" id="col_${status.index}">
                                                <c:out value="${col.columnName}" />
                                                <input type="hidden" name="sortedColumnNames[]" value="${col.columnName}" />
                                            </li>
                                        </c:forEach>
                                    </ul>
                                </div>
                            </div>
                        </fieldset>
                    </div>
                </div>
                <div id="tab-fk-sp" class="tab-pane fade in">
                    <div class="tableForm">
                        <fieldset>
                            <legend><fmt:message key="foreign.keys" /></legend>
                            <div>
                                <c:if test="${not empty actionBean.table.foreignKeys}">
                                    <table class="table">
                                        <tr>
                                            <th><fmt:message key="name" /></th>
                                            <th><fmt:message key="property.name.one.side" /></th>
                                            <th><fmt:message key="property.name.many.side" /></th>
                                            <th><fmt:message key="columns" /></th>
                                            <th><fmt:message key="referenced.table" /></th>
                                            <th><fmt:message key="referenced.columns" /></th>
                                        </tr>
                                        <c:forEach items="${actionBean.table.foreignKeys}" var="fk">
                                            <tr>
                                                <td rowspan="${fn:length(fk.references)}"><c:out value="${fk.name}" /></td>
                                                <td rowspan="${fn:length(fk.references)}">
                                                    <input name="fkOnePropertyNames[${fk.name}]" type="text" value="${actionBean.fkOnePropertyNames[fk.name]}"/>
                                                </td>
                                                <td rowspan="${fn:length(fk.references)}">
                                                    <input name="fkManyPropertyNames[${fk.name}]" type="text" value="${actionBean.fkManyPropertyNames[fk.name]}"/>
                                                </td>
                                                <td>
                                                    <a href="<stripes:url value="${actionBean.actionPath}/${fk.references[0].actualFromColumn.columnName}"/>">
                                                        <c:out value="${fk.references[0].actualFromColumn.columnName}" />
                                                    </a>
                                                </td>
                                                <td rowspan="${fn:length(fk.references)}">
                                                    <a href="<stripes:url value="${actionBean.baseActionPath}/${fk.toTable.databaseName}/${fk.toTable.schemaName}/${fk.toTable.tableName}"/>">
                                                        <c:out value="${fk.toTable.tableName}" />
                                                    </a>
                                                </td>
                                                <td>
                                                    <a href="<stripes:url value="${actionBean.baseActionPath}/${fk.toTable.databaseName}/${fk.toTable.schemaName}/${fk.toTable.tableName}/${fk.references[0].actualToColumn.columnName}"/>">
                                                        <c:out value="${fk.references[0].actualToColumn.columnName}" />
                                                    </a>
                                                </td>
                                            </tr>
                                            <c:forEach items="${fk.references}" var="ref" varStatus="status">
                                                <c:if test="${not status.first}">
                                                    <tr>
                                                        <td>
                                                            <a href="<stripes:url value="${actionBean.actionPath}/${ref.actualFromColumn.columnName}"/>">
                                                                <c:out value="${ref.actualFromColumn.columnName}" />
                                                            </a>
                                                        </td>
                                                        <td>
                                                            <a href="<stripes:url value="${actionBean.baseActionPath}/${ref.actualToColumn.databaseName}/${ref.actualToColumn.schemaName}/${ref.actualToColumn.tableName}/${ref.actualToColumn.columnName}"/>">
                                                                <c:out value="${ref.actualToColumn.columnName}" />
                                                            </a>
                                                        </td>
                                                    </tr>
                                                </c:if>
                                            </c:forEach>
                                        </c:forEach>
                                    </table>
                                </c:if>
                                <c:if test="${empty actionBean.table.foreignKeys}">
                                    <p><fmt:message key="none.available" /></p>
                                </c:if>
                            </div>
                        </fieldset>
                    </div>
                    <div class="tableForm">
                        <fieldset>
                            <legend><fmt:message key="selection.providers" /></legend>
                            <div>
                                <c:if test="${not empty actionBean.table.selectionProviders}">
                                    <table class="table">
                                        <tr>
                                            <th><fmt:message key="name" /></th>
                                            <th><fmt:message key="columns" /></th>
                                            <th><fmt:message key="database" /></th>
                                            <th><fmt:message key="query" /></th>
                                        </tr>
                                        <c:forEach items="${actionBean.table.selectionProviders}" var="sp">
                                            <tr>
                                                <td rowspan="${fn:length(sp.references)}">
                                                    <a href="<stripes:url value="${actionBean.actionPath}?editSelectionProvider=&amp;selectionProviderName=${sp.name}&amp;selectedTabId=tab-fk-sp"/>">
                                                        <c:out value="${sp.name}" />
                                                    </a>
                                                </td>
                                                <td>
                                                    <a href="<stripes:url value="${actionBean.actionPath}/${sp.references[0].actualFromColumn.columnName}"/>">
                                                        <c:out value="${sp.references[0].actualFromColumn.columnName}" />
                                                    </a>
                                                </td>
                                                <td rowspan="${fn:length(sp.references)}"><c:out value="${sp.toDatabase}" /></td>
                                                <td rowspan="${fn:length(sp.references)}">
                                                    <c:if test="${not empty sp.hql}">
                                                        <b>HQL:</b> <c:out value="${sp.hql}" /><br />
                                                    </c:if>
                                                    <c:if test="${not empty sp.sql}">
                                                        <b>SQL:</b> <c:out value="${sp.sql}" />
                                                    </c:if>
                                                </td>
                                            </tr>
                                            <c:forEach items="${sp.references}" var="ref" varStatus="status">
                                                <c:if test="${not status.first}">
                                                    <tr>
                                                        <td>
                                                            <a href="<stripes:url value="${actionBean.actionPath}/${ref.actualFromColumn.columnName}"/>">
                                                                <c:out value="${ref.actualFromColumn.columnName}" />
                                                            </a>
                                                        </td>
                                                    </tr>
                                                </c:if>
                                            </c:forEach>
                                        </c:forEach>
                                    </table>
                                </c:if>
                                <c:if test="${empty actionBean.table.selectionProviders}">
                                    <p><fmt:message key="none.available" /></p>
                                </c:if>
                                <div class="form-group">
                                    <div class="col-sm-12">
                                        <portofino:buttons list="table-selection-providers" />
                                    </div>
                                </div>
                            </div>
                        </fieldset>
                    </div>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-12">
                    <portofino:buttons list="table-edit" />
                </div>
            </div>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>