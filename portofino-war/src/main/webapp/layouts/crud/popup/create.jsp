<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.crud.AbstractCrudAction"/>
<stripes:layout-render name="/skins/${skin}${actionBean.pageTemplate}/popup.jsp">
    <stripes:layout-component name="portletTitle">
        <c:out value="${actionBean.createTitle}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <stripes:form action="${actionBean.dispatch.originalPath}" method="post" enctype="multipart/form-data"
                      class="form-horizontal">
            <c:if test="${actionBean.requiredFieldsPresent}">
                <p><fmt:message key="commons.fields_required"/>.</p>
            </c:if>
            <mde:write name="actionBean" property="form"/>
            <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
            <input type="hidden" name="popup" value="true" />
            <input type="hidden" name="popupCloseCallback" value="${actionBean.popupCloseCallback}" />
            <div class="form-actions">
                <portofino:buttons list="crud-create" />
            </div>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>