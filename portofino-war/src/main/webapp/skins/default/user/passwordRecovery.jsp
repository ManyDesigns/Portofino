<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="s" uri="/struts-tags"
%><%@taglib prefix="mdes" uri="/manydesigns-elements-struts2"
%><s:include value="/skins/default/header.jsp"/>
<s:form method="post">
    <div id="inner-content">


        <h1>Recupero password</h1>

        <p>Inserisci l'email del tuo account.<br/>
            Ti verranno inviate le istruzioni su come modificare la tua password</p>

        <form action="./SendPwd.action" method="post">
            email: <input type="text" name="email" size="25"/><br/>
            <input type="submit" name="invia" value="invia"/>
            <input type="hidden" name="method:send" value="send"/> 
        </form>
        <p/>
    </div>

</s:form>
<s:include value="/skins/default/footer.jsp"/>