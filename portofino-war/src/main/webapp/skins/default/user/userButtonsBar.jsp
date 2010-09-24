<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<s:if test="#buttonsBarBottom">
    <div class="buttons-bar-bottom">
</s:if><s:else>
    <s:set var="buttonsBarBottom" value="true"/>
    <div class="buttons-bar-top">
</s:else>


<s:if test="state==1">
    <s:submit id="User_update" method="edit" value="Update"/>
    <s:submit id="User_changePwd" method="changePwd" value="Change password"/>
</s:if>
<s:if test="state==2">
    <s:submit id="User_update" method="update" value="Update"/>
</s:if>
<s:if test="state==3">
    <s:submit id="User_chanhePwd" method="updatePwd" value="Change password"/>
</s:if>


</div>