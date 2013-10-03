<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><stripes:layout-definition><%--
--%><!doctype html>
    <html lang="en">
    <head>
        <jsp:include page="head.jsp"/>
        <stripes:layout-component name="customScripts"/>
        <title>
            <stripes:layout-component name="pageTitle" />
        </title>
    </head>
    <body>
    <jsp:include page="header.jsp"/>
    <div class="container">
        <div class="row">
            <div class="span2">
                <div id="navigation">
                    <jsp:include page="navigation.jsp" />
                </div>
            </div>
            <div id="content" class="span10">
                <jsp:useBean id="actionBean" scope="request"
                             type="com.manydesigns.portofino.actions.admin.appwizard.ApplicationWizard"/>
                <stripes:form action="${actionBean.context.actualServletPath}"
                              method="post" enctype="multipart/form-data" class="form-horizontal">
                    <jsp:include page="wizard-content-header.jsp" />
                    <div class="row-fluid">
                        <stripes:layout-component name="contentHeader" />
                    </div>
                    <div class="row-fluid">
                        <div>
                            <mde:sessionMessages/>
                            <div class="portletHeader">
                                <stripes:layout-component name="portletHeader">
                                    <h4>
                                        <span class="pull-right btn-group">
                                            <portofino:buttons list="portletHeaderButtons" cssClass="btn-mini" />
                                        </span>
                                        <stripes:layout-component name="portletTitle" />
                                    </h4>
                                </stripes:layout-component>
                            </div>
                            <div class="portletBody">
                                <stripes:layout-component name="portletBody" />
                            </div>
                        </div>
                    </div>
                    <div class="row-fluid">
                        <stripes:layout-component name="contentFooter" />
                    </div>
                </stripes:form>
            </div>
        </div>
        <div class="row">
            <jsp:include page="footer.jsp"/>
        </div>
    </div>
    </body>
    </html>
</stripes:layout-definition>