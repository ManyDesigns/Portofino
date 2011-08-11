<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<%@taglib prefix="mde" uri="/manydesigns-elements"%>
<stripes:layout-render name="/skins/${skin}/portlet.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.CrudAction"/>
    <stripes:layout-component name="portletTitle">
        <c:out value="${actionBean.crud.readTitle}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletHeaderButtons">
        <button name="configure" class="wrench">Configure</button>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <mde:write name="actionBean" property="form"/>
        <input type="hidden" name="pk" value="<c:out value="${actionBean.pk}"/>"/>
        <c:if test="${not empty actionBean.searchString}">
            <input type="hidden" name="searchString" value="<c:out value="${actionBean.searchString}"/>"/>
        </c:if>
        <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletFooter">
        <stripes:submit name="edit" value="Edit" class="portletButton"/>
        <stripes:submit name="delete" value="Delete" onclick="return confirm ('Are you sure?');" class="portletButton"/>
        <stripes:submit name="duplicate" value="Duplicate" disabled="true" class="portletButton"/>
        <stripes:submit name="print" value="Print" disabled="true" class="portletButton"/>
        <stripes:submit name="exportReadExcel" value="Excel" disabled="true" class="portletButton"/>
        <stripes:submit name="exportReadPdf" value="Pdf" disabled="false" class="portletButton"/>
        <!-- TODO custom buttons -->
    </stripes:layout-component>
</stripes:layout-render>