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
%><%@ tag import="com.manydesigns.portofino.pages.Permissions"
%><%@ tag import="com.manydesigns.portofino.security.RequiresPermissions"
%><%@ tag import="net.sourceforge.stripes.action.ActionBean"
%><%@ tag import="org.apache.commons.configuration.Configuration"
%><%@ tag import="org.apache.commons.lang.StringUtils"
%><%@ tag import="org.apache.shiro.SecurityUtils"
%><%@ tag import="org.apache.shiro.subject.Subject"
%><%@ tag import="org.jetbrains.annotations.NotNull"
%><%@ tag import="javax.servlet.jsp.jstl.fmt.LocalizationContext"
%><%@ tag import="java.lang.reflect.Method"
%><%@ tag import="java.util.List"
%>
<%@ tag import="java.util.MissingResourceException" %>
<%@ attribute name="list" required="true"
%><%@ attribute name="cssClass" required="false"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %><%
    ActionBean actionBean = (ActionBean) request.getAttribute("actionBean");
    Subject subject = SecurityUtils.getSubject();

    PageInstance currentPageInstance = null;
    if(actionBean instanceof PageAction) {
        currentPageInstance = ((PageAction) actionBean).getPageInstance();
    }
    List<ButtonInfo> buttons =
            ButtonsLogic.getButtonsForClass(actionBean.getClass(), list);
    ServletContext servletContext = ElementsThreadLocals.getServletContext();
    Configuration configuration =
            (Configuration) servletContext.getAttribute(BaseModule.PORTOFINO_CONFIGURATION);
    if(buttons != null) {
        String group = null;
        XhtmlBuffer buffer = new XhtmlBuffer(out);
        for(ButtonInfo button : buttons) {
            Method handler = button.getMethod();
            boolean isAdmin = SecurityLogic.isAdministrator(request);
            if(!isAdmin &&
               ((currentPageInstance != null && !hasPermissions(configuration, button, currentPageInstance, subject)) ||
                !SecurityLogic.satisfiesRequiresAdministrator(request, actionBean, handler))) {
                continue;
            }
            if(!ButtonsLogic.doGuardsPass(actionBean, handler, GuardType.VISIBLE)) {
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
                    }
                    buffer.openElement("div");
                    buffer.addAttribute("class", "btn-group");
                }
                group = theButton.group();
            } else {
                if(group != null) {
                    buffer.closeElement("div");
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
                    "btn " +
                    theButton.type();
            if(cssClass != null) {
                actualCssClass += cssClass;
            }
            buffer.addAttribute("class", actualCssClass);
            if(hasIcon) {
                buffer.openElement("i");
                buffer.addAttribute("class", theButton.icon());
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
        if(group != null) {
            buffer.closeElement("div");
        }
    }
%><%!
    protected static boolean hasPermissions
            (Configuration conf, @NotNull ButtonInfo button, @NotNull PageInstance pageInstance, @NotNull Subject subject) {
        RequiresPermissions requiresPermissions =
                    SecurityLogic.getRequiresPermissionsAnnotation(button.getMethod(), button.getFallbackClass());
        if(requiresPermissions != null) {
            Permissions permissions = SecurityLogic.calculateActualPermissions(pageInstance);
            return SecurityLogic.hasPermissions
                    (conf, permissions, subject, requiresPermissions);
        } else {
            return true;
        }
    }

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