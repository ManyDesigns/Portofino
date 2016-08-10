<%@ page import="com.manydesigns.elements.xml.XhtmlBuffer"
%><%@ page import="com.manydesigns.portofino.navigation.BaseHref"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ page import="com.manydesigns.portofino.PortofinoProperties"
   %><%@ page import="org.apache.commons.lang.StringUtils"
%><%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"
%><jsp:useBean id="portofinoConfiguration" scope="application" type="org.apache.commons.configuration.Configuration"/><%--

    This is only an example. You should customize your head.jsp depending on the installed modules.

--%><head>
<meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<!-- Polyfills for IE6-8 support of HTML5 elements + media queries + Canvas support + ES5 functions -->
<!--[if lt IE 9]>
    <script src="<stripes:url value='/webjars/es5-shim/4.0.6/es5-shim.min.js' />"></script>
    <script src="<stripes:url value='/webjars/html5shiv/3.7.2/html5shiv-printshiv.js' />"></script>
    <script src="<stripes:url value='/webjars/respond/1.4.2/dest/respond.min.js' />"></script>
    <script src="<stripes:url value='/webjars/excanvas/3/excanvas.compiled.js' />"></script>
<![endif]-->
<link rel="stylesheet" type="text/css" href="<stripes:url value='/webjars/bootstrap/3.3.7/css/bootstrap.min.css' />">
<link rel="stylesheet" type="text/css" href="<stripes:url value='/webjars/bootstrap/3.3.7/css/bootstrap-theme.min.css' />">
<link rel="stylesheet" type="text/css" href="<stripes:url value='/webjars/Eonasdan-bootstrap-datetimepicker/4.17.37/css/bootstrap-datetimepicker.min.css' />">
<link rel="stylesheet" type="text/css" href="<stripes:url value='/webjars/bootstrap-fileinput/4.2.0/css/fileinput.min.css' />">
<link rel="stylesheet" type="text/css" href="<stripes:url value='/webjars/jquery-ui/1.12.0/jquery-ui.structure.min.css' />">
<script type="text/javascript" src="<stripes:url value='/webjars/jquery/1.12.4/jquery.min.js' />" ></script>
<script type="text/javascript" src="<stripes:url value='/webjars/jquery-ui/1.12.0/jquery-ui.min.js' />" ></script>
<script type="text/javascript" src="<stripes:url value='/webjars/bootstrap/3.3.7/js/bootstrap.min.js' />" ></script>
<script type="text/javascript" src="<stripes:url value='/webjars/momentjs/2.10.3/min/moment-with-locales.min.js' />" ></script>
<script type="text/javascript" src="<stripes:url value='/webjars/Eonasdan-bootstrap-datetimepicker/4.17.37/js/bootstrap-datetimepicker.min.js' />" ></script>
<script type="text/javascript" src="<stripes:url value='/webjars/typeaheadjs/0.10.5/typeahead.bundle.min.js' />" ></script>
<script type="text/javascript" src="<stripes:url value='/webjars/bootstrap-fileinput/4.2.0/js/fileinput.min.js' />" ></script>
<!-- Charts with chart.js -->
<script type="text/javascript" src="<stripes:url value='/webjars/chartjs/1.0.2/Chart.min.js' />" ></script>
<script type="text/javascript" src="<stripes:url value='/m/chart/chartjs/chartjs-configuration.js' />" ></script>

<link rel="stylesheet" type="text/css" href="<stripes:url value='/theme/portofino.css' />">
<link type="text/css" rel="stylesheet" href="<stripes:url value="/m/openid/openid-selector/css/openid.css" />" />
<link type="text/css" rel="stylesheet" href="<stripes:url value="/m/openid/openid-form.css" />" />

<script type="text/javascript" src="<stripes:url value='/m/pageactions/portofino-messages.js' />" ></script>
<script type="text/javascript" src="<stripes:url value='/m/pageactions/portofino.js' />" ></script>
<script type="text/javascript" src="<stripes:url value='/m/crud/crud.js' />" ></script>
<script type="text/javascript">
    portofino.contextPath = '${pageContext.request.contextPath}';
</script>
<%
    BaseHref.emit(request, new XhtmlBuffer(out));
%>
<% if(!StringUtils.isEmpty(portofinoConfiguration.getString(PortofinoProperties.APP_LOGO))) { %>
<stripes:url var="logoUrl" value="<%= portofinoConfiguration.getString(PortofinoProperties.APP_LOGO) %>"/>
<link rel="shortcut icon" type="image/png" href="${logoUrl}" />
<% } %>
<title><c:out value='<%= request.getParameter("pageTitle") %>' escapeXml="false"  /></title>
</head>
