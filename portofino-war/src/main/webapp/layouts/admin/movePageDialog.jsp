<%@ page import="com.manydesigns.elements.xml.XhtmlBuffer" %>
<%@ page import="com.manydesigns.portofino.model.pages.Page" %>
<jsp:useBean id="actionBean" scope="request"
             type="com.manydesigns.portofino.actions.PageAction"/>
<div id="dialog-move-page" title="Move to...">
    <p>Choose where to move this page:</p>
    <%
        Page portofinoPage = actionBean.getPage();
        if(!portofinoPage.getChildPages().isEmpty()) { %>
            <p>Deleting it will also delete its children:</p>
            <%= displayPageChildrenAsList(portofinoPage) %>
    <%  } %>
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