<%@ taglib prefix="mde" uri="/manydesigns-elements" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="actionBean" scope="request"
             type="com.manydesigns.portofino.actions.admin.page.PageAdminAction"/>
<div id="dialog-move-page" title='<fmt:message key="layouts.admin.movePageDialog.move_to"/>'>
    <p><fmt:message key="layouts.admin.movePageDialog.choose_where_move"/>:</p>
    <input type="hidden" name="movePage" value="action" />
    <mde:write name="actionBean" property="moveForm"/>
</div>