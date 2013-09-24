<%@ page session="false"
%><%@ page import="com.manydesigns.portofino.modules.BaseModule"
%><%@ page import="org.apache.commons.configuration.Configuration"
%><%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="com.manydesigns.portofino.PortofinoProperties" %><%
    Configuration configuration =
            (Configuration) request.getServletContext().getAttribute(BaseModule.PORTOFINO_CONFIGURATION);
    String landingPage = configuration.getString(PortofinoProperties.LANDING_PAGE);
    if(landingPage == null) {
        LoggerFactory.getLogger(getClass()).error("Landing page not configured");
        response.sendError(404, "Landing page not configured!");
    } else {
        String redirectURL = request.getContextPath() + landingPage;
        response.sendRedirect(redirectURL);
    }
%>
