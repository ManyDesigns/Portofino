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
        <link rel="stylesheet" type="text/css"
              href="<stripes:url value="/skins/${skin}/templates/site-no-sidebar/site.css"/>"/>
        <stripes:layout-component name="customScripts"/>
        <jsp:useBean id="actionBean" scope="request"
                     type="com.manydesigns.portofino.pageactions.AbstractPageAction"/>
        <title><c:out value="${actionBean.dispatch.lastPageInstance.page.description}"/></title>
    </head>
    <body class="yui-skin-sam">
    <div id="doc4" class="yui-t2">
        <div id="hd">
            <jsp:include page="../../header.jsp"/>
            <div class="tabs">
                <jsp:include page="tabs.jsp">
                    <jsp:param name="tabsMaxLevels" value="1" />
                    <jsp:param name="tabsIncludeAdminButtons" value="true" />
                </jsp:include>
            </div>
        </div>
        <div id="bd">
            <div id="yui-main">
                <div id="content" class="yui-b">
                    <div class="contentBody">
                        <stripes:layout-component name="contentBody">
                            <div class="portlets">
                                <mde:sessionMessages/>
                                <portofino:portlets list="default" />
                                <div class="yui-g first">
                                    <portofino:portlets list="contentLayoutLeft" cssClass="yui-u first" />
                                    <portofino:portlets list="contentLayoutRight" cssClass="yui-u" />
                                </div>
                                <portofino:portlets list="contentLayoutBottom" />
                            </div>
                        </stripes:layout-component>
                    </div>
                </div>
            </div>
        </div>
        <div id="ft">
            <jsp:include page="../../footer.jsp"/>
        </div>
    </div>
    </body>
    </html>
</stripes:layout-definition>