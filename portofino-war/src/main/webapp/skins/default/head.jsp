<%@ page import="com.manydesigns.portofino.dispatcher.Dispatch" %>
<%@ page import="com.manydesigns.portofino.dispatcher.Dispatcher" %>
<%@ page import="java.net.MalformedURLException" %>
<%@ page import="java.net.URL" %>
<%@ page import="com.manydesigns.portofino.dispatcher.DispatcherUtil" %>
<%@ page import="com.manydesigns.portofino.dispatcher.PageAction" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"%>
<meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/>
<%--<meta http-equiv="Content-Script-Type" content="text/javascript"/>
<meta http-equiv="Content-Style-Type" content="text/css"/>--%>
<link rel="stylesheet" type="text/css"
      href="<stripes:url value="/yui-2.9.0/build/reset-fonts-grids/reset-fonts-grids.css"/>"/>
<link rel="stylesheet" type="text/css"
      href="<stripes:url value="/yui-2.9.0/build/base/base-min.css"/>"/>
<link rel="stylesheet" type="text/css"
      href="<stripes:url value="/yui-2.9.0/build/datatable/assets/skins/sam/datatable.css"/>">
<link rel="stylesheet" type="text/css"
      href="<stripes:url value="/yui-2.9.0/build/paginator/assets/skins/sam/paginator.css"/>">

<link rel="stylesheet" type="text/css"
      href="<stripes:url value="/jquery-ui-1.8.21/css/smoothness/jquery-ui-1.8.21.custom.css"/>"/>

<link rel="stylesheet" type="text/css"
      href="<stripes:url value="/skins/${skin}/portofino.css"/>"/>

<script type="text/javascript"
        src="<stripes:url value="/jquery-ui-1.8.21/js/jquery-1.7.2.min.js"/>"></script>
<script type="text/javascript"
        src="<stripes:url value="/jquery-ui-1.8.21/js/jquery-ui-1.8.21.custom.min.js"/>"></script>

<script type="text/javascript"
        src="<stripes:url value="/yui-2.9.0/build/yahoo-dom-event/yahoo-dom-event.js"/>"></script>
<script type="text/javascript"
        src="<stripes:url value="/yui-2.9.0/build/element/element-min.js"/>"></script>
<script type="text/javascript"
        src="<stripes:url value="/yui-2.9.0/build/datasource/datasource-min.js"/>"></script>
<script type="text/javascript"
        src="<stripes:url value="/yui-2.9.0/build/json/json-min.js"/>"></script>
<script type="text/javascript"
        src="<stripes:url value="/yui-2.9.0/build/connection/connection-min.js"/>"></script>
<script type="text/javascript"
        src="<stripes:url value="/yui-2.9.0/build/datatable/datatable-min.js"/>"></script>
<script type="text/javascript"
        src="<stripes:url value="/yui-2.9.0/build/paginator/paginator-min.js"/>"></script>

<script type="text/javascript"
        src="<stripes:url value="/elements.js"/>"></script>
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
