<%@ page import="org.apache.shiro.SecurityUtils" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<stripes:layout-definition><%--
--%><!doctype html>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en">
    <head>
        <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/>
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
              href="<stripes:url value="/portofino-base/admin-theme/portofino-admin.css"/>"/>
        <%-- jQuery & jQuery UI (for draggable/droppable) --%>
        <script type="text/javascript"
                src="<stripes:url value="/elements/jquery/jquery.min.js"/>"></script>
        <%--<script type="text/javascript"
                src="<stripes:url value="/jquery-ui/js/jquery-ui-1.10.3.custom.min.js"/>"></script>--%>
        <%-- Twitter Bootstrap --%>
        <script type="text/javascript"
                src="<stripes:url value="/elements/bootstrap/js/bootstrap.min.js"/>"></script>
        <script type="text/javascript"
                src="<stripes:url value="/elements/datepicker/js/bootstrap-datepicker.js"/>"></script>

        <script type="text/javascript"
                src="<stripes:url value="/elements/elements.js"/>"></script>
        <stripes:layout-component name="customScripts"/>
        <title>
            <stripes:layout-component name="pageTitle" />
        </title>
    </head>
    <body>
    <fmt:setLocale value="${pageContext.request.locale}"/>
    <div class="navbar navbar-inverse navbar-fixed-top">
        <div class="navbar-inner">
            <div class="container">
                <h4 id="app-title" class="pull-left"><stripes:link href="/">Administration</stripes:link></h4>
            </div>
        </div>
    </div>
    <div class="container">
        <div class="row">
            <div class="span2">
                <div id="navigation">
                    <ul class="nav nav-list portofino-sidenav">
                        <li class="nav-header">Security</li>
                        <stripes:layout-render name="adminLink.jsp"
                                               text="Root permissions"
                                               link="/actions/admin/root-page/permissions"/>
                        <li class="nav-header">Configuration</li>
                        <stripes:layout-render name="adminLink.jsp"
                                               text="Settings"
                                               link="/actions/admin/settings"/>
                        <stripes:layout-render name="adminLink.jsp"
                                               text="Top-level pages"
                                               link="/actions/admin/root-page/children"/>
                        <li class="nav-header">Data modeling</li>
                        <stripes:layout-render name="adminLink.jsp"
                                               text="Wizard"
                                               link="/actions/admin/wizard"/>
                        <stripes:layout-render name="adminLink.jsp"
                                               text="Connection providers"
                                               link="/actions/admin/connection-providers"/>
                        <stripes:layout-render name="adminLink.jsp"
                                               text="Tables"
                                               link="/actions/admin/tables"/>
                        <stripes:layout-render name="adminLink.jsp"
                                               text="Reload model"
                                               link="/actions/admin/reload-model"/>
                    </ul>
                </div>
            </div>
            <div id="content" class="span10">
                <div class="row-fluid">
                    <mde:sessionMessages/>
                    <stripes:layout-component name="portletHeader">
                        <h3 style="border-bottom: 1px solid #E5E5E5">
                            <stripes:layout-component name="portletTitle" />
                        </h3>
                    </stripes:layout-component>
                    <stripes:layout-component name="portletBody" />
                </div>
            </div>
        </div>
    </div>
    <footer>
        <div class="container">
            Powered by <a href="http://www.manydesigns.com/">Portofino</a>
            <c:out value="${mde:getString(portofinoConfiguration, 'portofino.version')}"/>
        </div>
    </footer>
    </body>
    </html>
</stripes:layout-definition>