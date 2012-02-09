<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<s:if test="#buttonsBarBottom">
    <div class="buttons-bar-bottom">
</s:if><s:else>
    <s:set var="buttonsBarBottom" value="true"/>
    <div class="buttons-bar-top">
</s:else>
    <s:submit id="refresh" method="execute" value="Refresh view"/>
    <s:submit id="SelfTest_sync" method="sync" value="Synchronize model"/>
    <s:submit id="SelfTest_export" method="export" value="Export xml"/>
</div>