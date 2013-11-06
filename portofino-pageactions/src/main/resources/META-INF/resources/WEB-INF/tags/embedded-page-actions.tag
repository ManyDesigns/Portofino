<%@ attribute name="list" required="true"
%><%@ attribute name="cssClass" required="false"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino" %>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.AbstractPageAction"/>
<div class="${cssClass} embeddedPageActions" data-page-action-list="${list}">
    <% actionBean.initEmbeddedPageActions(); %>
    <c:forEach var="embeddedPageAction" items="${ actionBean.embeddedPageActions[list] }">
        <portofino:embedded-page-action id="${embeddedPageAction.id}" path="${embeddedPageAction.path}"
                                        returnUrl="${actionBean.returnUrl}#${embeddedPageAction.id}" />
    </c:forEach>
</div>