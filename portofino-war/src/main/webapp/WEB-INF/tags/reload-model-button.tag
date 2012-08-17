<%@ tag import="com.manydesigns.portofino.logic.SecurityLogic" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<% if(SecurityLogic.isAdministrator(request)) { %>
<button name="reloadModel"
        type="submit"
        class="refresh ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only"
        role="button" aria-disabled="false"
        title="Reload model">
    <span class="ui-button-icon-primary ui-icon ui-icon-refresh"></span>
    <span class="ui-button-text">Reload model</span>
</button>
<% } %>