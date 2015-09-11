<%@ page import="com.manydesigns.portofino.modules.ModuleRegistry" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld" %>
<%@taglib prefix="mde" uri="/manydesigns-elements" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.admin.info.InfoAction"/>
<stripes:layout-render name="/m/admin/admin-theme/admin-page.jsp">
    <stripes:layout-component name="pageTitle">
        <fmt:message key="info"/>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">

        <div class="jumbotron">
            <div class="container">
                <h1>Portofino <c:out value="<%= ModuleRegistry.getPortofinoVersion() %>"/></h1>
                <p> <fmt:message key="info.portofino.intro" /> </p>
                <p> <fmt:message key="info.portofino.features" /> </p>
                <br>
                <div class="btn-group" role="group" aria-label="...">
                    <a class="btn btn-success btn-lg"  target="_blank" href="http://portofino.manydesigns.com/en/support" role="button">
                        <span class="glyphicon glyphicon-shopping-cart" aria-hidden="true"></span> Get support
                    </a>

                    <a class="btn btn-primary btn-lg" target="_blank" href="http://portofino.manydesigns.com/en/docs" role="button">
                        <span class="glyphicon glyphicon-book" aria-hidden="true"></span> Documentation
                    </a>

                    <a class="btn btn-info btn-lg" target="_blank"  href="https://tt.manydesigns.com/wiki" role="button">
                        <span class="glyphicon glyphicon-globe" aria-hidden="true"></span> Wiki
                    </a>

                    <a class="btn btn-danger btn-lg" target="_blank"  href="https://tt.manydesigns.com/" role="button">
                        <span class="glyphicon glyphicon-tags" aria-hidden="true"></span> Ticket Tracker
                    </a>
                </div>
            </div>
        </div>

        <style type="text/css">
            .jumbotron .h1, .jumbotron h1 { font-size: 43px; }
        </style>

    </stripes:layout-component>
</stripes:layout-render>