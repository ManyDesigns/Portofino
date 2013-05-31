<%@ taglib prefix="mde" uri="/manydesigns-elements" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="actionBean" scope="request"
             type="com.manydesigns.portofino.actions.admin.page.PageAdminAction"/>
<div id="dialog-copy-page" class="modal hide">
    <script type="text/javascript">
        $("#confirmCopyPageButton").click(function() {
            var form = $("#pageAdminForm");
            copyFormAsHiddenFields($("#dialog-copy-page"), form);
            form.submit();
        });

        $("#cancelCopyPageButton, #closeCopyPageButton").click(function() {
            $("#dialog-copy-page").modal("hide");
            $("#dialog-copy-page").remove();
        });
    </script>
    <div class="modal-header">
        <button id="closeCopyPageButton" type="button" class="close" aria-hidden="true">&times;</button>
        <h4><fmt:message key="layouts.admin.copyPageDialog.copy_to"/></h4>
    </div>
    <div class="modal-body form-horizontal">
        <input type="hidden" name="copyPage" value="action" />
        <mde:write name="actionBean" property="copyForm"/>
    </div>
    <div class="modal-footer">
        <button id="cancelCopyPageButton" type="button" class="btn">
            <fmt:message key="commons.cancel" />
        </button>
        <button id="confirmCopyPageButton" type="button" class="btn btn-primary">
            <fmt:message key="commons.copy" />
        </button>
    </div>
</div>