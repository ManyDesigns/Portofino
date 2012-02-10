<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="/skins/default/header.jsp"/>
<s:form method="post">
    <jsp:include page="/skins/default/user/profile/changePwdButtonsBar.jsp"/>
    <div id="inner-content">
        <h1><fmt:message key="layouts.user.profile.changePwd.change_password"/></h1>
        <mdes:write value="form"/>
    </div>
    <jsp:include page="/skins/default/user/profile/changePwdButtonsBar.jsp"/>
</s:form>
<jsp:include page="/skins/default/footer.jsp"/>