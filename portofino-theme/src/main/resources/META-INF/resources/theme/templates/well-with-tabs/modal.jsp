<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><stripes:layout-definition><%--
--%><!DOCTYPE html>
    <html lang="en">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.dispatcher.PageAction" />
    <head>
        <jsp:include page="/theme/head.jsp"/>
        <title>
            <stripes:layout-component name="pageTitle">
                <c:out value="${actionBean.pageInstance.description}"/>
            </stripes:layout-component>
        </title>
    </head>
    <body>
    <jsp:include page="/theme/header.jsp"/>
    <div class="container">
        <div class="row">
            <div class="span2">
                <stripes:layout-component name="sidebar">
                    <div class="navigation">
                        <jsp:include page="/theme/navigation.jsp" />
                    </div>
                </stripes:layout-component>
            </div>
            <div class="content span10">
                <stripes:layout-component name="mainPageAction">
                    <stripes:layout-component name="mainPageActionHeader">
                        <stripes:layout-component name="beforeSessionMessages" />
                        <mde:sessionMessages />
                        <stripes:layout-component name="afterSessionMessages" />
                        <stripes:layout-component name="beforeBreadcrumbs" />
                        <jsp:include page="/theme/breadcrumbs.jsp" />
                        <stripes:layout-component name="afterBreadcrumbs" />
                    </stripes:layout-component>
                    <stripes:layout-component name="mainPageActionBody">
                        <div>
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
    <jsp:include page="/theme/footer.jsp"/>
    </body>
    </html>
</stripes:layout-definition>