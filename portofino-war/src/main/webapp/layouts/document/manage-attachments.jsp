<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><stripes:layout-render name="/skins/${skin}/modal-page.jsp">
    <stripes:layout-component name="customScripts">
        <script type="text/javascript" src="<stripes:url value="/ckeditor/ckeditor.js"/>"></script>
        <script type="text/javascript" src="<stripes:url value="/ckeditor/adapters/jquery.js"/>"></script>
    </stripes:layout-component>
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.DocumentAction"/>
    <stripes:layout-component name="contentHeader">
        <stripes:submit name="updateAttachments" value="Save" class="contentButton"/>
        <stripes:submit name="cancel" value="Cancel" class="contentButton"/>
        <div class="breadcrumbs">
            <div class="inner">
                <mde:write name="breadcrumbs"/>
            </div>
        </div>
    </stripes:layout-component>
    <stripes:layout-component name="portletTitle">
        Manage attachments for page <c:out value="${actionBean.documentNode.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <c:if test="${not empty actionBean.blobs}">
            Attachments:
            <ul>
                <c:forEach var="blob" items="${actionBean.blobs}">
                    <li><c:out value="${blob.filename}"/></li>
                </c:forEach>
            </ul>
        </c:if><c:if test="${empty actionBean.blobs}">
            There are no attachments.
        </c:if>
        Upload a new file: <stripes:file name="upload"/>
    </stripes:layout-component>
    <stripes:layout-component name="contentFooter">
        <stripes:submit name="updateAttachments" value="Save" class="contentButton"/>
        <stripes:submit name="cancel" value="Cancel" class="contentButton"/>
    </stripes:layout-component>
</stripes:layout-render>