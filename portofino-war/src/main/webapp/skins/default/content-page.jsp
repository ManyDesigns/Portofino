<%
    // Avoid caching of dynamic pages
    response.setHeader("Pragma", "no-cache");
    response.addHeader("Cache-Control", "must-revalidate");
    response.addHeader("Cache-Control", "no-cache");
    response.addHeader("Cache-Control", "no-store");
    response.setDateHeader("Expires", 0);
%><%@ page import="com.manydesigns.portofino.logic.SecurityLogic"
%><%@ page import="java.util.Map"
%><%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><stripes:layout-definition><%--
--%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en">
    <head>
        <jsp:include page="head.jsp"/>
        <stripes:layout-component name="customScripts"/>
        <jsp:useBean id="actionBean" scope="request"
             type="com.manydesigns.portofino.pageactions.AbstractPageAction"/>
        <title><c:out value="${actionBean.dispatch.lastPageInstance.page.description}"/></title>
    </head>
    <body class="yui-skin-sam">
    <div id="doc3" class="yui-t2">
        <div id="hd">
            <jsp:include page="header.jsp"/>
        </div>
        <div id="bd">
            <div id="yui-main">
                <div id="content" class="yui-b">
                    <stripes:layout-component name="pageContent" />
                </div>
            </div>
            <div id="sidebar" class="yui-b">
                <jsp:include page="navigation.jsp"/>
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