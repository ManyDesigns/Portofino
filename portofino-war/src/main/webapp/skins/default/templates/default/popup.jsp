<%@ page import="com.manydesigns.portofino.dispatcher.Dispatch"
%><%@ page import="com.manydesigns.portofino.dispatcher.DispatcherUtil"
%><%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><stripes:layout-definition><%--
--%><!DOCTYPE html>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en">
    <head>
        <jsp:include page="../../head.jsp"/>
        <stripes:layout-component name="customScripts"/>
        <jsp:useBean id="actionBean" scope="request" type="net.sourceforge.stripes.action.ActionBean" />
        <%
            Dispatch dispatch = DispatcherUtil.getDispatch(request, actionBean);
            pageContext.setAttribute("dispatch", dispatch);
        %>
        <style type="text/css">
            body {
                padding-top: 0;
            }
        </style>
        <title>
            <stripes:layout-component name="pageTitle">
                <c:out value="${dispatch.lastPageInstance.page.description}"/>
            </stripes:layout-component>
        </title>
    </head>
    <body>
    <div class="container">
        <div class="row">
            <div id="content" class="span12">
                <div class="row-fluid">
                    <stripes:layout-component name="beforeSessionMessages" />
                    <mde:sessionMessages />
                    <stripes:layout-component name="beforeBreadcrumbs" />
                    <stripes:layout-component name="afterBreadcrumbs" />
                    <div>
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
    </div>
    </body>
    </html>
</stripes:layout-definition>