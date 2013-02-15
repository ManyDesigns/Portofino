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
            <stripes:layout-component name="pageTitle">
                Page title
            </stripes:layout-component>
        </title>
    </head>
    <body class="yui-skin-sam">
    <div id="doc3" class="yui-t2">
        <div id="hd">
            <jsp:include page="header.jsp"/>
        </div>
        <div id="bd">
            <div id="yui-main">
                <div id="content" class="yui-b">
                    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.admin.AdminAction"/>
                    <stripes:form action="${actionBean.actionPath}" method="post" enctype="multipart/form-data">
                        <stripes:layout-component name="contentHeaderContainer">
                            <div class="contentHeader">
                                <stripes:layout-component name="contentHeader">
                                    Content header
                                </stripes:layout-component>
                            </div>
                        </stripes:layout-component>
                        <div class="contentBody">
                            <div class="portletWrapper">
                                <div class="portlet">
                                    <mde:sessionMessages/>
                                    <div class="portletHeader">
                                        <stripes:layout-component name="portletHeader">
                                            <div>
                                                <div class="portletTitle">
                                                    <h1>
                                                    <stripes:layout-component name="portletTitle">
                                                        portletTitle
                                                    </stripes:layout-component>
                                                    </h1>
                                                </div>
                                                <div class="portletHeaderButtons">
                                                    <portofino:buttons list="portletHeaderButtons" />
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
                                        </stripes:layout-component>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <stripes:layout-component name="contentFooterContainer">
                            <div class="contentFooter">
                                <stripes:layout-component name="contentFooter">
                                    Content footer
                                </stripes:layout-component>
                            </div>
                        </stripes:layout-component>
                    </stripes:form>
                </div>
            </div>
            <div id="sidebar" class="yui-b adminSidebar">
                <ul>
                    <%--<li>
                        <div class="navigationHeader first">Site content</div>
                        <ul>
                            <stripes:layout-render name="/skins/${skin}/adminLink.jsp"
                                                   text="Recent site activity"
                                                   link="/"/>
                            <stripes:layout-render name="/skins/${skin}/adminLink.jsp"
                                                   text="Pages"
                                                   link="/"/>
                            <stripes:layout-render name="/skins/${skin}/adminLink.jsp"
                                                   text="Attachments"
                                                   link="/"/>
                        </ul>
                    </li>--%>
                    <li>
                        <div class="navigationHeader">Security</div>
                        <ul>
                            <stripes:layout-render name="/skins/${skin}/adminLink.jsp"
                                                   text="Root permissions"
                                                   link="/actions/admin/root-page/permissions"/>
                        </ul>
                    </li>
                    <li>
                        <div class="navigationHeader">Configuration</div>
                        <ul>
                            <stripes:layout-render name="/skins/${skin}/adminLink.jsp"
                                                   text="Settings"
                                                   link="/actions/admin/settings"/>
                            <stripes:layout-render name="/skins/${skin}/adminLink.jsp"
                                                   text="Top-level pages"
                                                   link="/actions/admin/root-page/children"/>
                        </ul>
                    </li>
                    <li>
                        <div class="navigationHeader">Data modeling</div>
                        <ul>
                            <stripes:layout-render name="/skins/${skin}/adminLink.jsp"
                                                   text="Wizard"
                                                   link="/actions/admin/wizard"/>
                            <stripes:layout-render name="/skins/${skin}/adminLink.jsp"
                                                   text="Connection providers"
                                                   link="/actions/admin/connection-providers"/>
                            <stripes:layout-render name="/skins/${skin}/adminLink.jsp"
                                                   text="Tables"
                                                   link="/actions/admin/tables"/>
                            <stripes:layout-render name="/skins/${skin}/adminLink.jsp"
                                                   text="Reload model"
                                                   link="/actions/admin/reload-model"/>
                        </ul>
                    </li>
                </ul>
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