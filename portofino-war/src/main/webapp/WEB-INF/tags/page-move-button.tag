<%@ tag import="com.manydesigns.portofino.logic.SecurityLogic" %>
<%@ tag import="com.manydesigns.portofino.security.AccessLevel" %>
<%@ tag import="org.apache.shiro.SecurityUtils" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.AbstractPageAction"/>
<% if(SecurityLogic.hasPermissions(actionBean.getPageInstance(), SecurityUtils.getSubject(), AccessLevel.EDIT)) { %>
<button onclick="showMovePageDialog(
                    '<%= actionBean.getDispatch().getLastPageInstance().getPathFromRoot() %>',
                    '<%= request.getContextPath() %>');
                return false;"
        type="submit"
        class="transferthick-e-w ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only"
        role="button" aria-disabled="false"
        title="Move page">
    <span class="ui-button-icon-primary ui-icon ui-icon-transferthick-e-w"></span>
    <span class="ui-button-text">Move page</span>
</button>
<% } %>