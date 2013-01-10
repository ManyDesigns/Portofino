<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><stripes:layout-render name="/skins/${skin}${actionBean.pageTemplate}/modal.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.changepassword.ChangePasswordAction"/>
    <stripes:layout-component name="contentHeader">
        <portofino:buttons list="configuration" cssClass="contentButton" />
        <jsp:include page="/skins/${skin}/breadcrumbs.jsp" />
    </stripes:layout-component>
    <stripes:layout-component name="portletHeader">
        <%@include file="../portlet-common-configuration.jsp" %>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <mde:write name="actionBean" property="configurationForm"/>
        <%@include file="../script-configuration.jsp" %>
        <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.dispatch.absoluteOriginalPath}"/>"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletFooter"/>
    <stripes:layout-component name="contentFooter">
        <portofino:buttons list="configuration" cssClass="contentButton" />
    </stripes:layout-component>
</stripes:layout-render>