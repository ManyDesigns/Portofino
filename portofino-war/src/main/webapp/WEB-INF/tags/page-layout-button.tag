<%@ tag import="com.manydesigns.portofino.logic.SecurityLogic" %>
<%@ tag import="com.manydesigns.portofino.security.AccessLevel" %>
<%@ tag import="org.apache.shiro.SecurityUtils" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.AbstractPageAction"/>
<% if(SecurityLogic.hasPermissions(actionBean.getPageInstance(), SecurityUtils.getSubject(), AccessLevel.EDIT)) { %>
<button onclick="portofino.enablePortletDragAndDrop(this); return false;"
        type="submit" class="btn btn-mini" role="button" aria-disabled="false"
        title="Edit page layout">
    <i class="icon-move"></i>
</button>
<% } %>