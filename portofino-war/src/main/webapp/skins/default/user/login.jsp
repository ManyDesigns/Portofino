<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="s" uri="/struts-tags"
%><%@ taglib prefix="mdes" uri="/manydesigns-elements-struts2"
%><s:include value="/skins/default/header.jsp"/>
<s:form method="post" action="Login.action">

    <div id="inner-content">
        <h1>Login </h1>
        <div id="login">
            <mdes:write value="form"/>
            <s:submit id="loginButton" method="login" value="Login"/>
            <s:submit id="cancelButton" method="cancel" value="Cancel"/>
        </div>
        <s:hidden name="cancelReturnUrl" value="%{cancelReturnUrl}"/>
        <s:hidden name="successReturnUrl" value="%{successReturnUrl}"/>
    </div>

</s:form>
<s:include value="/skins/default/footer.jsp"/>