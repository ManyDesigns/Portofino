<%@ page import="com.manydesigns.elements.forms.TableForm" %>
<%@ page import="com.manydesigns.elements.reflection.PropertyAccessor" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%@ page import="org.json.JSONWriter" %>
<%@ page language="java" %>
<c:set var="pageId" value="${actionBean.pageInstance.page.id}" />
<div id="<c:out value="tableContainer-${pageId}" />">
    <table id="<c:out value="table-${pageId}" />">
        <thead>
        <tr>
            <%
                TableForm tableForm = actionBean.getTableForm();
                TableForm.Column[] columns = tableForm.getColumns();
                for (TableForm.Column column : columns) {
                    out.print("<th>");
                    out.print(StringEscapeUtils.escapeHtml(column.getLabel()));
                    out.print("</th>");
                }
            %>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td colspan="<%=columns.length%>">Loading data...</td>
        </tr>
        </tbody>
    </table>
</div>
<script type="text/javascript">
    var initDatatable_<c:out value="${pageId}" /> = function() {
        var elementsFormatter = function(elCell, oRecord, oColumn, sData) {
            var href = sData.href;
            if (href) {
                elCell.innerHTML = '<a href="' + htmlEscape(href) + '">' +
                        htmlEscape(sData.displayValue) +
                        '</a>';
            } else {
                elCell.innerHTML = sData.displayValue;
            }
        };

        var selectionFormatter = function(elCell, oRecord, oColumn, sData) {
            elCell.innerHTML =
                    '<input name="selection" type="checkbox" value="' + htmlEscape(sData) + '" />';
        };

        var myDataSource = new YAHOO.util.DataSource("<c:out value="${actionBean.dispatch.absoluteOriginalPath}?jsonSearchData="/>");
        myDataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;
        myDataSource.connXhrMode = "queueRequests";
        myDataSource.responseSchema = {
            resultsList: "Result",
            fields:
                <%
                    JSONWriter jsonWriter = new JSONWriter(out);
                    jsonWriter.array();
                    jsonWriter.object();
                    jsonWriter.key("key");
                    jsonWriter.value("__rowKey");
                    jsonWriter.endObject();
                    for (TableForm.Column column : columns) {
                        PropertyAccessor propertyAccessor = column.getPropertyAccessor();
                        jsonWriter.object();
                        jsonWriter.key("key");
                        jsonWriter.value(propertyAccessor.getName());
                        jsonWriter.endObject();
                    }
                    jsonWriter.endArray();
                %>,
            metaFields: {
                totalRecords: "totalRecords",
                startIndex: "startIndex"
            }
        };

        var myColumnDefs = [
            <%
                boolean first = false;
                if(tableForm.isSelectable()) {
                    %>{key: '__rowKey', label: '', formatter: selectionFormatter}<%
                } else {
                    first = true;
                }
                for (TableForm.Column column : columns) {
                    PropertyAccessor propertyAccessor = column.getPropertyAccessor();
                    if(first) {
                        first = false;
                    } else {
                        out.print(", ");
                    }
                    out.print("{key : \"");
                    out.print(StringEscapeUtils.escapeJavaScript(propertyAccessor.getName()));
                    out.print("\"");
                    out.print(", label : \"");
                    out.print(StringEscapeUtils.escapeJavaScript(column.getLabel()));
                    out.print("\"");
                    out.print(", formatter : elementsFormatter, sortable : true");
                    out.print("}");
                }
            %>
        ];

        var generateRequest = function(oState, oSelf) {
            // Get states or use defaults
            oState = oState || { pagination: null, sortedBy: null };
            var sort = (oState.sortedBy) ? oState.sortedBy.key : "";
            var dir = (oState.sortedBy && oState.sortedBy.dir === YAHOO.widget.DataTable.CLASS_DESC) ? "desc" : "asc";
            var startIndex = (oState.pagination) ? oState.pagination.recordOffset : 0;
            var results = (oState.pagination) ? oState.pagination.rowsPerPage : 10;

            // Build custom request
            var url = "&firstResult=" + startIndex +
                    "&maxResults=10&sortProperty=" + sort + "&sortDirection=" + dir;
            <c:if test="${not empty actionBean.searchString}">
                url = url + "&searchString=" + encodeURIComponent(
                    '<%= StringEscapeUtils.escapeJavaScript(actionBean.getSearchString()) %>');
            </c:if>
            return url;
        };

        var myConfigs = {
            generateRequest: generateRequest,
            initialRequest: generateRequest(),
            dynamicData: true,
            paginator : new YAHOO.widget.Paginator({
                rowsPerPage: 10
            })
        };

        var myDataTable = new YAHOO.widget.DataTable(
                "<c:out value="tableContainer-${pageId}" />", myColumnDefs, myDataSource, myConfigs);
        myDataTable.doBeforeLoadData = function(oRequest, oResponse, oPayload) {
            oPayload.totalRecords = oResponse.meta.totalRecords;
            oPayload.pagination.recordOffset = oResponse.meta.startIndex;
            return oPayload;
        };
        myDataTable.doBeforePaginatorChange = function(oPaginatorState) {
            //Disable loading message
            return true;
        };
    }();

</script>