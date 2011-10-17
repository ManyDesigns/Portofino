<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="/skins/default/header.jsp"/>
<s:form method="post">
    <jsp:include page="/skins/default/user/profile/readButtonsBar.jsp"/>
    <div id="inner-content">
        <h1><fmt:message key="layouts.user.lostPwdChange.change_password"/></h1>
        <mdes:write value="form"/>
        <input type="hidden" name="token" value="<s:property value="token"/>"/>
        <input type="hidden" name="method:updatePwd" value="updatePwd"/>
        <input type="submit" name="invia" value="invia"/>
    </div>
    <jsp:include page="/skins/default/user/profile/readButtonsBar.jsp"/>
</s:form>
<jsp:include page="/skins/default/footer.jsp"/>