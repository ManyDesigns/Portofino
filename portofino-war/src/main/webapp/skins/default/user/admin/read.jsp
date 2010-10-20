<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="s" uri="/struts-tags"
%><%@ taglib prefix="mdes" uri="/manydesigns-elements-struts2"
%><s:include value="/skins/default/header.jsp"/>
<s:form method="post">
    <s:include value="/skins/default/user/admin/readButtonsBar.jsp"/>
    <div id="inner-content">
        <h1>User detail</h1>
        <mdes:write value="form"/>
        <h2>Active groups</h2>
        <mdes:write value="activeGroupsForm"/>

        <h2>Inactive groups</h2>
        <mdes:write value="deletedGroupsForm"/>
        <s:hidden name="pk" value="%{pk}"/>
        <s:if test="searchString != null">
            <s:hidden name="searchString" value="%{searchString}"/>
        </s:if>
        <s:url var="cancelReturnUrl"
               namespace="/user-admin"
               action="UsersAction"
               escapeAmp="false">
            <s:param name="pk" value="%{pk}"/>
            <s:param name="searchString" value="%{searchString}"/>
        </s:url>
        <s:hidden name="cancelReturnUrl" value="%{#cancelReturnUrl}"/>
    </div>
    <s:include value="/skins/default/user/admin/readButtonsBar.jsp"/>
</s:form>
<s:include value="/skins/default/footer.jsp"/>