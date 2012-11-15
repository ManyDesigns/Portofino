<%@ tag import="com.manydesigns.portofino.logic.SecurityLogic"
%><%@ tag import="com.manydesigns.portofino.security.AccessLevel"
%><%@ tag import="org.apache.shiro.SecurityUtils"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.AbstractPageAction"/>
<% if(SecurityLogic.hasPermissions(actionBean.getPageInstance(), SecurityUtils.getSubject(), AccessLevel.DEVELOP)) { %>
<button name="pagePermissions"
        type="submit"
        class="person ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only"
        role="button" aria-disabled="false"
        title="Page permissions">
    <span class="ui-button-icon-primary ui-icon ui-icon-person"></span>
    <span class="ui-button-text">Page permissions</span>
</button>
<% } %>