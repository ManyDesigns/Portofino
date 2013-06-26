<%@ page import="net.sourceforge.stripes.controller.ActionResolver" %>
<%@ page import="org.apache.commons.lang.ObjectUtils" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"%>
<%
    pageContext.removeAttribute("adminLinkClass");
    String actionPath = (String) request.getAttribute(ActionResolver.RESOLVED_ACTION);
    String link = ObjectUtils.toString(request.getParameter("link"), "/");
    if(!"/".equals(link) && actionPath.startsWith(link)) {
        pageContext.setAttribute("adminLinkClass", "active");
    }
%>
<li class="${adminLinkClass}">
    <stripes:link href="${param.link}"><c:out value="${param.text}"/></stripes:link>
</li>