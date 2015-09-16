<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mde" uri="/manydesigns-elements" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="portofino" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ page import="com.manydesigns.elements.ElementsThreadLocals" %>
<%@ page import="com.manydesigns.elements.xml.XhtmlBuffer" %>
<%@ page import="com.manydesigns.portofino.pageactions.crud.AbstractCrudAction" %>
<%@ page import="java.io.Writer" %>
<%@ page import="java.math.BigInteger" %>
<%@ page import="java.util.Map" %>
<%@ page language="java" %>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.crud.CrudAction"/>
<c:set var="pageId" value="${actionBean.pageInstance.page.id}" />
<c:set var="project" value="${actionBean.project}" />
<div class="portofino-datatable">
    <span class="crud-confirm-bulk-delete" style="display: none;">
        <fmt:message key="are.you.sure" />
    </span>
    <div class="search_results">
        <c:forEach var="version" items="${actionBean.objects}">
            <div class="media">
                <stripes:link class="pull-left" href="/projects/${project.id}/versions/${version.id}">
                    <img class="media-object" alt="project" src="<stripes:url value="/images/placeholder-64x64.png"/>" />
                </stripes:link>
                <div class="media-body">
                    <h4 class="media-heading">
                        <stripes:link href="/projects/${project.id}/versions/${version.id}">
                            <c:out value="${version.title}"/>
                        </stripes:link>
                        <span class="<c:out value="${version.fk_version_state.css_class}"/>"><c:out value="${version.fk_version_state.state}"/></span>
                    </h4>
                    <div>
                        <c:if test="${version.state < 3}">
                            <c:if test="${not empty version.planned_date}">
                                <fmt:message key="planned.date._">
                                    <fmt:param value="${version.planned_date}"/>
                                </fmt:message>
                            </c:if>
                            <c:if test="${empty version.planned_date}">
                                <fmt:message key="no.planned.date"/>
                            </c:if>
                        </c:if>
                        <c:if test="${version.state >= 3}">
                            <c:if test="${not empty version.release_date}">
                                <fmt:message key="release.date._">
                                    <fmt:param value="${version.release_date}"/>
                                </fmt:message>
                            </c:if>
                            <c:if test="${empty version.release_date}">
                                <fmt:message key="no.release.date"/>
                            </c:if>
                        </c:if>
                    </div>
                    <%
                        Map version = (Map)pageContext.getAttribute("version");
                        Long versionId = (Long) version.get("id");
                        int nTickets = ((BigInteger)actionBean.session
                                .createSQLQuery("select count(*) from tickets where fix_version = :versionId")
                                .setLong("versionId", versionId)
                                .uniqueResult()).intValue();
                        int nClosedTickets = ((BigInteger)actionBean.session
                                .createSQLQuery("select count(*) from tickets where state = 4 and fix_version = :versionId")
                                .setLong("versionId", versionId)
                                .uniqueResult()).intValue();
                        int percentage = 0;
                        if (nTickets > 0) {
                            percentage = nClosedTickets * 100 / nTickets;
                        }
                        pageContext.setAttribute("percentage", percentage);
                        pageContext.setAttribute("nClosedTickets", nClosedTickets);
                        pageContext.setAttribute("nTickets", nTickets);
                    %>
                    <div>
                        <c:if test="${nTickets > 0}">
                            <fmt:message key="progress._._.closed.of._">
                                <fmt:param value="${percentage}"/>
                                <fmt:param value="${nClosedTickets}"/>
                                <fmt:param value="${nTickets}"/>
                            </fmt:message>
                        </c:if>
                        <c:if test="${nTickets == 0}">
                        <fmt:message key="progress.no.tickets.in.this.version"/>
                        </c:if>
                    </div>
                </div>
            </div>
        </c:forEach>
    </div>
    <div class="pull-left">
        <portofino:buttons list="crud-search" />
    </div>
    <%
        Integer rowsPerPage = actionBean.getCrudConfiguration().getRowsPerPage();
        long totalSearchRecords = rowsPerPage != null ? actionBean.getTotalSearchRecords() : 0;
        if(rowsPerPage != null && totalSearchRecords > rowsPerPage) { %>
            <ul class="pagination pagination-sm">
                <% writePaginator(out, actionBean, rowsPerPage, totalSearchRecords); %>
            </ul>
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
        buf.addAttribute("title", ElementsThreadLocals.getText("first"));
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
        buf.addAttribute("title", ElementsThreadLocals.getText("previous"));
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
            buf.addAttribute("title", ElementsThreadLocals.getText("page._.of._", pg + 1, lastPage + 1));
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
        buf.addAttribute("title", ElementsThreadLocals.getText("next"));
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
        buf.addAttribute("title", ElementsThreadLocals.getText("last") + " (" + (lastPage + 1) + ")");
        buf.writeNoHtmlEscape("&gt;&gt;");
        buf.closeElement("a");
        buf.closeElement("li");
    }
%>