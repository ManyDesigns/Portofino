<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><stripes:layout-render name="/skins/${skin}/modal-page.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.TextAction"/>
    <stripes:layout-component name="contentHeader">
        <stripes:submit name="cancel" value="Ok" class="contentButton"/>
        <div class="breadcrumbs">
            <div class="inner">
                <mde:write name="breadcrumbs"/>
            </div>
        </div>
    </stripes:layout-component>
    <stripes:layout-component name="portletTitle">
        Manage attachments for page <c:out value="${actionBean.textPage.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <c:if test="${not empty actionBean.textPage.attachments}">
            Attachments:
            <p>
                <c:forEach var="attachment" items="${actionBean.textPage.attachments}">
                    <br/>
                    <stripes:checkbox name="selection" value="${attachment.id}"/>
                    <a href="<c:out value="${actionBean.dispatch.absoluteOriginalPath}?downloadAttachment=&id=${attachment.id}"/>"
                            ><c:out value="${attachment.filename}"/></a>
                </c:forEach>
            </p>
            <br/>
            <stripes:submit name="deleteAttachments" value="Delete selected attachments" class="portletButton"/>
        </c:if><c:if test="${empty actionBean.textPage.attachments}">
            There are no attachments.
        </c:if>
        <div class="horizontalSeparator"></div>
        Upload a new file:
        <stripes:file name="upload"/>
        <br/>
        <br/>
        <stripes:submit name="uploadAttachment" value="Upload" class="portletButton"/>
    </stripes:layout-component>
    <stripes:layout-component name="contentFooter">
        <stripes:submit name="cancel" value="Ok" class="contentButton"/>
    </stripes:layout-component>
</stripes:layout-render>