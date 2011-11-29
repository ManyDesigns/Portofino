<%@ taglib prefix="mde" uri="/manydesigns-elements" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="actionBean" scope="request"
             type="com.manydesigns.portofino.actions.PageAction"/>
<div id="dialog-copy-page" title="Copy to...">
    <p><fmt:message key="layouts.admin.movePageDialog.choose_where_move"/>:</p>
    <input type="hidden" name="copyPage" value="action" />
    <mde:write name="actionBean" property="copyForm"/>
</div>