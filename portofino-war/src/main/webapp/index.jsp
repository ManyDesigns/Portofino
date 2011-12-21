<%@ page import="com.manydesigns.portofino.actions.RequestAttributes" %>
<%@ page import="com.manydesigns.portofino.application.Application" %>
<%@ page import="com.manydesigns.portofino.logic.PageLogic" %>
<%@ page import="com.manydesigns.portofino.model.Model" %>
<%@ page import="com.manydesigns.portofino.model.pages.Page" %>
<%@ page import="com.manydesigns.portofino.model.pages.RootPage" %>
<%
    Application app =
            (Application) request.getAttribute(
                    RequestAttributes.APPLICATION);
    Model model = app.getModel();
    RootPage rootPage = model.getRootPage();
    Page landingPage = PageLogic.getLandingPage(rootPage);
    String redirectURL = request.getContextPath() + PageLogic.getPagePath(landingPage);
    response.sendRedirect(redirectURL);
%>
