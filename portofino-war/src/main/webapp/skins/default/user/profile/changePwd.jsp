<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="s" uri="/struts-tags"
%><%@taglib prefix="mdes" uri="/manydesigns-elements-struts2"
%><s:include value="/skins/default/header.jsp"/>
<s:form method="post">
    <s:include value="/skins/default/user/profile/changePwdButtonsBar.jsp"/>
    <div id="inner-content">
        <h1>Change password</h1>
        <mdes:write value="form"/>
    </div>
    <s:include value="/skins/default/user/profile/changePwdButtonsBar.jsp"/>
</s:form>
<s:include value="/skins/default/footer.jsp"/>