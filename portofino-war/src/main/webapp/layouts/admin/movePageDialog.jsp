<%@ taglib prefix="mde" uri="/manydesigns-elements" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="com.manydesigns.elements.xml.XhtmlBuffer" %>
<%@ page import="com.manydesigns.portofino.model.pages.Page" %>
<jsp:useBean id="actionBean" scope="request"
             type="com.manydesigns.portofino.actions.admin.page.PageAdminDialogAction"/>
<div id="dialog-move-page" title='<fmt:message key="layouts.admin.movePageDialog.move_to"/>'>
    <p><fmt:message key="layouts.admin.movePageDialog.choose_where_move"/>:</p>
    <input type="hidden" name="movePage" value="action" />
    <mde:write name="actionBean" property="moveForm"/>
</div>