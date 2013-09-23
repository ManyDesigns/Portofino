<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><stripes:layout-definition><%--
--%><!doctype html>
    <html lang="en">
    <head>
        <jsp:include page="../../head.jsp"/>
        <stripes:layout-component name="customScripts"/>
        <title>
            <stripes:layout-component name="pageTitle" />
        </title>
    </head>
    <body class="dialog">
    <stripes:layout-component name="dialogContainer" >
        <div class="dialog-container">
            <mde:sessionMessages/>
            <div class="dialogHeader">
                <stripes:layout-component name="dialogHeader" >
                    <h2>
                        <stripes:layout-component name="dialogTitle" />
                    </h2>
                </stripes:layout-component>
            </div>
            <div class="dialogBody spacingTop">
                <stripes:layout-component name="dialogBody" />
            </div>
            <div class="dialogFooter spacingWithDividerTop">
                <stripes:layout-component name="dialogFooter" >
                    <jsp:useBean id="portofinoConfiguration" scope="application"
                                 type="org.apache.commons.configuration.Configuration"/>
                    Powered by <a href="http://www.manydesigns.com/">Portofino</a>
                    <c:out value="${mde:getString(portofinoConfiguration, 'portofino.version')}"/>
                </stripes:layout-component>
            </div>
        </div>
    </stripes:layout-component>
    </body>
</html>
</stripes:layout-definition>