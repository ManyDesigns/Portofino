<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<stripes:layout-render name="/skins/${skin}/modal-page.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.CrudAction"/>
    <stripes:layout-component name="contentHeader">
        <stripes:submit name="bulkUpdate" value="Update" class="contentButton"/>
        <stripes:submit name="cancel" value="Cancel" class="contentButton"/>
        <div class="breadcrumbs">
            <div class="inner">
                <mde:write name="breadcrumbs"/>
            </div>
        </div>
    </stripes:layout-component>
    <stripes:layout-component name="portletTitle">
        <c:out value="${actionBean.crud.editTitle}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <fmt:message key = "layouts.crud.bulk-edit.select_columns"/>
        <mde:write name="actionBean" property="form"/>
        <stripes:hidden name="selection"/>
        <c:if test="${not empty actionBean.searchString}">
            <input type="hidden" name="searchString" value="<c:out value="${actionBean.searchString}"/>"/>
        </c:if>
        <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletFooter"/>
    <stripes:layout-component name="contentFooter">
        <stripes:submit name="bulkUpdate" value="Update" class="contentButton"/>
        <stripes:submit name="cancel" value="Cancel" class="contentButton"/>
    </stripes:layout-component>
</stripes:layout-render>