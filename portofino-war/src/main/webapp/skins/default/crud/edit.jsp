<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><jsp:include page="/skins/default/header.jsp"/>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.UseCaseAction"/>
<stripes:form action="${actionBean.dispatch.absolutePath}" method="post" enctype="multipart/form-data">
    <jsp:include page="/skins/default/crud/editButtonsBar.jsp"/>
    <div id="inner-content">
        <h1><c:out value="${actionBean.useCase.editTitle}"/></h1>
        <c:if test="${actionBean.requiredFieldsPresent}">
            Fields marked with a "*" are required.
        </c:if>
        <mde:write name="actionBean" property="form"/>
        <stripes:hidden name="pk" value="${actionBean.pk}"/>
        <c:if test="${not empty actionBean.searchString}">
            <stripes:hidden name="searchString" value="${actionBean.searchString}"/>
        </c:if>
        <stripes:hidden name="cancelReturnUrl" value="${actionBean.cancelReturnUrl}"/>
    </div>
    <jsp:include page="/skins/default/crud/editButtonsBar.jsp"/>
</stripes:form>
<jsp:include page="/skins/default/footer.jsp"/>