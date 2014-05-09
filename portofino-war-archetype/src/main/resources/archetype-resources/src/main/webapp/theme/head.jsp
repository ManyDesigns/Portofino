<%@ page import="com.manydesigns.elements.xml.XhtmlBuffer"
%><%@ page import="com.manydesigns.portofino.navigation.BaseHref"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"
%><%--

    This is only an example. You should customize your head.jsp depending on the installed modules.

--%><head>
<meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
<!--[if lt IE 9]>
  <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
<![endif]-->
<link rel="stylesheet" type="text/css" href="<stripes:url value='/theme/bootstrap/css/bootstrap.min.css' />">
<link rel="stylesheet" type="text/css" href="<stripes:url value='/theme/bootstrap/css/bootstrap-theme.min.css' />">
<link rel="stylesheet" type="text/css" href="<stripes:url value='/theme/datepicker/css/datepicker.css' />">
<link rel="stylesheet" type="text/css" href="<stripes:url value='/theme/jquery-ui/css/no-theme/jquery-ui-1.10.3.custom.min.css' />">
<script type="text/javascript" src="<stripes:url value='/theme/jquery/jquery.min.js' />" ></script>
<script type="text/javascript" src="<stripes:url value='/theme/bootstrap/js/bootstrap.min.js' />" ></script>
<script type="text/javascript" src="<stripes:url value='/theme/datepicker/js/bootstrap-datepicker.js' />" ></script>
<script type="text/javascript" src="<stripes:url value='/theme/typeahead/typeahead.bundle.min.js' />" ></script>
<script type="text/javascript" src="<stripes:url value='/theme/jquery-ui/js/jquery-ui-1.10.3.custom.min.js' />" ></script>

<link rel="stylesheet" type="text/css" href="<stripes:url value='/theme/portofino.css' />">
<link type="text/css" rel="stylesheet" href="<stripes:url value="/m/openid/openid-selector/css/openid.css" />" />
<style type="text/css">
    #openid_form { width: auto; }
    #openid_username { margin-right: .5em; }
    div#openid_highlight { padding: 0; }
</style>

<script type="text/javascript" src="<stripes:url value='/m/pageactions/portofino-messages.js' />" ></script>
<script type="text/javascript" src="<stripes:url value='/m/pageactions/portofino.js' />" ></script>
<script type="text/javascript" src="<stripes:url value='/m/crud/crud.js' />" ></script>
<script type="text/javascript">
    portofino.contextPath = '${pageContext.request.contextPath}';
</script>
<%
    BaseHref.emit(request, new XhtmlBuffer(out));
%>
<title><c:out value='<%= request.getParameter("pageTitle") %>' /></title>
</head>