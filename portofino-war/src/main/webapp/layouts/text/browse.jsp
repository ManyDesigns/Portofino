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
             type="com.manydesigns.portofino.pageactions.text.TextAction"/>
<head>
    <title><fmt:message key="layouts.text.browse.browse_server"/></title>
</head>
<body>
<% if(request.getParameter("images-only") == null) { %>
    <form action="${actionBean.dispatch.absoluteOriginalPath}">
        <input type="hidden" name="cancelReturnUrl"
               value='<%= actionBean.dispatch.getAbsoluteOriginalPath() + "?" + request.getQueryString() %>' />
        <input type="hidden" name="CKEditorFuncNum" value='${actionBean.CKEditorFuncNum}' />
        Pagine: <button name="browsePages" type="submit">Browse</button> (TODO) 
    </form>
<% } %>
<c:if test="${not empty actionBean.textConfiguration.attachments}">
    <fmt:message key="commons.attachments"/>:
    <ul>
        <c:forEach var="attachment" items="${actionBean.textConfiguration.attachments}">
            <li><a href="#"
                    onclick="window.opener.CKEDITOR.tools.callFunction(
                    <c:out value='${actionBean.CKEditorFuncNum}'/>,
                    '<c:out value="${actionBean.dispatch.absoluteOriginalPath}?viewAttachment=&id=${attachment.id}"/>'
                ); window.close();">
                <c:out value="${attachment.filename}"/>
            </a></li>
        </c:forEach>
    </ul>
</c:if><c:if test="${empty actionBean.textConfiguration.attachments}">
    <fmt:message key="layouts.text.browse.there_are_no_attachments"/>
    <a href="#" onclick="window.close()"><fmt:message key="layouts.text.browse.close_window"/></a>
</c:if>
</body>
</html>
