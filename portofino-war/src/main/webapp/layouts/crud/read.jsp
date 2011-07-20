<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%>
<stripes:layout-render name="/skins/${skin}/crud/common.jsp">
    <stripes:layout-component name="buttons">
        <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.UseCaseAction"/>
        <c:if test="${actionBean.position >= 0}">
            <div style="float: right;">
                <c:if test="${not empty actionBean.firstUrl}">
                    <stripes:link id="first" href="${actionBean.firstUrl}">first</stripes:link>
                    <stripes:link id="previous" href="${actionBean.previousUrl}">previous</stripes:link>
                </c:if>
                <c:if test="${empty actionBean.firstUrl}">
                    <span class="disabled">first</span> <span class="disabled">previous</span>
                </c:if>
                <c:out value="${actionBean.position + 1}"/> of <c:out value="${actionBean.size}"/>
                <c:if test="${not empty actionBean.lastUrl}">
                    <stripes:link id="next" href="${actionBean.nextUrl}">next</stripes:link>
                    <stripes:link id="last" href="${actionBean.lastUrl}">last</stripes:link>
                </c:if>
                <c:if test="${empty actionBean.lastUrl}">
                    <span class="disabled">next</span> <span class="disabled">last</span>
                </c:if>
            </div>
        </c:if>
        <stripes:submit id="Table_returnToSearch" name="returnToSearch" value="<< Return to search"/>
        <stripes:submit id="Table_edit" name="edit" value="Edit"/>
        <stripes:submit id="Table_delete" name="delete" value="Delete"
                  onclick="return confirm ('Are you sure?');"/>
        <stripes:submit id="Table_duplicate" name="duplicate" value="Duplicate" disabled="true"/>
        <stripes:submit id="Table_print" name="print" value="Print" disabled="true"/>
        <stripes:submit id="Table_exportExcel" name="exportReadExcel" value="Excel" disabled="false"/>
        <stripes:submit id="Table_exportPdf" name="exportReadPdf" value="Pdf" disabled="false"/>
        <!-- TODO custom buttons -->
    </stripes:layout-component>
    <stripes:layout-component name="innerContent">
        <h1><c:out value="${actionBean.useCase.readTitle}"/></h1>
        <mde:write name="actionBean" property="form"/>
        <stripes:hidden name="pk" value="${actionBean.pk}"/>
        <c:if test="${not empty actionBean.searchString}">
            <stripes:hidden name="searchString" value="${actionBean.searchString}"/>
        </c:if>
        <stripes:hidden name="cancelReturnUrl" value="${actionBean.cancelReturnUrl}"/>

        <%--<c:set var="skin" value="embedded" scope="request"/>
        <jsp:include page="${actionBean.dispatch.servletPath}/issues" />--%>
    </stripes:layout-component>
</stripes:layout-render>