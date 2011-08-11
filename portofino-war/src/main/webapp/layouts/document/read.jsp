<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><stripes:layout-render name="/skins/${skin}/portlet.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.DocumentAction"/>
    <stripes:layout-component name="portletTitle">
        <c:out value="${actionBean.documentNode.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <c:out value="${actionBean.content}" escapeXml="false"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletFooter">
        <c:if test="${not empty actionBean.blobs}">
            <div class="attachments">
                Attachments:
                <c:forEach var="blob" items="${actionBean.blobs}">
                    <a href="<c:out value="${actionBean.dispatch.absoluteOriginalPath}?downloadAttachment=&code=${blob.code}"/>"
                            ><c:out value="${blob.filename}"/></a>
                </c:forEach>
            </div>
        </c:if>
    </stripes:layout-component>
</stripes:layout-render>