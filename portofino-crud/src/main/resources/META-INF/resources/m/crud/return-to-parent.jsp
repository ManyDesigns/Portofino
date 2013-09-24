<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@ page import="net.sourceforge.stripes.util.UrlBuilder"
%><%@ page contentType="text/html;charset=UTF-8" language="java"
%><jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.crud.AbstractCrudAction" />
<c:if test="${not empty actionBean.returnToParentTarget}">
    <%
        UrlBuilder urlBuilder =
                new UrlBuilder(request.getLocale(), actionBean.getContext().getActualServletPath(), false);
        urlBuilder.addParameters(actionBean.getReturnToParentParams());
        urlBuilder.setEvent("returnToParent");
    %>
    <stripes:link href="<%= urlBuilder.toString() %>" class="btn btn-small">
        &lt;&lt;
        <fmt:message key="commons.returnToParent">
            <fmt:param value="${actionBean.returnToParentTarget}" />
        </fmt:message>
    </stripes:link>
</c:if>