<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><stripes:layout-definition><%--
--%><!doctype html>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en">
    <head>
        <jsp:include page="head.jsp"/>
        <style type="text/css">
            body {
                padding-top: 40px;
                padding-bottom: 40px;
                background-color: #f5f5f5;
            }

            .login-container {
                max-width: 360px;
                padding: 19px 29px 29px;
                margin: 0 auto 20px;
                background-color: #ffffff;
                border: 1px solid #e5e5e5;
                -webkit-border-radius: 5px;
                   -moz-border-radius: 5px;
                        border-radius: 5px;
                -webkit-box-shadow: 0 1px 2px rgba(0,0,0,.05);
                   -moz-box-shadow: 0 1px 2px rgba(0,0,0,.05);
                        box-shadow: 0 1px 2px rgba(0,0,0,.05);
            }

            .spacingTop {
                margin-top: 20px;
            }

            .spacingWithDividerTop {
                margin-top: 20px;
                border-top: 1px solid #ddd;
                padding-top: 20px;
            }
        </style>
        <stripes:layout-component name="customScripts"/>
        <jsp:useBean id="app" scope="request"
                     type="com.manydesigns.portofino.application.Application"/>
        <title><fmt:message key="skins.default.login.login_to"/> <c:out value="${app.name}"/></title>
    </head>
    <body>
    <stripes:layout-component name="loginContainer" >
        <div class="login-container">
            <mde:sessionMessages/>
            <div class="loginHeader">
                <stripes:layout-component name="loginHeader" >
                    <h2>
                        <stripes:layout-component name="loginTitle">
                            Please log in
                        </stripes:layout-component>
                    </h2>
                </stripes:layout-component>
            </div>
            <div class="loginBody spacingTop">
                <stripes:layout-component name="loginBody" >
                    Sample login body
                </stripes:layout-component>
            </div>
            <div class="loginFooter spacingWithDividerTop">
                <stripes:layout-component name="loginFooter" >
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