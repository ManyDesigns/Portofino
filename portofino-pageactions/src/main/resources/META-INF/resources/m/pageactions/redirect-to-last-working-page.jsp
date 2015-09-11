<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><%@ page import="com.manydesigns.portofino.dispatcher.PageInstance"
%><%@ page import="com.manydesigns.portofino.dispatcher.DispatcherLogic"
%><%@ page import="org.slf4j.Logger"
%><%@ page import="org.slf4j.LoggerFactory"
%><%@ page import="com.manydesigns.portofino.pageactions.PageActionLogic"
%><%!
    private static Logger logger = LoggerFactory.getLogger("redirect-to-last-working-page.jsp");
%><%

    PageInstance invalidPage =
            (PageInstance) request.getAttribute(DispatcherLogic.INVALID_PAGE_INSTANCE);

    if(invalidPage != null && PageActionLogic.isEmbedded(invalidPage.getActionBean())) {
        request.getRequestDispatcher("/m/pageactions/safemode/safemode.jsp").include(request, response);
        return;
    }

    Integer httpError = (Integer) request.getAttribute("http-error-code");
    response.setStatus(httpError != null ? httpError : 404);

    if(invalidPage == null) {
        return;
    }

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
    
    %><!doctype html>
<html lang="<%= request.getLocale() %>">
    <head>
        <meta http-equiv="refresh" content='<%= "0; " + targetUrl %>' />
    </head>
    <body onload="window.location.href = '<%= targetUrl %>';">
        <p>
            The page you asked for wasn't found, or failed due to an error. You should be automatically be redirected to
            <a href="<%= targetUrl %>"><%= targetUrl %></a>, but if that doesn't happen, follow the link to
            <a href="<%= targetUrl %>"><%= targetUrl %></a>.
        </p>
        <!--
        This is a comment to make this page bigger and make Internet Explorer happy, otherwise it will show its
        built-in 404 page. See for example http://serverfault.com/questions/129008/apaches-404-page-not-shown-in-ie-but-works-in-firefox
        This should really be enough, but to be sure, let's go on for a little while still. Ok, that should do it. Really.
        -->
    </body>
</html>