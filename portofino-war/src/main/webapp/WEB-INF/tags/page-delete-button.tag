<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="actionBean" scope="request"
     type="com.manydesigns.portofino.pageactions.AbstractPageAction"/>
<button name="deletePage"
        onclick="confirmDeletePage(
                    '<%= actionBean.dispatch.getLastPageInstance().getPathFromRoot() %>',
                    '<%= request.getContextPath() %>');
                return false;"
        type="submit"
        class="minusthick ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only"
        role="button" aria-disabled="false"
        title="Delete page">
    <span class="ui-button-icon-primary ui-icon ui-icon-minusthick"></span>
    <span class="ui-button-text">Delete page</span>
</button>