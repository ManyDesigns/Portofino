<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="s" uri="/struts-tags"
%><%@ taglib prefix="mdes" uri="/manydesigns-elements-struts2"
%><s:include value="/skins/default/header.jsp"/>
<s:form method="post" namespace="/user" action="Login.action">

    <div id="inner-content">

        <div id="login">
            <h1>Login</h1>
            <p/>
            <mdes:write value="form"/>

            <s:submit id="loginButton" method="login" value="Login" />
            <p>
            <s:if test="recoverPwd">
            Hai dimenticato la password? <a href="PwdRecovery.action">recupera password</a>
            </p>
            </s:if>
        </div>
        <s:hidden name="successReturnUrl" value="%{successReturnUrl}"/>
    </div>

</s:form>
<s:include value="/skins/default/footer.jsp"/>