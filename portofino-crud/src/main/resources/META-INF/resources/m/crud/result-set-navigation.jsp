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
    <ul class="pager" style="display: inline;">
        <c:if test="${not empty resultSetNavigation.firstUrl}">
            <li>
                <fmt:message key="first" var="pg_title" />
                <stripes:link href="${resultSetNavigation.firstUrl}" title="${pg_title}"
                              class="paginator-link">
                   <span class="glyphicon glyphicon-menu-left"></span>
                   <span class="glyphicon glyphicon-menu-left g_pag_left"></span>
                </stripes:link>
            </li>
            <li>
                <fmt:message key="previous" var="pg_title" />
                <stripes:link href="${resultSetNavigation.previousUrl}" title="${pg_title}"
                              class="paginator-link">
                    <span class="glyphicon glyphicon-menu-left"></span>
                </stripes:link>
            </li>
        </c:if>
        <c:if test="${empty resultSetNavigation.firstUrl}">
            <li class="disabled">
                <a class="paginator-link">
                    <span class="glyphicon glyphicon-menu-left"></span>
                    <span class="glyphicon glyphicon-menu-left g_pag_left"> </span>
                </a>
            </li>
            <li class="disabled"><a class="paginator-link"><span class="glyphicon glyphicon-menu-left"></span></a></li>
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
            <stripes:link class="paginator-link" href="<%= urlBuilder.toString() %>" title="${s_title}">
                <fmt:message key="_.of._">
                    <fmt:param value="${resultSetNavigation.position + 1}" />
                    <fmt:param value="${resultSetNavigation.size}" />
                </fmt:message>
            </stripes:link>
        </li>

        <c:if test="${not empty resultSetNavigation.lastUrl}">
            <li>
                <fmt:message key="next" var="pg_title" />
                <stripes:link href="${resultSetNavigation.nextUrl}" title="${pg_title}"
                              class="paginator-link">
                   <span class="glyphicon glyphicon-menu-right"></span>
                </stripes:link>
            </li>
            <li>
                <fmt:message key="last" var="pg_title" />
                <stripes:link href="${resultSetNavigation.lastUrl}" title="${pg_title}"
                              class="paginator-link">
                    <span class="glyphicon glyphicon-menu-right g_pag_right"></span>
                    <span class="glyphicon glyphicon-menu-right "></span>
                </stripes:link>
            </li>
        </c:if>
        <c:if test="${empty resultSetNavigation.lastUrl}">
            <li class="disabled"><a class="paginator-link"><span class="glyphicon glyphicon-menu-right"></span> </a></li>
            <li class="disabled">
                <a class="paginator-link">
                    <span class="glyphicon glyphicon-menu-right g_pag_right"></span>
                    <span class="glyphicon glyphicon-menu-right"></span>
                </a>
            </li>
        </c:if>
    </ul>
</c:if>