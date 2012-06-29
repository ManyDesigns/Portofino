<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><%@ page import="com.manydesigns.portofino.dispatcher.PageInstance"
%><%@ page import="com.manydesigns.portofino.interceptors.ApplicationInterceptor"
%><%@ page import="org.slf4j.Logger"
%><%@ page import="org.slf4j.LoggerFactory" %><%!
    private static Logger logger = LoggerFactory.getLogger("redirect-to-last-working-page.jsp");
%><%

    PageInstance invalidPage =
            (PageInstance) request.getAttribute(ApplicationInterceptor.INVALID_PAGE_INSTANCE);

    if(invalidPage.getActionBean().isEmbedded()) {
        request.getRequestDispatcher("/layouts/safemode/safemode.jsp").include(request, response);
        return;
    }

    response.setStatus(404);

    // Avoid caching of dynamic pages
    response.setHeader("Pragma", "no-cache");
    response.addHeader("Cache-Control", "must-revalidate");
    response.addHeader("Cache-Control", "no-cache");
    response.addHeader("Cache-Control", "no-store");
    response.setDateHeader("Expires", 0);

    if(!invalidPage.getParameters().isEmpty()) {
        logger.debug("Page instance with parameters failed, trying without parameters");
        invalidPage.getParameters().clear();
    } else {
        logger.debug("Page instance without parameters failed, trying parent");
        invalidPage = invalidPage.getParent();
        if(invalidPage == null || invalidPage.getParent() == null) {
            logger.debug("Parent is null, aborting");
            response.sendError(500, "A fatal error occurred: all pages in path failed initialization.");
            // TODO: i18n, gestire root/landing page; redirigere su SafeModeAction? Come?
            return;
        }
    }

    String targetUrl = request.getContextPath() + invalidPage.getPath();
    response.setHeader("Refresh", "0; " + targetUrl);
    
    %><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en">
    <head>
        <meta http-equiv="refresh" content='<%= "0; " + targetUrl %>' />
    </head>
    <body onload="window.location.href = '<%= targetUrl %>';"></body>
</html>