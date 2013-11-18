<%@ taglib prefix="mde" uri="/manydesigns-elements" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="actionBean" scope="request"
             type="com.manydesigns.portofino.actions.admin.page.PageAdminAction"/>
<div class="dialog-move-page modal hide">
    <div class="modal-header">
        <button name="closeMovePageButton" type="button" class="close" aria-hidden="true">&times;</button>
        <h4><fmt:message key="move.to"/></h4>
    </div>
    <div class="modal-body form-horizontal">
        <input type="hidden" name="movePage" value="action" />
        <mde:write name="actionBean" property="moveForm"/>
    </div>
    <div class="modal-footer">
        <button name="cancelMovePageButton" type="button" class="btn">
            <fmt:message key="cancel" />
        </button>
        <button name="confirmMovePageButton" type="button" class="btn btn-warning">
            <fmt:message key="move" />
        </button>
    </div>
</div>