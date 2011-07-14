<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><jsp:include page="/skins/default/header.jsp"/>
<jsp:useBean id="actionBean" class="com.manydesigns.portofino.actions.DocumentAction"/>
<div id="inner-content">
    ${actionBean.content}
</div>
<jsp:include page="/skins/default/footer.jsp"/>