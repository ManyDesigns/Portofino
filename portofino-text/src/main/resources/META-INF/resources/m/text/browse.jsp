<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %><%--
--%><!doctype html>
<html lang="en">
<jsp:useBean id="actionBean" scope="request"
             type="com.manydesigns.portofino.pageactions.text.TextAction"/>
<fmt:message key="layouts.text.browse.browse_server" var="pageTitle"/>
<jsp:include page="/theme/head.jsp">
    <jsp:param name="pageTitle" value="${pageTitle}" />
</jsp:include>
<body>
    <div class="container">
    <% if(request.getParameter("images-only") == null) { %>
        <form action="${pageContext.request.contextPath}${actionBean.context.actualServletPath}">
            <input type="hidden" name="returnUrl"
                   value='<%= request.getContextPath() + actionBean.getContext().getActualServletPath() + "?" + request.getQueryString() %>' />
            <input type="hidden" name="CKEditorFuncNum" value='${actionBean.CKEditorFuncNum}' />
            <fmt:message key="layouts.text.browse.pages"/>:
            <button class="btn" type="submit" name="browsePages" role="button" aria-disabled="false">
                <fmt:message key="layouts.text.browse"/></button>
        </form>
    <% } %>
    <c:if test="${not empty actionBean.textConfiguration.attachments}">
        <fmt:message key="commons.attachments"/>:
        <ul>
            <c:forEach var="attachment" items="${actionBean.textConfiguration.attachments}">
                <li><a href="#"
                        onclick="window.opener.CKEDITOR.tools.callFunction(
                        <c:out value='${actionBean.CKEditorFuncNum}'/>,
                        '<c:out value="${pageContext.request.contextPath}${actionBean.context.actualServletPath}?viewAttachment=&id=${attachment.id}"/>'
                    ); window.close();">
                    <c:out value="${attachment.filename}"/>
                </a></li>
            </c:forEach>
        </ul>
    </c:if>
    <c:if test="${empty actionBean.textConfiguration.attachments}">
        <fmt:message key="layouts.text.browse.there_are_no_attachments"/>
        <a href="#" onclick="window.close()"><fmt:message key="layouts.text.browse.close_window"/></a>
    </c:if>
    </div>
</body>
</html>
