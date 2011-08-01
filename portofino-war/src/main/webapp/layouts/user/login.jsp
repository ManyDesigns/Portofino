<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
        %><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
        %><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
        %><%@taglib prefix="mde" uri="/manydesigns-elements"
        %><stripes:layout-render name="/skins/default/common-simple.jsp">
    <stripes:layout-component name="content">
        <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.user.LoginAction"/>
        <stripes:form action="${dispatch.absoluteOriginalPath}" method="post">
            <div id="content-login">
                <h1>Login</h1>
                <br/>
                <table class="details">
                    <tbody>
                    <tr>
                        <th><label class="field" for="userName">User name:</label></th>
                        <td><stripes:text name="userName" id="userName" class="text" /></td>
                    </tr>
                    <tr>
                        <th><label class="field" for="pwd">Password:</label></th>
                        <td><input type="password" name="pwd" id="pwd" class="text" /></td>
                    </tr>
                    <tr>
                        <th></th>
                        <td><stripes:submit id="loginButton" name="login" value="Login" /></td>
                    </tr>
                    <c:if test="recoverPwd">
                        <tr>
                            <td colspan="2">
                                Hai dimenticato la password? <a href="PwdRecovery.action">recupera password</a>
                            </td>
                        </tr>
                    </c:if>
                    </tbody>
                </table>
                <br/>
            </div>
            <script type="text/javascript">
                $('#userName').focus();
            </script>
            <stripes:hidden name="returnUrl"/>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>