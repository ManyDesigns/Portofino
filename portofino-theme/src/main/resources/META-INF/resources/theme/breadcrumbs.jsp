<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@
        taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %><%@
        page import="com.manydesigns.portofino.breadcrumbs.BreadcrumbItem" %><%@
        page import="com.manydesigns.portofino.breadcrumbs.Breadcrumbs" %><%@
        page import="com.manydesigns.portofino.dispatcher.Dispatch" %><%@
        page import="com.manydesigns.portofino.dispatcher.DispatcherUtil" %><%@
        page import="java.util.List" %><%@
        page contentType="text/html;charset=UTF-8" language="java"
        %><jsp:useBean id="actionBean" scope="request" type="net.sourceforge.stripes.action.ActionBean" />
<%
    Dispatch dispatch = DispatcherUtil.getDispatch(request, actionBean);
    Breadcrumbs breadcrumbs = new Breadcrumbs(dispatch);
    List<BreadcrumbItem> items = breadcrumbs.getItems();
    if (items.size()>1) {
%>

<div class="pull-left">
    <ol class="breadcrumb">
        <%

            for (int i = 0; i < items.size(); i++) {
                BreadcrumbItem current = items.get(i);
                pageContext.setAttribute("current", current);
                boolean last = i == items.size() - 1;
                if (last) {
        %>
        <li class="active" title="<c:out value='${current.title}' />" ><c:out value='${current.text}' /></li>
        <%
        } else {
        %>
        <li><a
                href="<c:out value='${pageContext.request.contextPath}${current.href}' />"
                title="<c:out value='${current.title}' />"
                ><c:out value='${current.text}' /></a></li>
        <%
                }
            }
        %>
    </ol>
</div>
<%
    }

%>
