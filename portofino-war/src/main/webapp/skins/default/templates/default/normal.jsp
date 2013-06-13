<%@ page import="java.util.Map"
%>
<%@ page import="net.sourceforge.stripes.util.UrlBuilder" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><stripes:layout-definition><%--
--%><!DOCTYPE html>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en">
    <head>
        <jsp:include page="../../head.jsp"/>
        <!--<link rel="stylesheet" type="text/css"
              href="<stripes:url value="/skins/${skin}/templates/site/site.css"/>"/>-->
        <stripes:layout-component name="customScripts"/>
        <jsp:useBean id="actionBean" scope="request"
                     type="com.manydesigns.portofino.pageactions.AbstractPageAction"/>
        <title><c:out value="${actionBean.dispatch.lastPageInstance.page.description}"/></title>
    </head>
    <body>
    <jsp:include page="../../header.jsp"/>
    <div class="container">
        <div class="row">
            <div class="span2 portofino-sidebar">
                <portofino:portlets list="aboveNavigation" />
                <div id="navigation">
                    <jsp:include page="../../navigation.jsp" />
                </div>
                <portofino:portlets list="belowNavigation" />
            </div>
            <div id="content" class="span10">
                <stripes:layout-component name="contentHeader">
                    <div class="row-fluid">
                        <mde:sessionMessages/>
                        <jsp:include page="../../breadcrumbs.jsp" />
                        <c:if test="${not empty actionBean.returnToParentTarget}">
                            <%
                                UrlBuilder urlBuilder =
                                        new UrlBuilder(request.getLocale(), actionBean.dispatch.getAbsoluteOriginalPath(), true);
                                urlBuilder.addParameters(actionBean.getReturnToParentParams());
                                urlBuilder.setEvent("returnToParent");
                            %>
                            <a href="<%= urlBuilder.toString() %>"
                               class="btn btn-small pull-right">
                                &lt;&lt; <%= actionBean.getMessage("commons.returnToParent", actionBean.returnToParentTarget) %>
                            </a>
                        </c:if>
                        <%--
                        <div class="text-right">
                            <portofino:result-set-navigation />
                        </div>
                    --%>
                    </div>
                    <div style="margin-bottom: 10px;"><!-- Separator --></div>
                </stripes:layout-component>
                <stripes:layout-component name="contentBody">
                    <div class="row-fluid">
                        <portofino:portlets list="default" cssClass="row-fluid" />
                        <div class="row-fluid">
                            <portofino:portlets list="contentLayoutLeft" cssClass="span6" />
                            <portofino:portlets list="contentLayoutRight" cssClass="span6" />
                        </div>
                        <portofino:portlets list="contentLayoutBottom" cssClass="row-fluid" />
                    </div>
                </stripes:layout-component>
                <stripes:layout-component name="contentFooter" />
            </div>
        </div>
    </div>
    <jsp:include page="../../footer.jsp"/>
    </body>
    </html>
</stripes:layout-definition>