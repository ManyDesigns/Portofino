<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="/skins/default/header.jsp"/>
<s:form method="post">
    <div id="inner-content">


        <h1><fmt:message key="layouts.user.passwordRecovery.password_recovery"/></h1>
        <p><fmt:message key="layouts.user.passwordRecovery.insert_email"/></p>
        <form action="./SendPwd.action" method="post">
            email: <input type="text" name="email" size="25"/><br/>
            <input type="submit" name="invia" value="invia"/>
            <input type="hidden" name="method:send" value="send"/> 
        </form>
        <p/>
    </div>

</s:form>
<jsp:include page="/skins/default/footer.jsp"/>