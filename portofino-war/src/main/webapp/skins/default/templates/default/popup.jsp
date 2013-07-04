<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><stripes:layout-definition><%--
--%><!DOCTYPE html>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en">
    <head>
        <jsp:include page="../../head.jsp"/>
        <stripes:layout-component name="customScripts"/>
        <style type="text/css">
            body {
                padding-top: 0;
            }
        </style>
        <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.dispatcher.PageAction" />
        <title>
            <stripes:layout-component name="pageTitle">
                <c:out value="${actionBean.dispatch.lastPageInstance.page.description}"/>
            </stripes:layout-component>
        </title>
    </head>
    <body>
    <div class="container">
        <div class="row">
            <div id="content" class="span12">
                <stripes:layout-component name="mainPageAction">
                    <stripes:layout-component name="mainPageActionHeader">
                        <stripes:layout-component name="beforeSessionMessages" />
                        <mde:sessionMessages />
                        <stripes:layout-component name="beforeBreadcrumbs" />
                        <stripes:layout-component name="afterBreadcrumbs" />
                    </stripes:layout-component>
                    <stripes:layout-component name="mainPageActionBody">
                        <div id="portlet_${actionBean.pageInstance.page.id}">
                            <stripes:layout-component name="portletHeader">
                                <div class="portletHeader" style="padding-bottom: 0;">
                                    <h3>
                                        <stripes:layout-component name="portletTitle" />
                                    </h3>
                                </div>
                            </stripes:layout-component>
                            <div class="portletBody">
                                <stripes:layout-component name="portletBody" />
                            </div>
                        </div>
                    </stripes:layout-component>
                    <stripes:layout-component name="mainPageActionFooter" />
                </stripes:layout-component>
            </div>
        </div>
    </div>
    </body>
    </html>
</stripes:layout-definition>