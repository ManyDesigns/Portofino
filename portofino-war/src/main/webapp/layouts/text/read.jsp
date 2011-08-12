<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><stripes:layout-render name="/skins/${skin}/portlet.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.TextAction"/>
    <stripes:layout-component name="portletTitle">
        <c:out value="${actionBean.textPage.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletHeaderButtons">
        <button name="configure" class="wrench">Configure</button>
        <button name="manageAttachments" class="link">Manage attachments</button>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <c:out value="${actionBean.content}" escapeXml="false"/>
        <c:if test="${not empty actionBean.blobs}">
            <div class="horizontalSeparator"></div>
            Attachments:
            <div class="attachmentBox">
                <c:forEach var="blob" items="${actionBean.blobs}">
                    <div class="attachment <c:out value="mime-${fn:replace(blob.contentType,'/','-')}"/>">
                        <div class="attachmentName"><c:out value="${blob.filename}"/></div>
                        <c:out value="${mde:bytesToHumanString(blob.size)}"/>
                        <a href="<c:out value="${actionBean.dispatch.absoluteOriginalPath}?viewAttachment=&code=${blob.code}"/>">view</a>
                        <a href="<c:out value="${actionBean.dispatch.absoluteOriginalPath}?downloadAttachment=&code=${blob.code}"/>">download</a>
                    </div>
                </c:forEach>
                <div style="clear:both"></div>
            </div>
        </c:if>
    </stripes:layout-component>
    <stripes:layout-component name="portletFooter">
    </stripes:layout-component>
</stripes:layout-render>