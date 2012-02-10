<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<stripes:layout-render name="/skins/${skin}/portlet.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.text.TextAction"/>
    <stripes:layout-component name="portletTitle">
        <c:out value="${actionBean.pageInstance.page.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <c:out value="${actionBean.content}" escapeXml="false"/>
        <c:if test="${not empty actionBean.downloadableAttachments}">
            <div class="horizontalSeparator"></div>
            <fmt:message key="commons.attachments"/>:
            <div class="attachmentBox">
                <c:forEach var="attachment" items="${actionBean.downloadableAttachments}">
                    <div class="attachment <c:out value="mime-${fn:replace(attachment.contentType,'/','-')}"/>">
                        <div class="attachmentName"><c:out value="${attachment.filename}"/></div>
                        <c:out value="${mde:bytesToHumanString(attachment.size)}"/>
                        <a href="<c:out value="${actionBean.dispatch.absoluteOriginalPath}?viewAttachment=&id=${attachment.id}"/>">view</a>
                        <a href="<c:out value="${actionBean.dispatch.absoluteOriginalPath}?downloadAttachment=&id=${attachment.id}"/>">download</a>
                    </div>
                </c:forEach>
                <div style="clear:both"></div>
            </div>
        </c:if>
    </stripes:layout-component>
    <stripes:layout-component name="portletFooter">
    </stripes:layout-component>
</stripes:layout-render>