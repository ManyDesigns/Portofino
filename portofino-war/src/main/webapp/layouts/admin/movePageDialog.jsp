<%@ taglib prefix="mde" uri="/manydesigns-elements" %>
<%@ page import="com.manydesigns.elements.xml.XhtmlBuffer" %>
<%@ page import="com.manydesigns.portofino.model.pages.Page" %>
<jsp:useBean id="actionBean" scope="request"
             type="com.manydesigns.portofino.actions.PageAction"/>
<div id="dialog-move-page" title="Move to...">
    <p>Choose where to move this page:</p>
    <mde:write name="actionBean" property="moveForm"/>
</div>