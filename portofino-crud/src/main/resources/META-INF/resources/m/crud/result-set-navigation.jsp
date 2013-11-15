<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld" %>
<jsp:useBean id="actionBean" scope="request"
     type="com.manydesigns.portofino.pageactions.crud.AbstractCrudAction"/>
<c:set var="resultSetNavigation" scope="request" value="${actionBean.resultSetNavigation}"/>
<c:if test="${not empty resultSetNavigation}">
    <div class="pagination pagination-small" style="display: inline; padding-right: 1em;">
        <ul style="display: inline;">
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
            <li class="active">
                <a class="paginator-link">
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
    </div>
</c:if>