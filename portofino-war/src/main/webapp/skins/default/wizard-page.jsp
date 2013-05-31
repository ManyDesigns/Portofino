<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><stripes:layout-definition><%--
--%><!doctype html>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en">
    <head>
        <jsp:include page="head.jsp"/>
        <stripes:layout-component name="customScripts"/>
        <title>
            <stripes:layout-component name="pageTitle" />
        </title>
    </head>
    <body>
    <div class="container">
        <div class="row">
            <jsp:include page="header.jsp"/>
        </div>
        <div class="row">
            <div class="span2 portofino-sidebar">
                <div id="navigation">
                    <jsp:include page="navigation.jsp" />
                </div>
            </div>
            <div id="content" class="span10">
                <jsp:useBean id="actionBean" scope="request"
                             type="com.manydesigns.portofino.pageactions.wizard.AbstractWizardPageAction"/>
                <stripes:form action="${actionBean.dispatch.originalPath}"
                              method="post" enctype="multipart/form-data" class="form-horizontal">
                    <jsp:include page="wizard-content-header.jsp" />
                    <div class="contentHeader row-fluid">
                        <stripes:layout-component name="contentHeader" />
                    </div>
                    <div class="contentBody row-fluid">
                        <div class="portletWrapper noSpacing">
                            <div class="portlet">
                                <mde:sessionMessages/>
                                <div class="portletHeader">
                                    <stripes:layout-component name="portletHeader">
                                        <div>
                                            <div class="portletTitle">
                                                <h4><stripes:layout-component name="portletTitle" /></h4>
                                            </div>
                                            <div class="portletHeaderButtons">
                                                <portofino:buttons list="portletHeaderButtons" />
                                            </div>
                                        </div>
                                    </stripes:layout-component>
                                </div>
                                <div class="portletBody">
                                    <stripes:layout-component name="portletBody" />
                                </div>
                                <div class="portletFooter">
                                    <stripes:layout-component name="portletFooter" />
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="contentFooter row-fluid">
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