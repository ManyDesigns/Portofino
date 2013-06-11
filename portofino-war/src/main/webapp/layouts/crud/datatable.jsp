<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mde" uri="/manydesigns-elements" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="portofino" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="com.manydesigns.elements.xml.XhtmlBuffer" %>
<%@ page import="com.manydesigns.portofino.pageactions.crud.AbstractCrudAction" %>
<%@ page import="java.io.Writer" %>
<%@ page language="java" %>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.crud.AbstractCrudAction"/>
<c:set var="pageId" value="${actionBean.pageInstance.page.id}" />
<div id="tableContainer-${pageId}">
    <mde:write name="actionBean" property="tableForm" />
    <c:if test="${empty actionBean.crudConfiguration.rowsPerPage}">
        <div style="line-height: 8px;">&nbsp;<%-- Reserve space for the missing paginator --%></div>
    </c:if>
    <div class="pull-left">
        <portofino:buttons list="crud-search" cssClass="portletButton" />
    </div>
    <c:if test="${not empty actionBean.crudConfiguration.rowsPerPage}">
        <%
            int rowsPerPage = actionBean.getCrudConfiguration().getRowsPerPage();
            long totalSearchRecords = actionBean.getTotalSearchRecords();
            if(totalSearchRecords <= rowsPerPage) {
                %><div style="line-height: 8px;">&nbsp;<%-- Reserve space for the missing paginator --%></div><%
            } else { %>
                <div class="pagination pagination-right" data-for="tableContainer-${pageId}">
                    <ul>
                        <% writePaginator(out, actionBean, rowsPerPage, totalSearchRecords); %>
                    </ul>
                </div>
        <%  } %>
    </c:if>
    <input type="hidden" name="sortProperty" value="${actionBean.sortProperty}" />
    <input type="hidden" name="sortDirection" value="${actionBean.sortDirection}" />
    <script type="text/javascript">
        $("#portlet_${pageId} .search_results button[name=bulkDelete]").click(function() {
            return confirm ('<fmt:message key="commons.confirm" />');
        });
    </script>
    <div style="clear: both;"></div>
</div>
<%--
<!--<input type="hidden" name="firstResult" value="${actionBean.firstResult}" />
<input type="hidden" name="maxResults" value="${actionBean.maxResults}" />-->
<script type="text/javascript">
    var initDatatable_<c:out value="${pageId}" /> = function() {
        var elementsFormatter = function(elCell, oRecord, oColumn, sData) {
            if(sData.displayValue) { //Handle null value
                var href = sData.href;
                if (href) {
                    elCell.innerHTML = '<a href="' + htmlEscape(href) + '">' +
                            sData.displayValue +
                            '</a>';
                } else {
                    elCell.innerHTML = sData.displayValue;
                }
            }
        };

        var selectionFormatter = function(elCell, oRecord, oColumn, sData) {
            elCell.innerHTML =
                    '<input name="selection" type="checkbox" value="' + htmlEscape(sData) + '" />';
        };

        var myDataSource = new YAHOO.util.DataSource("<c:out value="${actionBean.dispatch.absoluteOriginalPath}?jsonSearchData="/>");
        myDataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;
        myDataSource.connMethodPost = true;
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
                    out.print(StringEscapeUtils.escapeJavaScript(column.getActualLabel()));
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
            var firstResult = (oState.pagination) ? oState.pagination.recordOffset : 0;
            var maxResults = null;
            <c:if test="${not empty actionBean.crudConfiguration.rowsPerPage}">
                maxResults = (oState.pagination) ? oState.pagination.rowsPerPage : ${actionBean.crudConfiguration.rowsPerPage};
            </c:if>

            // Build custom request
            var url = "&firstResult=" + firstResult + "&sortProperty=" + sort + "&sortDirection=" + dir;
            if(maxResults) {
                url += "&maxResults=" + maxResults;
            }
            <c:if test="${(not actionBean.embedded) && (not empty actionBean.searchString)}">
                url = url + "&searchString=" + encodeURIComponent(
                    '<%= StringEscapeUtils.escapeJavaScript(crudAction.getSearchString()) %>');
            </c:if>

            //Update pagination & sort input fields in the form
            $('#<c:out value="portlet_${pageId}" /> input[name=sortProperty]').val(sort);
            $('#<c:out value="portlet_${pageId}" /> input[name=sortDirection]').val(dir);
            //$('#<c:out value="portlet_${pageId}" /> input[name=firstResult]').val(firstResult);
            //$('#<c:out value="portlet_${pageId}" /> input[name=maxResults]').val(maxResults);

            return url;
        };

        var firstReqConf = {};
        <% if(crudAction.getFirstResult() != null) { %>
            firstReqConf['pagination'] = {
                recordOffset: <%= crudAction.getFirstResult() %>,
                rowsPerPage:  <%= crudAction.getMaxResults() %>
            };
        <% } %>

        var myConfigs = {
            generateRequest: generateRequest,
            initialRequest: generateRequest(firstReqConf),
            dynamicData: true,
            MSG_EMPTY: '<fmt:message key="layouts.crud.datatable.msg_empty"/>'
        };

        <c:if test="${not empty actionBean.crudConfiguration.rowsPerPage}">
            myConfigs.paginator = new YAHOO.widget.Paginator({
                rowsPerPage: <%= crudAction.getCrudConfiguration().getRowsPerPage() %>,
                firstPageLinkLabel: '&lt;&lt; <fmt:message key="commons.first" />',
                previousPageLinkLabel: '&lt; <fmt:message key="commons.prev" />',
                nextPageLinkLabel: '<fmt:message key="commons.next" /> &gt;',
                lastPageLinkLabel: '<fmt:message key="commons.last" /> &gt;&gt;'
            });
        </c:if>

        var myDataTable = new YAHOO.widget.DataTable(
                '<c:out value="tableContainer-${pageId}" />', myColumnDefs, myDataSource, myConfigs);
        myDataTable.doBeforeLoadData = function(oRequest, oResponse, oPayload) {
            oPayload.totalRecords = oResponse.meta.totalRecords;
            if(oPayload.pagination) {
                oPayload.pagination.recordOffset = oResponse.meta.startIndex;
            }
            return oPayload;
        };
        myDataTable.doBeforePaginatorChange = function(oPaginatorState) {
            //Disable loading message
            return true;
        };
        myDataTable.doBeforeSortColumn = myDataTable.doBeforePaginatorChange;
    }();

