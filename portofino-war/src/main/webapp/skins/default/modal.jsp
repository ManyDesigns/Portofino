<%
    // Avoid caching of dynamic pages
    response.setHeader("Pragma", "no-cache");
    response.addHeader("Cache-Control", "must-revalidate");
    response.addHeader("Cache-Control", "no-cache");
    response.addHeader("Cache-Control", "no-store");
    response.setDateHeader("Expires", 0);
%>
<%@ page contentType="text/html;charset=ISO-8859-1" language="java" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<%@ taglib prefix="mde" uri="/manydesigns-elements"%>
<stripes:layout-definition><%--
--%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en">
    <head>
        <jsp:include page="head.jsp"/>
        <stripes:layout-component name="customScripts"/>
        <jsp:useBean id="dispatch" scope="request"
                     type="com.manydesigns.portofino.dispatcher.Dispatch"/>
        <title><c:out value="${dispatch.lastSiteNodeInstance.siteNode.description}"/></title>
    </head>
    <body>
    <div id="doc3" class="yui-t2">
        <stripes:url var="profileUrl" value="/Profile.action"/>
        <jsp:useBean id="portofinoConfiguration" scope="application"
                     type="org.apache.commons.configuration.Configuration"/>
        <div id="hd">
            <div id="globalLinks">
                <c:if test="${mde:getBoolean(portofinoConfiguration, 'user.enabled')}">
                    <c:if test="${not empty userId}">
                        <stripes:link href="/Profile.action"><c:out
                                value="${userName}"/></stripes:link> -
                        <stripes:link
                                href="/user/Settings.action">Settings</stripes:link> -
                        <stripes:link
                                href="/user/Help.action">Help</stripes:link> -
                        <stripes:link
                                href="/user/login.action?logout=">Log out</stripes:link>
                    </c:if><c:if test="${empty userId}">
                    <stripes:link href="/user/Help.action">Help</stripes:link> -
                    <stripes:link
                            href="/user/login.action">Log in</stripes:link>
                </c:if>
                </c:if><c:if
                    test="${not mde:getBoolean(portofinoConfiguration, 'user.enabled')}">
                <stripes:link href="/user/Help.action">Help</stripes:link>
            </c:if>
            </div>
            <div style="position: absolute; left: 20em;">
                <mde:sessionMessages/>
            </div>
            <h1><stripes:link href="/"><c:out
                    value="${mde:getString(portofinoConfiguration, 'application.name')}"/></stripes:link></h1>
        </div>
        <div id="bd">
            <div id="yui-main">
                <div id="content" class="yui-b">
                    <jsp:useBean id="actionBean" scope="request"
                                 type="com.manydesigns.portofino.actions.PortletAction"/>
                    <stripes:form
                            action="${actionBean.dispatch.absoluteOriginalPath}"
                            method="post"
                            enctype="${actionBean.multipartRequest ? 'multipart/form-data' : 'application/x-www-form-urlencoded'}">
                        <div class="portletPageHeader">
                            <stripes:layout-component name="portletPageHeader">
                                Portlet page header
                            </stripes:layout-component>
                        </div>
                        <div id="portletPageBody">
                            <div class="portletWrapper">
                                <div class="portlet">
                                    <div class="portletHeader">
                                        <stripes:layout-component name="portletHeader">
                                            <div class="yui-g">
                                                <div class="portletTitle">
                                                    <h1>
                                                    <stripes:layout-component name="portletTitle">
                                                        portletTitle
                                                    </stripes:layout-component>
                                                    </h1>
                                                </div>
                                                <div class="portletHeaderButtons">
                                                    <button class="wrench">Prova</button>
                                                </div>
                                            </div>
                                        </stripes:layout-component>
                                    </div>
                                    <div class="portletBody">
                                        <stripes:layout-component name="portletBody">
                                            Portlet body
                                        </stripes:layout-component>
                                    </div>
                                    <div class="portletFooter">
                                        <stripes:layout-component name="portletFooter">
                                            Portlet footer
                                        </stripes:layout-component>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="portletPageFooter">
                            <stripes:layout-component name="portletPageFooter">
                                Portlet page footer
                            </stripes:layout-component>
                        </div>
                    </stripes:form>
                </div>
            </div>
            <div id="sidebar" class="yui-b">
                <mde:write name="navigation"/>
            </div>
            <script type="text/javascript">
                fixSideBar();
            </script>
        </div>
        <div id="ft">
            <jsp:include page="footer.jsp"/>
        </div>
    </div>
    </body>
    </html>
</stripes:layout-definition>