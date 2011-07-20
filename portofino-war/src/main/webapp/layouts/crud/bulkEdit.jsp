<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><stripes:layout-render name="/skins/${skin}/crud/common.jsp">
    <stripes:layout-component name="buttons">
        <stripes:submit id="TableData_update" name="bulkUpdate" value="Update"/>
        <stripes:submit id="TableData_cancel" name="cancel" value="Cancel"/>
    </stripes:layout-component>
    <stripes:layout-component name="innerContent">
        <h1><c:out value="${actionBean.useCase.editTitle}"/></h1>
        In the first column, select the fields you want to edit. Then, fill in their values.
        <mde:write name="actionBean" property="form"/>
        <stripes:hidden name="selection"/>
        <c:if test="${not empty actionBean.searchString}">
            <stripes:hidden name="searchString" value="${actionBean.searchString}"/>
        </c:if>
        <stripes:hidden name="cancelReturnUrl" value="${actionBean.cancelReturnUrl}"/>
    </stripes:layout-component>
</stripes:layout-render>