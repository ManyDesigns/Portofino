<%@ page import="com.manydesigns.portofino.modules.ModuleRegistry" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld" %>
<%@ taglib prefix="mde" uri="/manydesigns-elements" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="portofino" %>
<%@ page import="com.manydesigns.portofino.shiro.ShiroUtils"%>
<!DOCTYPE html>
<html lang="<%= request.getLocale() %>">
<jsp:include page="/theme/head.jsp">
    <jsp:param name="pageTitle" value="${pageTitle}" />
</jsp:include>
<body class="dialog">
    <div class="dialog-container">
        <div class="pageBody spacingTop">
            <div style="text-align: center;">
                <strong>Loading...</strong>
            </div>
            <script>
                window.location.replace('/login?googleCallbackJS&'+window.location.hash.substring(1))
            </script>
        </div>
    </div>
</body>
</html>
