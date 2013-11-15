<%@ attribute name="id" required="true"
%><%@ attribute name="path" required="true"
%><%@ attribute name="returnUrl" required="false"
%><%@ attribute name="cssClass" required="false"
%><%@ attribute name="anchor" required="false" type="java.lang.Boolean"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ tag import="org.slf4j.LoggerFactory"
%><%@ tag import="com.manydesigns.portofino.dispatcher.PageAction" %>
<c:if test="${empty returnUrl}">
    <jsp:useBean id="actionBean" scope="request"
                 type="com.manydesigns.portofino.pageactions.AbstractPageAction"/>
    <c:set var="returnUrl" value="${actionBean.returnUrl}#${id}" />
</c:if>
<c:if test="${(empty anchor) or anchor}">
    <a name="<c:out value='${id}' />"></a>
</c:if>
<div id="embeddedPageAction_${id}" class="${cssClass}">
    <% try {%>
        <jsp:include page="${path}" flush="false">
            <jsp:param name="returnUrl" value="${returnUrl}" />
        </jsp:include>
    <%} catch (Throwable t) {
        LoggerFactory.getLogger(PageAction.class).error("Error in included page", t);
    %>
        <div class="alert alert-error">
            <button data-dismiss="alert" class="close" type="button">&times;</button>
            <ul class="errorMessages">
                <li>
                    <fmt:message key="this.page.has.thrown.an.exception.during.rendering">
                        <fmt:param value="${path}" />
                    </fmt:message>
                </li>
            </ul>
        </div>
    <%}%>
</div>