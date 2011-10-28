<%@ page import="com.manydesigns.elements.reflection.ClassAccessor" %>
<%@ page import="com.manydesigns.elements.reflection.PropertyAccessor" %>
<%@ page import="com.manydesigns.elements.annotations.InSummary" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%@ page import="com.manydesigns.elements.annotations.Label" %>
<%@ page import="com.manydesigns.elements.forms.TableForm" %>
<%@ page import="org.json.JSONWriter" %>
<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<%@taglib prefix="mde" uri="/manydesigns-elements"%>
<stripes:layout-render name="/skins/${skin}/portlet.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.CrudAction"/>
    <stripes:layout-component name="portletTitle">
        <c:out value="${actionBean.crud.searchTitle}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletHeaderButtons">
        <button name="configure" class="wrench">Configure</button>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <c:if test="${not empty actionBean.searchForm}">
            <div class="yui-gc">
                <div class="yui-u first">
                    <div class="search_results withSearchForm">
                        <div id="myMarkedUpContainer">
                            <table id="myTable">
                                <thead>
                                <tr>
                                    <%
                                        TableForm.Column[] columns = actionBean.getTableForm().getColumns();
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
                            var elementsFormatter = function(elCell, oRecord, oColumn, sData) {
                                var href = sData.href;
                                if (href) {
                                    elCell.innerHTML = '<a href="' + htmlEscape(href) + '">' +
                                            htmlEscape(sData.stringValue) +
                                            '</a>';
                                } else {
                                    elCell.innerHTML = sData.stringValue;
                                }
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
                                    boolean first = true;
                                    for (TableForm.Column column : columns) {
                                        PropertyAccessor propertyAccessor = column.getPropertyAccessor();
                                        if (first) {
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
                                        out.print(", formatter : elementsFormatter");
                                        out.print("}");
                                    }
                                %>
                            ];

                            var generateRequest = function(oState, oSelf) {
                                // Get states or use defaults
                                oState = oState || { pagination: null, sortedBy: null };
                                var sort = (oState.sortedBy) ? oState.sortedBy.key : "id";
                                var dir = (oState.sortedBy && oState.sortedBy.dir === YAHOO.widget.DataTable.CLASS_DESC) ? "desc" : "asc";
                                var startIndex = (oState.pagination) ? oState.pagination.recordOffset : 0;
                                var results = (oState.pagination) ? oState.pagination.rowsPerPage : 10;

                                // Build custom request
                                return  "&firstResult=" + startIndex +
                                        "&maxResults=10";
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
                                    "myMarkedUpContainer", myColumnDefs, myDataSource, myConfigs);
                            myDataTable.doBeforeLoadData = function(oRequest, oResponse, oPayload) {
                                oPayload.totalRecords = oResponse.meta.totalRecords;
                                oPayload.pagination.recordOffset = oResponse.meta.startIndex;
                                return oPayload;
                            };
                        </script>
                        <stripes:submit name="create" value="Create new" class="portletButton"/>
                        <stripes:submit name="bulkEdit" value="Edit" class="portletButton"/>
                        <stripes:submit name="bulkDelete" value="Delete"  class="portletButton" onclick="return confirm ('Are you sure?');"/>
                        <stripes:submit name="print" value="Print" disabled="true" class="portletButton"/>
                        <stripes:submit name="exportSearchExcel" value="Excel" class="portletButton" disabled="true"/>
                        <stripes:submit name="exportSearchPdf" value="Pdf" class="portletButton" disabled="true"/>
                    </div>
                    <!-- TODO custom buttons -->
                </div>
                <div class="yui-u">
                        <div class="search_form">
                            <mde:write name="actionBean" property="searchForm"/>
                            <div class="searchFormButtons">
                                <stripes:submit name="search" value="Search" class="portletButton"/>
                                <stripes:submit name="resetSearch" value="Reset form" class="portletButton"/>
                            </div>
                        </div>
                </div>
            </div>
        </c:if><c:if test="${empty actionBean.searchForm}">
            <div class="search_results">
                <mde:write name="actionBean" property="tableForm"/>
                <stripes:submit name="create" value="Create new" class="portletButton"/>
                <stripes:submit name="bulkEdit" value="Edit" class="portletButton"/>
                <stripes:submit name="bulkDelete" value="Delete"  class="portletButton" onclick="return confirm ('Are you sure?');"/>
                <stripes:submit name="print" value="Print" disabled="true" class="portletButton"/>
                <stripes:submit name="exportSearchExcel" value="Excel" class="portletButton" disabled="true"/>
                <stripes:submit name="exportSearchPdf" value="Pdf" class="portletButton" disabled="true"/>
            </div>
            <!-- TODO custom buttons -->
        </c:if>

        <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>

    </stripes:layout-component>
</stripes:layout-render>