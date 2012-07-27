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
            boolean hasIcon = !StringUtils.isBlank(theButton.icon());
            buffer.openElement("button");
            String id = UUID.randomUUID().toString();
            if(hasIcon) {
                buffer.addAttribute("id", id);
            }
            if(!ButtonsLogic.doGuardsPass(actionBean, handler, GuardType.ENABLED)) {
                buffer.addAttribute("disabled", "disabled");
            }
            buffer.addAttribute("name", handler.getName());
            %>
                <fmt:message key="<%= theButton.key() %>" var="__buttonValue" scope="page" />
            <%
            String value = (String) jspContext.getAttribute("__buttonValue");
            jspContext.removeAttribute("__buttonValue");
            buffer.addAttribute("type", "submit");
            String actualCssClass = "ui-button ui-widget ui-state-default ui-corner-all ";
            if(cssClass != null) {
                actualCssClass += cssClass;
            }
            if(hasIcon) {
                actualCssClass += " ui-button-icon-only";
            } else {
                actualCssClass += " ui-button-text-only";
            }
            buffer.addAttribute("class", actualCssClass);
            if(hasIcon) {
                buffer.openElement("span");
                buffer.addAttribute("class", "ui-button-icon-primary ui-icon " + theButton.icon());
                buffer.closeElement("span");
            }
            buffer.openElement("span");
            buffer.addAttribute("class", "ui-button-text");
            buffer.write(value);
            buffer.closeElement("span");
            buffer.closeElement("button");
            if(hasIcon) {
                %><script type="text/javascript">
                    $(function() {
                        portofino.decorateIconButton("#<%= id %>", "<%= theButton.icon() %>");
                    });
                </script><%
            }
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