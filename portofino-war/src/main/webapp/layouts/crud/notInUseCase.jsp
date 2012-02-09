<%@ page import="com.manydesigns.portofino.dispatcher.Dispatch" %>
<%@ page import="com.manydesigns.portofino.RequestAttributes" %>
<%@ page import="com.manydesigns.portofino.interceptors.ApplicationInterceptor" %>
<%@ page import="com.manydesigns.portofino.dispatcher.PageInstance" %>
<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><%

    response.setStatus(404);

    // Avoid caching of dynamic pages
    response.setHeader("Pragma", "no-cache");
    response.addHeader("Cache-Control", "must-revalidate");
    response.addHeader("Cache-Control", "no-cache");
    response.addHeader("Cache-Control", "no-store");
    response.setDateHeader("Expires", 0);

    PageInstance invalidPage =
            (PageInstance) request.getAttribute(ApplicationInterceptor.INVALID_PAGE_INSTANCE);
    invalidPage.getParameters().clear();

    String targetUrl = request.getContextPath() + invalidPage.getPath();
    response.setHeader("Refresh", "0; " + targetUrl);
    
    %><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en">
    <head>
        <meta http-equiv="refresh" content='<%= "0; " + targetUrl %>' />
    </head>
    <body onload="window.location.href = '<%= targetUrl %>';"></body>
</html>