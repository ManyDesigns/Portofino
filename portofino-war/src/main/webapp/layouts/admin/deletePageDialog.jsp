<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="com.manydesigns.elements.xml.XhtmlBuffer" %>
<%@ page import="com.manydesigns.portofino.model.pages.Page" %>
<jsp:useBean id="actionBean" scope="request"
             type="com.manydesigns.portofino.actions.admin.PageAdminDialogAction"/>
<div id="dialog-confirm-delete-page" title='<fmt:message key="layouts.admin.deletePageDialog.really_delete"/>'>
    <p><fmt:message key="layouts.admin.deletePageDialog.are_you_sure"/></p>
    <%
        Page portofinoPage = actionBean.getPage();
        if(!portofinoPage.getChildPages().isEmpty()) { %>
            <p><fmt:message key="layouts.admin.deletePageDialog.children"/></p>
            <%= displayPageChildrenAsList(portofinoPage) %>
    <%  } %>
    <input type="hidden" name="deletePage" value="action" />
</div><%!
    private void displayPageChildrenAsList(Page portofinoPage, XhtmlBuffer buf) {
        if(!portofinoPage.getChildPages().isEmpty()) {
            buf.openElement("ul");
            for(Page page : portofinoPage.getChildPages()) {
                buf.openElement("li");
                buf.write(page.getTitle());
                displayPageChildrenAsList(page, buf);
                buf.closeElement("li");
            }
            buf.closeElement("ul");
        }
    }

    private String displayPageChildrenAsList(Page portofinoPage) {
        XhtmlBuffer buf = new XhtmlBuffer();
        displayPageChildrenAsList(portofinoPage, buf);
        return buf.toString();
    }
%>