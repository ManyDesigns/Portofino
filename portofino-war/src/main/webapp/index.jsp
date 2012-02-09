<%@ page import="com.manydesigns.portofino.RequestAttributes" %>
<%@ page import="com.manydesigns.portofino.application.Application" %>
<%@ page import="com.manydesigns.portofino.application.AppProperties" %>
<%
    Application app =
            (Application) request.getAttribute(
                    RequestAttributes.APPLICATION);
    String landingPage = app.getAppConfiguration().getString(AppProperties.LANDING_PAGE);
    String redirectURL = request.getContextPath() + landingPage;
    response.sendRedirect(redirectURL);
%>
