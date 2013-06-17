<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@
    taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %><%@
    page import="com.manydesigns.portofino.dispatcher.PageAction" %><%@
    page import="net.sourceforge.stripes.util.UrlBuilder" %><%@
    page contentType="text/html;charset=UTF-8" language="java"
%><jsp:useBean id="actionBean" scope="request" type="net.sourceforge.stripes.action.ActionBean" />
<c:if test="<%=(actionBean instanceof PageAction) && (((PageAction) actionBean).getReturnToParentTarget() != null)%>">
    <%
        PageAction pageAction = (PageAction) actionBean;
        UrlBuilder urlBuilder =
                new UrlBuilder(request.getLocale(), pageAction.getDispatch().getAbsoluteOriginalPath(), true);
        urlBuilder.addParameters(pageAction.getReturnToParentParams());
        urlBuilder.setEvent("returnToParent");
    %>
    <a href="<%= urlBuilder.toString() %>" class="btn">
        &lt;&lt;
        <fmt:message key="commons.returnToParent">
            <fmt:param value="<%= pageAction.getReturnToParentTarget() %>" />
        </fmt:message>
    </a>
</c:if>