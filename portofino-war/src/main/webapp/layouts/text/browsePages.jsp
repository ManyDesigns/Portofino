<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %><%--
--%><!doctype html>
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" style="height: 100%;">
<jsp:useBean id="actionBean" scope="request"
             type="com.manydesigns.portofino.pageactions.text.TextAction"/>
<head>
    <title><fmt:message key="layouts.text.browse.browse_server"/></title>
    <jsp:include page="/skins/${skin}/head.jsp" />
</head>
<body style="margin: 0 10px 0 10px; height: 100%;">
<form action="${actionBean.dispatch.absoluteOriginalPath}" method="post" style="padding-top: 10px;">
    Choose a page:
    <button onclick="window.opener.CKEDITOR.tools.callFunction(
                        <c:out value='${actionBean.CKEditorFuncNum}'/>,
                        $('#iframe').prop('contentWindow').location.href
                    ); window.close();"
            class="contentButton">
        <span class="ui-button-text">Choose</span>
    </button>
    <input type="hidden" name="cancelReturnUrl" value="${actionBean.cancelReturnUrl}" />
    <button name="cancel" type="submit" class="contentButton">
        <span class="ui-button-text">Cancel</span>
    </button>
</form>
<iframe src="${actionBean.dispatch.absoluteOriginalPath}" id="iframe"
        style="width: 100%; height: 85%; border: 1px dashed black; margin: 10px 0 10px 0;">
</iframe>
<form action="${actionBean.dispatch.absoluteOriginalPath}" method="post">
    Choose a page:
    <button onclick="window.opener.CKEDITOR.tools.callFunction(
                        <c:out value='${actionBean.CKEditorFuncNum}'/>,
                        $('#iframe').prop('contentWindow').location.href
                    ); window.close();"
            class="contentButton">
        <span class="ui-button-text">Choose</span>
    </button>
    <input type="hidden" name="cancelReturnUrl" value="${actionBean.cancelReturnUrl}" />
    <button name="cancel" type="submit" class="contentButton">
        <span class="ui-button-text">Cancel</span>
    </button>
</form>
</body>
</html>
