<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><stripes:layout-render name="/skins/${skin}/${actionBean.pageTemplate}/modal.jsp">
    <stripes:layout-component name="customScripts">
        <script type="text/javascript" src="<stripes:url value="/ckeditor/ckeditor.js"/>"></script>
        <script type="text/javascript" src="<stripes:url value="/ckeditor/adapters/jquery.js"/>"></script>
    </stripes:layout-component>
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.text.TextAction"/>
    <stripes:layout-component name="contentHeader">
        <portofino:buttons list="edit-content" cssClass="contentButton" />
        <jsp:include page="/skins/${skin}/breadcrumbs.jsp" />
    </stripes:layout-component>
    <stripes:layout-component name="portletTitle">
        <fmt:message key="layouts.text.configure.content"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <stripes:textarea class="mde-form-rich-text" name="content" value="${actionBean.content}"/>
        <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletFooter">
        <script type="text/javascript">
            var windowWidth = 640, windowHeight = 480;
            if (window.innerWidth && window.innerHeight) {
                windowWidth = window.innerWidth;
                windowHeight = window.innerHeight;
            } else if (document.compatMode=='CSS1Compat' &&
                document.documentElement &&
                document.documentElement.offsetWidth ) {
                windowWidth = document.documentElement.offsetWidth;
                windowHeight = document.documentElement.offsetHeight;
            } else if (document.body && document.body.offsetWidth) {
                windowWidth = document.body.offsetWidth;
                windowHeight = document.body.offsetHeight;
            }

            $('textarea.mde-form-rich-text').data('mdeRichTextConfig', {
                toolbar: 'Full',
                toolbarCanCollapse: false,
                filebrowserWindowWidth : windowWidth,
                filebrowserWindowHeight : windowHeight,
                filebrowserBrowseUrl : '<c:out value="${actionBean.dispatch.absoluteOriginalPath}"/>?browse=',
                filebrowserImageBrowseUrl : '<c:out value="${actionBean.dispatch.absoluteOriginalPath}"/>?browse=&images-only=',
                filebrowserUploadUrl : '<c:out value="${actionBean.dispatch.absoluteOriginalPath}"/>?uploadAttachmentFromCKEditor='
            });
        </script>
    </stripes:layout-component>
    <stripes:layout-component name="contentFooter">
        <portofino:buttons list="edit-content" cssClass="contentButton" />
    </stripes:layout-component>
</stripes:layout-render>