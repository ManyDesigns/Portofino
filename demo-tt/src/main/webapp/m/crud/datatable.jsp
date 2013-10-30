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
<div class="portofino-datatable">
    <div class="search_results">
        <mde:write name="actionBean" property="tableForm" />
    </div>
    <div class="pull-left">
        <portofino:buttons list="crud-search" />
    </div>
    <%
        Integer rowsPerPage = actionBean.getCrudConfiguration().getRowsPerPage();
        long totalSearchRecords = rowsPerPage != null ? actionBean.getTotalSearchRecords() : 0;
        if(rowsPerPage != null && totalSearchRecords > rowsPerPage) { %>
            <div class="pagination pagination-right">
                <ul>
                    <% writePaginator(out, actionBean, rowsPerPage, totalSearchRecords); %>
                </ul>
            </div>
    <%  } %>
    <input type="hidden" name="sortProperty" value="${actionBean.sortProperty}" />
    <input type="hidden" name="sortDirection" value="${actionBean.sortDirection}" />
    <input type="hidden" name="eventName" value="${actionBean.context.eventName}" />
    <div style="clear: both;"></div>
</div>
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
            buf.addAttribute("href", actionBean.getLinkToPage(0));
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
            buf.addAttribute("href", actionBean.getLinkToPage(currentPage - 1));
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
            buf.addAttribute("href", actionBean.getLinkToPage(pg));
            buf.addAttribute("title", actionBean.getMessage("commons.pageNumber", (pg + 1)));
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
            buf.addAttribute("href", actionBean.getLinkToPage(currentPage + 1));
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
            buf.addAttribute("href", actionBean.getLinkToPage(lastPage));
        }
        buf.addAttribute("title", actionBean.getMessage("commons.last") + " (" + (lastPage + 1) + ")");
        buf.writeNoHtmlEscape("&gt;&gt;");
        buf.closeElement("a");
        buf.closeElement("li");
    }
%>