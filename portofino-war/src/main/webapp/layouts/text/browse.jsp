<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %><%--
--%><!doctype html>
<html xmlns="http://www.w3.org/1999/xhtml" lang="en">
<jsp:useBean id="actionBean" scope="request"
             type="com.manydesigns.portofino.pageactions.text.TextAction"/>
<head>
    <jsp:include page="/skins/${skin}/head.jsp" />
    <title><fmt:message key="layouts.text.browse.browse_server"/></title>
</head>
<body style="text-align: left;">
<% if(request.getParameter("images-only") == null) { %>
    <form action="${actionBean.dispatch.absoluteOriginalPath}">
        <input type="hidden" name="cancelReturnUrl"
               value='<%= actionBean.getDispatch().getAbsoluteOriginalPath() + "?" + request.getQueryString() %>' />
        <input type="hidden" name="CKEditorFuncNum" value='${actionBean.CKEditorFuncNum}' />
        <fmt:message key="layouts.text.browse.pages"/>:
        <button class="ui-button ui-widget ui-state-default ui-corner-all contentButton ui-button-text-only"
                type="submit" name="browsePages" role="button" aria-disabled="false">
            <span class="ui-button-text"><fmt:message key="layouts.text.browse"/></span></button>
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
