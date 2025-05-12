<%@ attribute name="id" required="true"
%><%@ attribute name="path" required="true"
%><%@ attribute name="returnUrl" required="false"
%><%@ attribute name="cssClass" required="false"
%><%@ attribute name="anchor" required="false" type="java.lang.Boolean"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@ taglib prefix="fmt" uri="jakarta.tags.fmt"
%><%@ taglib prefix="c" uri="jakarta.tags.core"
%><%@ tag import="org.slf4j.LoggerFactory"
%><%@ tag import="com.manydesigns.portofino.dispatcher.PageAction" %>
<c:if test="${empty returnUrl}">
    <jsp:useBean id="actionBean" scope="request"
                 type="com.manydesigns.portofino.pageactions.AbstractPageAction"/>
    <c:set var="returnUrl" value="${actionBean.returnUrl}#${id}" />
</c:if>
<c:if test="${(empty anchor) or anchor}">
    <a name="<c:out value='${id}' />"></a>
</c:if>


<div id="embeddedPageAction_${id}" class="${cssClass}">

</div>

    <script>
        {
        const container = document.getElementById("embeddedPageAction_<c:out value='${id}' />");
        const returnUrl = "<c:out value='${returnUrl}' />";

        fetch("<c:out value='${path}' />?__portofino_quiet_auth_failure=true&embedded=true&returnUrl=" + encodeURIComponent(returnUrl))
            .then(res => res.text())
            .then(html => container.innerHTML = html)
            .catch(err => container.innerHTML = "Errore nel caricamento");
        }
    </script>


