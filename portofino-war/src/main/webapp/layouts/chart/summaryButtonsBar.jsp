<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<s:if test="#buttonsBarBottom">
    <div class="buttons-bar-bottom">
</s:if><s:else>
    <s:set var="buttonsBarBottom" value="true"/>
    <div class="buttons-bar-top">
</s:else>
    <s:submit id="TableDesign_edit" method="edit" value="Edit" />
    <s:submit id="TableDesign_rename" method="rename" value="Rename" />
    <s:submit id="TableDesign_drop" method="drop" value="Drop" />
    <s:submit id="TableDesign_create" method="create" value="Create New" />
</div>