<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@ page import="com.manydesigns.portofino.pageactions.crud.AbstractCrudAction"
%><%@ page import="net.sourceforge.stripes.util.UrlBuilder"
%><%@ page import="org.apache.commons.lang.StringUtils"
%><%@ page contentType="text/html;charset=UTF-8" language="java"
%><jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.crud.AbstractCrudAction" />
<c:if test="${not empty actionBean.object}">
    <%
        UrlBuilder urlBuilder =
                new UrlBuilder(request.getLocale(), actionBean.getContext().getActionPath(), false);
        if(!StringUtils.isBlank(actionBean.getSearchString())) {
            urlBuilder.addParameter(AbstractCrudAction.SEARCH_STRING_PARAM, actionBean.getSearchString());
        }
        urlBuilder.setEvent("returnToSearch");
    %>
    <stripes:link href="<%= urlBuilder.toString() %>" class="btn btn-default btn-sm">
        <span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>
        <fmt:message key="return.to.search" />
    </stripes:link>
</c:if>