<%@ taglib prefix="mde" uri="/manydesigns-elements" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="actionBean" scope="request"
             type="com.manydesigns.portofino.actions.admin.page.PageAdminAction"/>
<div id="dialog-copy-page" title='<fmt:message key="layouts.admin.copyPageDialog.copy_to"/>'>
    <p><fmt:message key="layouts.admin.copyPageDialog.choose_where_copy"/>:</p>
    <input type="hidden" name="copyPage" value="action" />
    <mde:write name="actionBean" property="copyForm"/>
</div>