<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino" %>
<stripes:layout-render name="/skins/default/admin-page.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.admin.ConnectionProvidersAction"/>
    <stripes:layout-component name="pageTitle">
        <fmt:message key="layouts.admin.connectionProviders.create.create"/>
    </stripes:layout-component>
    <stripes:layout-component name="contentHeader">
        <portofino:buttons list="connectionProviders-create" cssClass="contentButton" />
    </stripes:layout-component>
    <stripes:layout-component name="portletTitle">
        <fmt:message key="layouts.admin.connectionProviders.create.create"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <mde:write name="actionBean" property="form"/>
        <c:if test="${actionBean.detectedValuesForm != null}">
            <div class="horizontalSeparator"></div>
        </c:if>
        <stripes:hidden name="connectionType" />
    </stripes:layout-component>
    <stripes:layout-component name="contentFooter">
        <portofino:buttons list="connectionProviders-create" cssClass="contentButton" />
    </stripes:layout-component>
</stripes:layout-render>