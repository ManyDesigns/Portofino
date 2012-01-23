<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld" %>
<jsp:useBean id="actionBean" scope="request"
     type="com.manydesigns.portofino.pageactions.AbstractPageAction"/>
<c:set var="resultSetNavigation" scope="request"
       value="${actionBean.resultSetNavigation}"/>
<c:if test="${not empty resultSetNavigation}">
    <div class="resultSetNavigation">
        <c:if test="${not empty resultSetNavigation.firstUrl}">
            <stripes:link id="first"
                          href="${resultSetNavigation.firstUrl}"><fmt:message key="commons.first" /></stripes:link>
            <stripes:link id="previous"
                          href="${resultSetNavigation.previousUrl}"><fmt:message key="commons.prev" /></stripes:link>
        </c:if>
        <c:if test="${empty resultSetNavigation.firstUrl}">
            <span class="disabled"><fmt:message key="commons.first" /></span>
            <span class="disabled"><fmt:message key="commons.prev" /></span>
        </c:if>
        <c:out value="${resultSetNavigation.position + 1}"/>
        <fmt:message key="commons.of" /> <c:out
            value="${resultSetNavigation.size}"/>
        <c:if test="${not empty resultSetNavigation.lastUrl}">
            <stripes:link id="next"
                          href="${resultSetNavigation.nextUrl}"><fmt:message key="commons.next" /></stripes:link>
            <stripes:link id="last"
                          href="${resultSetNavigation.lastUrl}"><fmt:message key="commons.last" /></stripes:link>
        </c:if>
        <c:if test="${empty resultSetNavigation.lastUrl}">
            <span class="disabled"><fmt:message key="commons.next" /></span>
            <span class="disabled"><fmt:message key="commons.last" /></span>
        </c:if>
    </div>
</c:if>