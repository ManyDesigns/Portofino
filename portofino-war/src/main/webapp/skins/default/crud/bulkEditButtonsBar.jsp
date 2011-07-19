<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:if test="${not empty buttonsBarBottom}">
    <div class="buttons-bar-bottom">
</c:if><c:if test="${empty buttonsBarBottom}">
    <c:set var="buttonsBarBottom" value="true"/>
    <div class="buttons-bar-top">
</c:if>
    <stripes:submit id="TableData_update" name="bulkUpdate" value="Update"/>
    <stripes:submit id="TableData_cancel" name="cancel" value="Cancel"/>
</div>