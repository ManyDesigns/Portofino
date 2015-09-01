<%@ page import="net.sourceforge.stripes.util.UrlBuilder" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="com.manydesigns.portofino.pageactions.crud.AbstractCrudAction" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld" %>
<jsp:useBean id="actionBean" scope="request"
             type="com.manydesigns.portofino.pageactions.crud.AbstractCrudAction"/>
<c:set var="resultSetNavigation" scope="request" value="${actionBean.resultSetNavigation}"/>
<c:if test="${not empty resultSetNavigation}">
    <nav style="text-align: center;">
        <ul class="pager" style="display: inline; padding-right: 1em;">
            <c:if test="${not empty resultSetNavigation.firstUrl}">
                <li>
                    <fmt:message key="first" var="pg_title" />
                    <stripes:link href="${resultSetNavigation.firstUrl}" title="${pg_title}"
                                  class="paginator-link">
                        &lt;&lt;
                    </stripes:link>
                </li>
                <li>
                    <fmt:message key="previous" var="pg_title" />
                    <stripes:link href="${resultSetNavigation.previousUrl}" title="${pg_title}"
                                  class="paginator-link">
                        &lt;
                    </stripes:link>
                </li>
            </c:if>
            <c:if test="${empty resultSetNavigation.firstUrl}">
                <li class="disabled"><a class="paginator-link">&lt;&lt;</a></li>
                <li class="disabled"><a class="paginator-link">&lt;</a></li>
            </c:if>

    <%
        UrlBuilder urlBuilder = new UrlBuilder(request.getLocale(), actionBean.getContext().getActionPath(), false);
        if(!StringUtils.isBlank(actionBean.getSearchString())) {
            urlBuilder.addParameter(AbstractCrudAction.SEARCH_STRING_PARAM, actionBean.getSearchString());
        }
        urlBuilder.setEvent("returnToSearch");
    %>

            <li class="active">
                <fmt:message key="return.to.search" var="s_title" />
                <a class="paginator-link" href="<%= urlBuilder.toString() %>" title="${s_title}">
                    <fmt:message key="_.of._">
                        <fmt:param value="${resultSetNavigation.position + 1}" />
                        <fmt:param value="${resultSetNavigation.size}" />
                    </fmt:message>
                </a>
            </li>

            <c:if test="${not empty resultSetNavigation.lastUrl}">
                <li>
                    <fmt:message key="next" var="pg_title" />
                    <stripes:link href="${resultSetNavigation.nextUrl}" title="${pg_title}"
                                  class="paginator-link">
                        &gt;
                    </stripes:link>
                </li>
                <li>
                    <fmt:message key="last" var="pg_title" />
                    <stripes:link href="${resultSetNavigation.lastUrl}" title="${pg_title}"
                                  class="paginator-link">
                        &gt;&gt;
                    </stripes:link>
                </li>
            </c:if>
            <c:if test="${empty resultSetNavigation.lastUrl}">
                <li class="disabled"><a class="paginator-link">&gt;</a></li>
                <li class="disabled"><a class="paginator-link">&gt;&gt;</a></li>
            </c:if>
        </ul>
    </nav>
</c:if>