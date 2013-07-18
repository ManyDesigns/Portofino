<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@
    taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %><%@
    page import="net.sourceforge.stripes.util.UrlBuilder" %><%@
    page contentType="text/html;charset=UTF-8" language="java"
%><jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.crud.AbstractCrudAction" />
<c:if test="${not empty actionBean.returnToParentTarget}">
    <%
        UrlBuilder urlBuilder =
                new UrlBuilder(request.getLocale(), actionBean.getDispatch().getAbsoluteOriginalPath(), true);
        urlBuilder.addParameters(actionBean.getReturnToParentParams());
        urlBuilder.setEvent("returnToParent");
    %>
    <a href="<%= urlBuilder.toString() %>" class="btn btn-small">
        &lt;&lt;
        <fmt:message key="commons.returnToParent">
            <fmt:param value="${actionBean.returnToParentTarget}" />
        </fmt:message>
    </a>
</c:if>