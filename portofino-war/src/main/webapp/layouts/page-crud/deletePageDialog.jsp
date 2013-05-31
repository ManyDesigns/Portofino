<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="com.manydesigns.elements.xml.XhtmlBuffer" %>
<%@ page import="com.manydesigns.portofino.dispatcher.PageInstance" %>
<%@ page import="com.manydesigns.portofino.pages.ChildPage" %>
<%@ page import="com.manydesigns.portofino.pages.Page" %>
<%@ page import="java.util.List" %>
<jsp:useBean id="actionBean" scope="request"
             type="com.manydesigns.portofino.actions.admin.page.PageAdminAction"/>
<div id="dialog-confirm-delete-page" class="modal hide">
    <script type="text/javascript">
        $("#confirmDeletePageButton").click(function() {
            var form = $("#pageAdminForm");
            copyFormAsHiddenFields($("#dialog-confirm-delete-page"), form);
            form.submit();
        });

        $("#cancelDeletePageButton, #closeDeletePageButton").click(function() {
            $("#dialog-confirm-delete-page").modal("hide");
            $("#dialog-confirm-delete-page").remove();
        });
    </script>
    <div class="modal-header">
        <button id="closeDeletePageButton" type="button" class="close" aria-hidden="true">&times;</button>
        <h4><fmt:message key="layouts.admin.deletePageDialog.really_delete"/></h4>
    </div>
    <div class="modal-body">
        <p><fmt:message key="layouts.admin.deletePageDialog.are_you_sure"/></p>
        <%
            PageInstance pageInstance = actionBean.getPageInstance();
            Page pg = pageInstance.getPage();
            if(!pg.getLayout().getChildPages().isEmpty() ||
               !pg.getDetailLayout().getChildPages().isEmpty()) { %>
                <p><fmt:message key="layouts.admin.deletePageDialog.children"/></p><%
                out.print(displayPageChildrenAsList(pg));
            }
        %>
        <input type="hidden" name="deletePage" value="action" />
    </div>
    <div class="modal-footer">
        <button id="cancelDeletePageButton" type="button" class="btn">
            <fmt:message key="commons.cancel" />
        </button>
        <button id="confirmDeletePageButton" type="button" class="btn btn-warning">
            <fmt:message key="commons.delete" />
        </button>
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