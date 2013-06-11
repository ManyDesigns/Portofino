<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mde" uri="/manydesigns-elements" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="portofino" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="com.manydesigns.elements.xml.XhtmlBuffer" %>
<%@ page import="com.manydesigns.portofino.pageactions.crud.AbstractCrudAction" %>
<%@ page import="net.sourceforge.stripes.util.UrlBuilder" %>
<%@ page import="java.io.Writer" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page language="java" %>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.crud.AbstractCrudAction"/>
<c:set var="pageId" value="${actionBean.pageInstance.page.id}" />
<div id="datatable-${pageId}" class="portofino-datatable">
    <mde:write name="actionBean" property="tableForm" />
    <div class="pull-left">
        <portofino:buttons list="crud-search" cssClass="portletButton" />
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
    <script type="text/javascript">
        $("#portlet_${pageId} .search_results button[name=bulkDelete]").click(function() {
            return confirm ('<fmt:message key="commons.confirm" />');
        });
    </script>
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
        Map<String, Object> parameters = new HashMap<String, Object>(actionBean.getContext().getRequest().getParameterMap());
        parameters.put("sortProperty", actionBean.getSortProperty());
        parameters.put("sortDirection", actionBean.getSortDirection());
        parameters.put("firstResult", page * rowsPerPage);
        parameters.put("maxResults", rowsPerPage);
        parameters.put(AbstractCrudAction.SEARCH_STRING_PARAM, actionBean.getSearchString());

        UrlBuilder urlBuilder =
                new UrlBuilder(Locale.getDefault(), actionBean.getDispatch().getAbsoluteOriginalPath(), false)
                        .addParameters(parameters);
        return urlBuilder.toString();
    }
%>