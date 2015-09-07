<%@ tag import="com.manydesigns.elements.ElementsThreadLocals"
%><%@ tag import="com.manydesigns.elements.xml.XhtmlBuffer"
%><%@ tag import="com.manydesigns.portofino.buttons.ButtonInfo"
%><%@ tag import="com.manydesigns.portofino.buttons.ButtonsLogic"
%><%@ tag import="com.manydesigns.portofino.buttons.GuardType"
%><%@ tag import="com.manydesigns.portofino.buttons.annotations.Button"
%><%@ tag import="com.manydesigns.portofino.dispatcher.PageAction"
%><%@ tag import="com.manydesigns.portofino.dispatcher.PageInstance"
%><%@ tag import="com.manydesigns.portofino.logic.SecurityLogic"
%><%@ tag import="com.manydesigns.portofino.modules.BaseModule"
%><%@ tag import="net.sourceforge.stripes.action.ActionBean"
%><%@ tag import="org.apache.commons.configuration.Configuration"
%><%@ tag import="org.apache.commons.lang.StringUtils"
%><%@ tag import="org.apache.shiro.SecurityUtils"
%><%@ tag import="org.apache.shiro.subject.Subject"
%><%@ tag import="org.slf4j.Logger"
%><%@ tag import="org.slf4j.LoggerFactory"
%><%@ tag import="java.lang.reflect.Method"
%><%@ tag import="java.util.List"
%><%@ attribute name="list" required="true"
%><%@ attribute name="cssClass" required="false"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %><%
    Logger logger = LoggerFactory.getLogger("buttons.tag");
    logger.debug("Button list: {}", list);
    ActionBean actionBean = (ActionBean) request.getAttribute("actionBean");
    Subject subject = SecurityUtils.getSubject();

    PageInstance currentPageInstance = null;
    if(actionBean instanceof PageAction) {
        logger.trace("actionBean is instance of PageAction");
        currentPageInstance = ((PageAction) actionBean).getPageInstance();
    }
    List<ButtonInfo> buttons =
            ButtonsLogic.getButtonsForClass(actionBean.getClass(), list);
    ServletContext servletContext = ElementsThreadLocals.getServletContext();
    Configuration configuration =
            (Configuration) servletContext.getAttribute(BaseModule.PORTOFINO_CONFIGURATION);
    if(buttons == null) {
        logger.trace("buttons == null");
    } else {
        logger.trace("buttons != null");
        String group = null;
        XhtmlBuffer buffer = new XhtmlBuffer(out);
        for(ButtonInfo button : buttons) {
            logger.trace("ButtonInfo: {}", button);
            Method handler = button.getMethod();
            boolean isAdmin = SecurityLogic.isAdministrator(request);
            if(!isAdmin &&
               ((currentPageInstance != null && !SecurityLogic.hasPermissions(
                       configuration, button.getMethod(), button.getFallbackClass(), currentPageInstance, subject)) ||
                !SecurityLogic.satisfiesRequiresAdministrator(request, actionBean, handler))) {
                continue;
            }
            if(ButtonsLogic.doGuardsPass(actionBean, handler, GuardType.VISIBLE)) {
                logger.trace("Guards passed");
            } else {
                logger.trace("Guards do not pass");
                continue;
            }
            Button theButton = button.getButton();
            boolean hasText = !StringUtils.isBlank(theButton.key());
            boolean hasIcon = !StringUtils.isBlank(theButton.icon());
            boolean hasTitle = !StringUtils.isBlank(theButton.titleKey());
            if(!StringUtils.isBlank(theButton.group())) {
                if(!theButton.group().equals(group)) {
                    if(group != null) {
                        buffer.closeElement("div");
                        buffer.write(" ");
                    }
                    buffer.openElement("div");
                    buffer.addAttribute("class", "btn-group");
                }
                group = theButton.group();
            } else {
                if(group != null) {
                    buffer.closeElement("div");
                    buffer.write(" ");
                }
                group = null;
            }
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
            String actualCssClass =
                    "btn btn-sm" +
                    theButton.type();
            if(cssClass != null) {
                actualCssClass += cssClass;
            }
            buffer.addAttribute("class", actualCssClass);
            if(hasIcon && theButton.iconBefore() ) {
                buffer.openElement("em");
                buffer.addAttribute("class", "glyphicon " + theButton.icon());
                buffer.closeElement("em");
            }
            if(hasText) {
                %>
                    <fmt:message key="<%= theButton.key() %>" var="__buttonValue" scope="page" />
                <%
                String value = (String) jspContext.getAttribute("__buttonValue");
                jspContext.removeAttribute("__buttonValue");
                buffer.write(value);
            }
            if(hasIcon && !theButton.iconBefore()) {
                buffer.openElement("em");
                buffer.addAttribute("class", "glyphicon " + theButton.icon());
                buffer.closeElement("em");
            }
            buffer.closeElement("button");
            buffer.write(" ");
        }
        if(group != null) {
            buffer.closeElement("div");
            buffer.write(" ");
        }
    }
%>