</script>
--%>
<%!
    private void writePaginator(Writer out, AbstractCrudAction actionBean, int rowsPerPage, long totalSearchRecords) {
        int firstResult = actionBean.getFirstResult() != null ? actionBean.getFirstResult() : 1;
        int currentPage = firstResult / rowsPerPage;
        int lastPage = (int) (totalSearchRecords / rowsPerPage);
        if(totalSearchRecords % rowsPerPage == 0) {
            lastPage--;
        }

        XhtmlBuffer buf = new XhtmlBuffer(out);

        //First
        buf.openElement("li");
        if(currentPage == 0) {
            buf.addAttribute("class", "disabled");
            buf.openElement("a");
        } else {
            buf.openElement("a");
            buf.addAttribute("class", "paginator-link");
            buf.addAttribute("href", getLinkToPage(actionBean, 0));
        }
        buf.addAttribute("title", actionBean.getMessage("commons.first"));
        buf.writeNoHtmlEscape("&lt;&lt;");
        buf.closeElement("a");
        buf.closeElement("li");

        //Prev
        buf.openElement("li");
        if(currentPage == 0) {
            buf.addAttribute("class", "disabled");
            buf.openElement("a");
        } else {
            buf.openElement("a");
            buf.addAttribute("class", "paginator-link");
            buf.addAttribute("href", getLinkToPage(actionBean, currentPage - 1));
        }
        buf.addAttribute("title", actionBean.getMessage("commons.prev"));
        buf.writeNoHtmlEscape("&lt;");
        buf.closeElement("a");
        buf.closeElement("li");

        int start = currentPage - 2;
        int end = currentPage + 2;
        if(start < 0) {
            end = end - start;
            start = 0;
        }
        if(end > lastPage) {
            start = start - (end - lastPage);
            end = lastPage;
        }
        if(start < 0) {
            start = 0;
        }

        for(int pg = start; pg <= end; pg++) {
            buf.openElement("li");
            if(pg == currentPage) {
                buf.addAttribute("class", "active");
            }
            buf.openElement("a");
            buf.addAttribute("class", "paginator-link");
            buf.addAttribute("href", getLinkToPage(actionBean, pg));
            buf.addAttribute("title", "Page " + (pg + 1)); //TODO I18n
            buf.write("" + (pg + 1));
            buf.closeElement("a");
            buf.closeElement("li");
        }

        //Next
        buf.openElement("li");
        if(currentPage == lastPage) {
            buf.addAttribute("class", "disabled");
            buf.openElement("a");
        } else {
            buf.openElement("a");
            buf.addAttribute("class", "paginator-link");
            buf.addAttribute("href", getLinkToPage(actionBean, currentPage + 1));
        }
        buf.addAttribute("title", actionBean.getMessage("commons.next"));
        buf.writeNoHtmlEscape("&gt;");
        buf.closeElement("a");
        buf.closeElement("li");

        //Last
        buf.openElement("li");
        if(currentPage == lastPage) {
            buf.addAttribute("class", "disabled");
            buf.openElement("a");
        } else {
            buf.openElement("a");
            buf.addAttribute("class", "paginator-link");
            buf.addAttribute("href", getLinkToPage(actionBean, lastPage));
        }
        buf.addAttribute("title", actionBean.getMessage("commons.last"));
        buf.writeNoHtmlEscape("&gt;&gt;");
        buf.closeElement("a");
        buf.closeElement("li");
    }

    private String getLinkToPage(AbstractCrudAction actionBean, int page) {
        int rowsPerPage = actionBean.getCrudConfiguration().getRowsPerPage();
        return actionBean.getDispatch().getAbsoluteOriginalPath() +
                "?firstResult=" + (page * rowsPerPage) +
                "&maxResults=" + rowsPerPage;
    }
%>