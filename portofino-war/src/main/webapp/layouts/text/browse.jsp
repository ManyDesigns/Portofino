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
<%@ taglib prefix="mde" uri="/manydesigns-elements"%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en">
<jsp:useBean id="actionBean" scope="request"
             type="com.manydesigns.portofino.actions.TextAction"/>
<jsp:useBean id="dispatch" scope="request"
             type="com.manydesigns.portofino.dispatcher.Dispatch"/>
<head>
    <title>Browse server</title>
</head>
<body>
<c:if test="${not empty actionBean.blobs}">
    Attachments:
    <ul>
        <c:forEach var="blob" items="${actionBean.blobs}">
            <li><a href="#"
                    onclick="window.opener.CKEDITOR.tools.callFunction(
                    <c:out value='${actionBean.CKEditorFuncNum}'/>,
                    '<c:out value="${actionBean.dispatch.absoluteOriginalPath}?viewAttachment=&code=${blob.code}"/>'
                ); window.close();">
                <c:out value="${blob.filename}"/>
            </a></li>
        </c:forEach>
    </ul>
</c:if><c:if test="${empty actionBean.blobs}">
    There are no attachments. <a href="#" onclick="window.close()">Close window</a>
</c:if>
</body>
</html>
