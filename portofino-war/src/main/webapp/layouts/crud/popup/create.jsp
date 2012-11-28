<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<stripes:layout-render name="/skins/${skin}/popup-page.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.crud.AbstractCrudAction"/>
    <stripes:layout-component name="contentHeader">
        <portofino:buttons list="crud-create" cssClass="contentButton" />
    </stripes:layout-component>
    <stripes:layout-component name="portletTitle">
        <c:out value="${actionBean.createTitle}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <c:if test="${actionBean.requiredFieldsPresent}">
            <fmt:message key="commons.fields_required"/>
        </c:if>
        <mde:write name="actionBean" property="form"/>
        <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
        <input type="hidden" name="popup" value="true" />
        <input type="hidden" name="popupCloseCallback" value="${actionBean.popupCloseCallback}" />
    </stripes:layout-component>
    <stripes:layout-component name="portletFooter"/>
    <stripes:layout-component name="contentFooter">
        <portofino:buttons list="crud-create" cssClass="contentButton" />
    </stripes:layout-component>
</stripes:layout-render>