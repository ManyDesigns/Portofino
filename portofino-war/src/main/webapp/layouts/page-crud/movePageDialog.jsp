<%@ taglib prefix="mde" uri="/manydesigns-elements" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="actionBean" scope="request"
             type="com.manydesigns.portofino.actions.admin.page.PageAdminAction"/>
<div id="dialog-move-page" class="modal hide">
    <script type="text/javascript">
        $("#confirmMovePageButton").click(function() {
            var form = $("#pageAdminForm");
            copyFormAsHiddenFields($("#dialog-move-page"), form);
            form.submit();
        });

        $("#cancelMovePageButton, #closeMovePageButton").click(function() {
            $("#dialog-move-page").modal("hide");
            $("#dialog-move-page").remove();
        });
    </script>
    <div class="modal-header">
        <button id="closeMovePageButton" type="button" class="close" aria-hidden="true">&times;</button>
        <h4><fmt:message key="layouts.admin.movePageDialog.move_to"/></h4>
    </div>
    <div class="modal-body form-horizontal">
        <input type="hidden" name="movePage" value="action" />
        <mde:write name="actionBean" property="moveForm"/>
    </div>
    <div class="modal-footer">
        <button id="cancelMovePageButton" type="button" class="btn">
            <fmt:message key="commons.cancel" />
        </button>
        <button id="confirmMovePageButton" type="button" class="btn btn-warning">
            <fmt:message key="commons.move" />
        </button>
    </div>
</div>