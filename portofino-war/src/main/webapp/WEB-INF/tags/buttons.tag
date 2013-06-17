<%@ tag import="com.manydesigns.elements.xml.XhtmlBuffer" %>
<%@ tag import="com.manydesigns.portofino.buttons.ButtonInfo" %>
<%@ tag import="com.manydesigns.portofino.buttons.ButtonsLogic" %>
<%@ tag import="com.manydesigns.portofino.buttons.GuardType" %>
<%@ tag import="com.manydesigns.portofino.buttons.annotations.Button" %>
<%@ tag import="com.manydesigns.portofino.dispatcher.PageAction" %>
<%@ tag import="com.manydesigns.portofino.dispatcher.PageInstance" %>
<%@ tag import="com.manydesigns.portofino.logic.SecurityLogic" %>
<%@ tag import="net.sourceforge.stripes.action.ActionBean" %>
<%@ tag import="org.apache.commons.lang.StringUtils" %>
<%@ tag import="org.apache.shiro.SecurityUtils" %>
<%@ tag import="org.apache.shiro.subject.Subject" %>
<%@ tag import="javax.servlet.jsp.jstl.fmt.LocalizationContext" %>
<%@ tag import="java.lang.reflect.Method" %>
<%@ tag import="java.util.List" %>
<%@ tag import="java.util.MissingResourceException" %>
<%@ tag import="java.util.UUID" %>
<%@ tag import="org.slf4j.LoggerFactory" %>

<%@ attribute name="list" required="true" %>
<%@ attribute name="cssClass" required="false" %>

<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
    ActionBean actionBean = (ActionBean) request.getAttribute("actionBean");
    Subject subject = SecurityUtils.getSubject();

    PageInstance currentPageInstance = null;
    if(actionBean instanceof PageAction) {
        currentPageInstance = ((PageAction) actionBean).getPageInstance();
    }
    List<ButtonInfo> buttons =
            ButtonsLogic.getButtonsForClass(actionBean.getClass(), list);
    if(buttons != null) {
        boolean primaryFound = false;
        for(ButtonInfo button : buttons) {
            Method handler = button.getMethod();
            boolean isAdmin = SecurityLogic.isAdministrator(request);
            if(!isAdmin &&
               ((currentPageInstance != null && !ButtonsLogic.hasPermissions(button, currentPageInstance, subject)) ||
                !SecurityLogic.satisfiesRequiresAdministrator(request, actionBean, handler))) {
                continue;
            }
            if(!ButtonsLogic.doGuardsPass(actionBean, handler, GuardType.VISIBLE)) {
                continue;
            }
            XhtmlBuffer buffer = new XhtmlBuffer(out);
            Button theButton = button.getButton();
            boolean hasText = !StringUtils.isBlank(theButton.key());
            boolean hasIcon = !StringUtils.isBlank(theButton.icon());
            boolean hasTitle = !StringUtils.isBlank(theButton.titleKey());
            buffer.openElement("button");
            if(!ButtonsLogic.doGuardsPass(actionBean, handler, GuardType.ENABLED)) {
                buffer.addAttribute("disabled", "disabled");
            }
            buffer.addAttribute("name", handler.getName());

            if(hasTitle) {
                %>
                    <fmt:message key="<%= theButton.titleKey() %>" var="__buttonTitle" scope="page" />
                <%
                String title = (String) jspContext.getAttribute("__buttonTitle");
                jspContext.removeAttribute("__buttonTitle");
                buffer.addAttribute("title", title);
            }

            buffer.addAttribute("type", "submit");
            String type = theButton.type();
            String actualCssClass = "btn " + (StringUtils.isBlank(type) ? "" : "btn-" + type + " ");
            if(hasIcon && !hasText) {
                actualCssClass += "btn-mini ";
            }
            if(cssClass != null) {
                actualCssClass += cssClass;
            }
            buffer.addAttribute("class", actualCssClass);
            if(hasIcon) {
                buffer.openElement("i");
                buffer.addAttribute("class", "icon-" + theButton.icon());
                buffer.closeElement("i");
            }
            if(hasText) {
                %>
                    <fmt:message key="<%= theButton.key() %>" var="__buttonValue" scope="page" />
                <%
                String value = (String) jspContext.getAttribute("__buttonValue");
                jspContext.removeAttribute("__buttonValue");
                buffer.write(value);
            }
            buffer.closeElement("button");
            buffer.write(" ");
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