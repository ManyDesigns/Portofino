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
<div class="navbar navbar-inverse navbar-fixed-top">
    <div class="navbar-inner">
        <div class="container">
            <h4 id="app-title" class="pull-left">
                <stripes:link href="/">
                    <c:out value="<%= portofinoConfiguration.getString(PortofinoProperties.APP_NAME) %>"/>
                </stripes:link>
            </h4>
            This is the default header. You should customize it including a /m/theme/header.jsp page in your application.
        </div>
    </div>
</div>
