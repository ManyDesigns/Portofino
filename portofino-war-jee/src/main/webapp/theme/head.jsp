<%@ page import="com.manydesigns.elements.xml.XhtmlBuffer"
%><%@ page import="com.manydesigns.portofino.navigation.BaseHref"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"
%><%--

    This is only an example. You should customize your head.jsp depending on the installed modules.

--%>
<meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
<!--[if lt IE 9]>
  <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
<![endif]-->
<link rel="stylesheet" type="text/css" href="<stripes:url value='/theme/bootstrap/css/bootstrap.min.css' />">
<link rel="stylesheet" type="text/css" href="<stripes:url value='/theme/bootstrap/css/bootstrap-responsive.min.css' />">
<link rel="stylesheet" type="text/css" href="<stripes:url value='/theme/datepicker/css/datepicker.css' />">
<link rel="stylesheet" type="text/css" href="<stripes:url value='/theme/jquery-ui/css/no-theme/jquery-ui-1.10.3.custom.min.css' />">
<script type="text/javascript" src="<stripes:url value='/theme/jquery/jquery.min.js' />" ></script>
<script type="text/javascript" src="<stripes:url value='/theme/bootstrap/js/bootstrap.min.js' />" ></script>
<script type="text/javascript" src="<stripes:url value='/theme/datepicker/js/bootstrap-datepicker.js' />" ></script>
<script type="text/javascript" src="<stripes:url value='/theme/jquery-ui/js/jquery-ui-1.10.3.custom.min.js' />" ></script>

<link rel="stylesheet" type="text/css" href="<stripes:url value='/theme/portofino.css' />">

<script type="text/javascript" src="<stripes:url value='/m/pageactions/portofino.js' />" ></script>
<script type="text/javascript">
    portofino.contextPath = '${pageContext.request.contextPath}';
</script>
<%
    BaseHref.emit(request, new XhtmlBuffer(out));
%>
<style type="text/css">
@media (min-width: 980px) {
    body {
        padding-top: 50px;
    }
}
</style>