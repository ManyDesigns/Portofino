<%@ tag import="com.manydesigns.elements.xml.XhtmlBuffer" %>
<%@ tag import="com.manydesigns.portofino.actions.PortletAction" %>
<%@ tag import="com.manydesigns.portofino.actions.RequestAttributes" %>
<%@ tag import="com.manydesigns.portofino.buttons.ButtonInfo" %>
<%@ tag import="com.manydesigns.portofino.buttons.ButtonsLogic" %>
<%@ tag import="com.manydesigns.portofino.logic.SecurityLogic" %>
<%@ tag import="com.manydesigns.portofino.model.pages.Page" %>
<%@ tag import="net.sourceforge.stripes.action.ActionBean" %>
<%@ tag import="org.apache.commons.lang.StringUtils" %>
<%@ tag import="javax.servlet.jsp.jstl.fmt.LocalizationContext" %>
<%@ tag import="java.lang.reflect.Method" %>
<%@ tag import="java.util.List" %>
<%@ tag import="java.util.MissingResourceException" %>
<%@ tag import="com.manydesigns.portofino.buttons.GuardType" %>
<%@ tag import="org.apache.taglibs.standard.tag.common.fmt.BundleSupport" %>

<%@ attribute name="list" required="true" %>
<%@ attribute name="cssClass" required="false" %>

<%@taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
    ActionBean actionBean = (ActionBean) request.getAttribute("actionBean");
    List<String> groups = (List<String>) request.getAttribute(RequestAttributes.GROUPS);

    Page currentPage = null;
    if(actionBean instanceof PortletAction) {
        currentPage = ((PortletAction) actionBean).getPage();
    }
    List<ButtonInfo> buttons =
            ButtonsLogic.getButtonsForClass(actionBean.getClass(), list, groups, currentPage);
    if(buttons != null) {
        for(ButtonInfo button : buttons) {
            Method handler = button.getMethod();
            boolean isAdmin = SecurityLogic.isAdministrator(request);
            if(!isAdmin &&
               ((currentPage != null && !ButtonsLogic.hasPermissions(button, currentPage, groups)) ||
                !SecurityLogic.satisfiesRequiresAdministrator(request, actionBean, handler))) {
                continue;
            }
            if(!ButtonsLogic.doGuardsPass(actionBean, handler, GuardType.VISIBLE)) {
                continue;
            }
            XhtmlBuffer buffer = new XhtmlBuffer(out);
            buffer.openElement("button");
            if(!ButtonsLogic.doGuardsPass(actionBean, handler, GuardType.ENABLED)) {
                buffer.addAttribute("disabled", "disabled");
            }
            buffer.addAttribute("name", handler.getName());
            %>
                <fmt:message key="<%= button.getButton().key() %>" var="__buttonValue" scope="page" />
            <%
            String value = (String) jspContext.getAttribute("__buttonValue");
            if(cssClass != null) {
                buffer.addAttribute("class", cssClass);
            }
            buffer.addAttribute("type", "submit");
            buffer.write(value);
            buffer.closeElement("button");
        }
    }
%>
<%!
    protected String getValue(ButtonInfo button, LocalizationContext localizationContext) {
        String key = button.getButton().key();
        if(!StringUtils.isEmpty(key)) {
            try {
                return localizationContext.getResourceBundle().getString(key);
            } catch (MissingResourceException e) {
                //ignore
            }
        }
        return button.getMethod().getName();
    }
%>