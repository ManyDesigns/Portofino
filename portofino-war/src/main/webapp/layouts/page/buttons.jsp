<%@ page import="com.manydesigns.elements.xml.XhtmlBuffer" %>
<%@ page import="com.manydesigns.portofino.actions.PortletAction" %>
<%@ page import="com.manydesigns.portofino.actions.RequestAttributes" %>
<%@ page import="com.manydesigns.portofino.buttons.ButtonInfo" %>
<%@ page import="com.manydesigns.portofino.buttons.ButtonsLogic" %>
<%@ page import="com.manydesigns.portofino.model.pages.Page" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.apache.taglibs.standard.tag.common.fmt.BundleSupport" %>
<%@ page import="javax.servlet.jsp.jstl.fmt.LocalizationContext" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.MissingResourceException" %>
<%@ page import="com.manydesigns.portofino.logic.SecurityLogic" %>
<%@taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld" %>
<stripes:layout-definition>
<%
    LocalizationContext localizationContext = BundleSupport.getLocalizationContext(pageContext);

    Object actionBean = request.getAttribute("actionBean");
    String list = (String) pageContext.getAttribute("list");
    List<String> groups = (List<String>) request.getAttribute(RequestAttributes.GROUPS);
    String cssClass = (String) pageContext.getAttribute("cssClass");

    Page currentPage = null;
    if(actionBean instanceof PortletAction) {
        currentPage = ((PortletAction) actionBean).getPage();
    }
    List<ButtonInfo> buttons =
            ButtonsLogic.getButtonsForClass(actionBean.getClass(), list, groups, currentPage);
    if(buttons != null) {
        for(ButtonInfo button : buttons) {
            boolean isAdmin = SecurityLogic.isAdministrator(request);
            if(currentPage != null && !isAdmin && !ButtonsLogic.hasPermissions(button, currentPage, groups)) {
                continue;
            }
            XhtmlBuffer buffer = new XhtmlBuffer(out);
            buffer.openElement("button");
            buffer.addAttribute("name", button.getMethod().getName());
            String value = getValue(button, localizationContext);
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
</stripes:layout-definition>