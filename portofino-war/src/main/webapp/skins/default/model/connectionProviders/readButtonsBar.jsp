<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<s:if test="#buttonsBarBottom">
    <div class="buttons-bar-bottom">
</s:if><s:else>
    <s:set var="buttonsBarBottom" value="true"/>
    <div class="buttons-bar-top">
</s:else>
    <s:submit id="ConnectionProviders_returnToSearch" method="returnToSearch" value="<< Return to list"/>
    <s:submit id="ConnectionProviders_test" method="test" value="Test"/>
    <s:submit id="ConnectionProviders_delete" method="delete" value="Delete"
              onclick="return confirm ('Are you sure?');"/>
    <s:submit id="ConnectionProviders_update" method="edit" value="Update"/>
</div>