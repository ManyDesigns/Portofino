<%@ page import="com.manydesigns.portofino.head.HtmlHead"
%><%@ page import="com.manydesigns.portofino.head.HtmlHeadBuilder"
%><%@ page import="com.manydesigns.portofino.modules.BaseModule"
%><%@ page import="com.manydesigns.elements.xml.XhtmlFragment"
%><%@ page import="com.manydesigns.elements.xml.XhtmlBuffer"
%><%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/>
<meta name="viewport" content="width=device-width, initial-scale=1.0">

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

<%
    HtmlHeadBuilder htmlHeadBuilder = (HtmlHeadBuilder) application.getAttribute(BaseModule.HTML_HEAD_BUILDER);
    HtmlHead head = htmlHeadBuilder.build();

    XhtmlBuffer xhtmlBuffer = new XhtmlBuffer(out);
    for(XhtmlFragment fragment : head.fragments) {
        fragment.toXhtml(xhtmlBuffer);
    }

%>
<%-- jQuery & jQuery UI (for draggable/droppable) --%>
<script type="text/javascript"
        src="<stripes:url value="/elements/jquery/jquery.min.js"/>"></script>
<script type="text/javascript"
        src="<stripes:url value="/jquery-ui/js/jquery-ui-1.10.3.custom.min.js"/>"></script>
<%-- Twitter Bootstrap --%>
<script type="text/javascript"
        src="<stripes:url value="/elements/bootstrap/js/bootstrap.min.js"/>"></script>
<script type="text/javascript"
        src="<stripes:url value="/elements/datepicker/js/bootstrap-datepicker.js"/>"></script>

<script type="text/javascript"
        src="<stripes:url value="/elements/elements.js"/>"></script>