<%
    // Avoid caching of dynamic pages
    response.setHeader("Pragma", "no-cache");
    response.addHeader("Cache-Control", "must-revalidate");
    response.addHeader("Cache-Control", "no-cache");
    response.addHeader("Cache-Control", "no-store");
    response.setDateHeader("Expires", 0);
%><%@ page contentType="text/html;charset=ISO-8859-1" language="java" pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %><%--
--%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en">
<jsp:useBean id="actionBean" scope="request"
             type="com.manydesigns.portofino.actions.TextAction"/>
<head>
    <title><fmt:message key="layouts.text.browse.browse_server"/></title>
    <jsp:include page="/skins/${skin}/head.jsp"/>
</head>
<body>
<form action="${actionBean.dispatch.absoluteOriginalPath}">
    Choose a page:
    <button onclick="window.opener.CKEDITOR.tools.callFunction(
                        <c:out value='${actionBean.CKEditorFuncNum}'/>,
                        $('#iframe').attr('contentWindow').location.href
                    ); window.close();">
        <span class="ui-button-text">Choose</span>
    </button>
    <input type="hidden" name="cancelReturnUrl" value="${actionBean.cancelReturnUrl}"></input>
    <button name="cancel">
        <span class="ui-button-text">Cancel</span>
    </button>
</form>
<iframe src="${actionBean.dispatch.absoluteOriginalPath}" id="iframe" style="width: 100%; height: 600px; border: 1px dashed black;"></iframe>
<form action="${actionBean.dispatch.absoluteOriginalPath}">
    Choose a page:
    <button onclick="window.opener.CKEDITOR.tools.callFunction(
                        <c:out value='${actionBean.CKEditorFuncNum}'/>,
                        $('#iframe').attr('contentWindow').location.href
                    ); window.close();">
        <span class="ui-button-text">Choose</span>
    </button>
    <input type="hidden" name="cancelReturnUrl" value="${actionBean.cancelReturnUrl}"></input>
    <button name="cancel">
        <span class="ui-button-text">Cancel</span>
    </button>
</form>
</body>
</html>
