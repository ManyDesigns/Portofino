<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><stripes:layout-render name="/theme/templates/${actionBean.pageTemplate}/normal.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.text.TextAction"/>
    <stripes:layout-component name="pageTitle">
        <c:out value="${actionBean.pageInstance.page.title}"/>
        <c:if test="${not empty actionBean.pageInstance.page.description}">
            (<c:out value="${actionBean.pageInstance.page.description}"/>)
        </c:if>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <c:out value="${actionBean.content}" escapeXml="false"/>
        <c:if test="${not empty actionBean.downloadableAttachments}">
            <div class="horizontalSeparator"></div>
            <fmt:message key="attachments"/>:
            <div class="attachmentBox">
                <c:forEach var="attachment" items="${actionBean.downloadableAttachments}">
                    <div class="attachment">
                        <div class="attachmentName">
                            <em class="glyphicon glyphicon-file"></em>
                            <c:out value="${attachment.filename}"/>
                        </div>
                        <c:out value="${mde:bytesToHumanString(attachment.size)}"/>
                        <a href="<c:out value="${pageContext.request.contextPath}${actionBean.context.actionPath}?viewAttachment=&id=${attachment.id}"/>">view</a>
                        <a href="<c:out value="${pageContext.request.contextPath}${actionBean.context.actionPath}?downloadAttachment=&id=${attachment.id}"/>">download</a>
                    </div>
                </c:forEach>
                <div style="clear:both"></div>
            </div>
        </c:if>
    </stripes:layout-component>
</stripes:layout-render>