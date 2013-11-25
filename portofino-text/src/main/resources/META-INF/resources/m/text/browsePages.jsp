<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %><%--
--%><!doctype html>
<html lang="<%= request.getLocale() %>" style="height: 100%;">
<jsp:useBean id="actionBean" scope="request"
             type="com.manydesigns.portofino.pageactions.text.TextAction"/>
<fmt:message key="browse.server" var="pageTitle"/>
<jsp:include page="/theme/head.jsp">
    <jsp:param name="pageTitle" value="${pageTitle}" />
</jsp:include>
<body style="margin: 0 10px 0 10px; height: 100%; padding-top: 0;">
<form action="${pageContext.request.contextPath}${actionBean.context.actionPath}" method="post" style="padding-top: 10px;">
    Choose a page:
    <button onclick="window.opener.CKEDITOR.tools.callFunction(
                        <c:out value='${actionBean.CKEditorFuncNum}'/>,
                        $('#iframe').prop('contentWindow').location.href
                    ); window.close();"
            class="btn">
        Choose
    </button>
    <input type="hidden" name="returnUrl" value="${actionBean.returnUrl}" />
    <button name="cancel" type="submit" class="btn">
        Cancel
    </button>
</form>
<iframe src="${pageContext.request.contextPath}${actionBean.context.actionPath}" id="iframe"
        style="width: 100%; height: 85%; border: 1px dashed black; margin: 10px 0 10px 0;">
</iframe>
<form action="${pageContext.request.contextPath}${actionBean.context.actionPath}" method="post">
    Choose a page:
    <button onclick="window.opener.CKEDITOR.tools.callFunction(
                        <c:out value='${actionBean.CKEditorFuncNum}'/>,
                        $('#iframe').prop('contentWindow').location.href
                    ); window.close();"
            class="btn">
        Choose
    </button>
    <input type="hidden" name="returnUrl" value="${actionBean.returnUrl}" />
    <button name="cancel" type="submit" class="btn">
        Cancel
    </button>
</form>
</body>
</html>
