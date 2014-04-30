<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="com.manydesigns.elements.xml.XhtmlBuffer" %>
<%@ page import="com.manydesigns.portofino.dispatcher.PageInstance" %>
<%@ page import="com.manydesigns.portofino.pages.ChildPage" %>
<%@ page import="com.manydesigns.portofino.pages.Page" %>
<%@ page import="java.util.List" %>
<jsp:useBean id="actionBean" scope="request"
             type="com.manydesigns.portofino.actions.admin.page.PageAdminAction"/>
<div class="dialog-confirm-delete-page modal" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button name="closeDeletePageButton" type="button" class="close" aria-hidden="true">&times;</button>
                <h4 class="modal-title"><fmt:message key="really.delete"/></h4>
            </div>
            <div class="modal-body">
                <p><fmt:message key="are.you.sure.you.want.to.delete.this.page"/></p>
                <%
                    PageInstance pageInstance = actionBean.getPageInstance();
                    Page pg = pageInstance.getPage();
                    if(!pg.getLayout().getChildPages().isEmpty() ||
                       !pg.getDetailLayout().getChildPages().isEmpty()) { %>
                        <p><fmt:message key="deleting.it.will.also.delete.its.children"/></p><%
                        out.print(displayPageChildrenAsList(pg));
                    }
                %>
                <input type="hidden" name="deletePage" value="action" />
            </div>
            <div class="modal-footer">
                <button name="cancelDeletePageButton" type="button" class="btn btn-default">
                    <fmt:message key="cancel" />
                </button>
                <button name="confirmDeletePageButton" type="button" class="btn btn-warning">
                    <fmt:message key="delete" />
                </button>
            </div>
        </div>
    </div>
</div><%!
    private void displayPageChildrenAsList(Page page, XhtmlBuffer buf) {
        List<ChildPage> childPages = page.getLayout().getChildPages();
        List<ChildPage> detailChildPages = page.getDetailLayout().getChildPages();
        
        if(!childPages.isEmpty() || !detailChildPages.isEmpty()) {
            buf.openElement("ul");
            for(ChildPage childPage : childPages) {
                buf.openElement("li");
                buf.write(childPage.getName());
                buf.closeElement("li");
            }
            for(ChildPage childPage : detailChildPages) {
                buf.openElement("li");
                buf.write(childPage.getName() + " (detail)");
                buf.closeElement("li");
            }
            buf.closeElement("ul");
        }
    }

    private String displayPageChildrenAsList(Page page) {
        XhtmlBuffer buf = new XhtmlBuffer();
        displayPageChildrenAsList(page, buf);
        return buf.toString();
    }
%>