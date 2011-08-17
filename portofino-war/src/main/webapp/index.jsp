<%@ page import="com.manydesigns.portofino.context.Application" %>
<%@ page import="com.manydesigns.portofino.ApplicationAttributes" %>
<%@ page import="com.manydesigns.portofino.model.Model" %>
<%@ page import="com.manydesigns.portofino.model.pages.RootPage" %><%
    ServletContext servletContext = pageContext.getServletContext();
    Application app =
            (Application) servletContext.getAttribute(
                    ApplicationAttributes.APPLICATION);
    Model model = app.getModel();
    RootPage rootPage = model.getRootPage();
    String landingPage = rootPage.getLandingPage();
    if (landingPage == null) {
        landingPage = "/" + rootPage.getChildPages().get(0).getId();
    }
    String redirectURL = request.getContextPath() + landingPage;
    response.sendRedirect(redirectURL);
%>
