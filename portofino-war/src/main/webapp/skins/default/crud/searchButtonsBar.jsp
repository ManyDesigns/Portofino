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
    <stripes:submit id="Table_create" name="create" value="Create new"/>
    <stripes:submit id="Table_bulkEdit" name="bulkEdit" value="Edit"/>
    <stripes:submit id="Table_bulkDelete" name="bulkDelete" value="Delete"
              onclick="return confirm ('Are you sure?');"/>
    <stripes:submit id="Table_print" name="print" value="Print" disabled="true"/>
    <stripes:submit id="Table_exportExcel" name="exportSearchExcel" value="Excel" disabled="false"/>
    <stripes:submit id="Table_exportPdf" name="exportSearchPdf" value="Pdf" disabled="false"/>
    <jsp:include page="crudButtons.jsp"/>
</div>