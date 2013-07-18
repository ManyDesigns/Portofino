<%@   page import="com.manydesigns.portofino.AppProperties"
%><%@ page import="com.manydesigns.portofino.modules.BaseModule"
%><%@ page import="org.apache.commons.configuration.Configuration"
%><%
    Configuration configuration =
            (Configuration) request.getServletContext().getAttribute(BaseModule.PORTOFINO_CONFIGURATION);
    String landingPage = configuration.getString(AppProperties.LANDING_PAGE);
    String redirectURL = request.getContextPath() + landingPage;
    response.sendRedirect(redirectURL);
%>
