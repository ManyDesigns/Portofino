<%@ page import="com.manydesigns.portofino.ApplicationAttributes" %><%@
    page import="com.manydesigns.portofino.context.Application" %><%@
    page import="com.manydesigns.portofino.model.Model" %><%@
    page import="com.manydesigns.portofino.model.pages.Page" %><%@
    page import="com.manydesigns.portofino.model.pages.RootPage" %><%@
    page import="com.manydesigns.portofino.logic.PageLogic" %><%
    ServletContext servletContext = pageContext.getServletContext();
    Application app =
            (Application) servletContext.getAttribute(
                    ApplicationAttributes.APPLICATION);
    Model model = app.getModel();
    RootPage rootPage = model.getRootPage();
    Page landingPage = PageLogic.getLandingPage(rootPage);
    String redirectURL = request.getContextPath() + PageLogic.getPagePath(landingPage);
    response.sendRedirect(redirectURL);
%>