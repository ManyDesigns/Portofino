<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"
%><%@ page import="com.manydesigns.portofino.PortofinoProperties"
%><%@ page import="com.manydesigns.portofino.modules.BaseModule"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><stripes:url var="profileUrl" value="/actions/profile"/>
<jsp:useBean id="portofinoConfiguration" scope="application"
             type="org.apache.commons.configuration.Configuration"/>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.stripes.AbstractActionBean"/>
<fmt:setLocale value="${pageContext.request.locale}"/>
<style type="text/css">
@media (min-width: 980px) {
    body {
        padding-top: 50px;
    }
}
</style>
<div class="navbar navbar-inverse navbar-fixed-top">
    <div class="navbar-inner">
        <div class="container">
            <h4 id="app-title" class="pull-left">
                <stripes:link href="/">
                    <c:out value="<%= portofinoConfiguration.getString(PortofinoProperties.APP_NAME) %>"/>
                </stripes:link>
            </h4>
            <button type="button" class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <div id="header-menu" class="nav-collapse collapse">
                <c:if test="${not empty actionBean.dispatch}">
                    <form id="pageAdminForm" action="${pageContext.request.contextPath}/actions/admin/page">
                        <input type="hidden" name="originalPath" value="${actionBean.dispatch.originalPath}" />
                    </form>
                </c:if>
                <ul id="app-menu" class="nav">
                    <% request.setAttribute("menu", BaseModule.APP_MENU); %>
                    <jsp:include page="/m/base/admin-theme/render-menu.jsp" />
                </ul>
                <ul id="user-menu" class="nav">
                    <% request.setAttribute("menu", BaseModule.USER_MENU); %>
                    <jsp:include page="/m/base/admin-theme/render-menu.jsp" />
                </ul>
            </div>
        </div>
    </div>
</div>
