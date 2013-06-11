<%@ page import="com.manydesigns.portofino.dispatcher.Dispatch" %>
<%@ page import="com.manydesigns.portofino.dispatcher.Dispatcher" %>
<%@ page import="java.net.MalformedURLException" %>
<%@ page import="java.net.URL" %>
<%@ page import="com.manydesigns.portofino.dispatcher.DispatcherUtil" %>
<%@ page import="com.manydesigns.portofino.dispatcher.PageAction" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"%>
<meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/>

<link rel="stylesheet" type="text/css"
      href="<stripes:url value="/jquery-ui/css/no-theme/jquery-ui-1.10.3.custom.min.css"/>"/>

<link rel="stylesheet" type="text/css"
      href="<stripes:url value="/elements/bootstrap/css/bootstrap.min.css"/>"/>
<style type="text/css">
    body {
        padding-top: 50px;
    }
</style>
<link rel="stylesheet" type="text/css"
      href="<stripes:url value="/elements/bootstrap/css/bootstrap-responsive.min.css"/>"/>
<link rel="stylesheet" type="text/css"
      href="<stripes:url value="/elements/datepicker/css/datepicker.css"/>"/>

<!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
<!--[if lt IE 9]>
      <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
<![endif]-->
<link rel="stylesheet" type="text/css"
      href="<stripes:url value="/skins/${skin}/portofino.css"/>"/>
<%-- jQuery & jQuery UI (for draggable/droppable) --%>
<script type="text/javascript"
        src="<stripes:url value="/elements/jquery/jquery.min.js"/>"></script>
<script type="text/javascript"
        src="<stripes:url value="/jquery-ui/js/jquery-ui-1.10.3.custom.min.js"/>"></script>
<%-- Twitter Bootstrap --%>
<script type="text/javascript"
        src="<stripes:url value="/elements/bootstrap/js/bootstrap.js"/>"></script>
<script type="text/javascript"
        src="<stripes:url value="/elements/datepicker/js/bootstrap-datepicker.js"/>"></script>

<script type="text/javascript"
        src="<stripes:url value="/elements/elements.js"/>"></script>
<script type="text/javascript"
        src="<stripes:url value="/skins/${skin}/portofino.js.jsp"/>"></script>
<jsp:useBean id="actionBean" scope="request" type="net.sourceforge.stripes.action.ActionBean" />
<%
    Dispatch dispatch = DispatcherUtil.getDispatch(request, actionBean);
    if(dispatch != null) {
        String baseHref = dispatch.getAbsoluteOriginalPath();
        //Remove all trailing slashes
        while (baseHref.length() > 1 && baseHref.endsWith("/")) {
            baseHref = baseHref.substring(0, baseHref.length() - 1);
        }
        //Add a single trailing slash so all relative URLs use this page as the root
        baseHref += "/";
        //Try to make the base HREF absolute
        try {
            URL url = new URL(request.getRequestURL().toString());
            String port = url.getPort() > 0 ? ":" + url.getPort() : "";
            baseHref = url.getProtocol() + "://" + url.getHost() + port + baseHref;
        } catch (MalformedURLException e) {
            //Ignore
        }
        %><base href="<%= baseHref %>" /><%
    }
%>
