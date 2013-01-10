<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.text.TextAction"
/><stripes:layout-render name="/skins/${skin}${actionBean.pageTemplate}/modal.jsp">
    <stripes:layout-component name="customScripts">
        <script type="text/javascript" src="<stripes:url value="/ckeditor/ckeditor.js"/>"></script>
        <script type="text/javascript" src="<stripes:url value="/ckeditor/adapters/jquery.js"/>"></script>
        <script type="text/javascript">
            $(function() {
                portofino.setupRichTextEditors({
                    filebrowserBrowseUrl : '<c:out value="${actionBean.dispatch.absoluteOriginalPath}"/>?browse=',
                    filebrowserImageBrowseUrl : '<c:out value="${actionBean.dispatch.absoluteOriginalPath}"/>?browse=&images-only=',
                    filebrowserUploadUrl : '<c:out value="${actionBean.dispatch.absoluteOriginalPath}"/>?uploadAttachmentFromCKEditor='
                });
            });
        </script>
    </stripes:layout-component>
    <stripes:layout-component name="contentHeader">
        <portofino:buttons list="edit-content" cssClass="contentButton" />
        <jsp:include page="/skins/${skin}/breadcrumbs.jsp" />
    </stripes:layout-component>
    <stripes:layout-component name="portletTitle">
        <input type="text" name="title" value="<c:out value="${actionBean.title}"/>"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <!-- Content editor -->
        <fieldset class="mde-form-fieldset">
            <legend><fmt:message key="layouts.text.configure.content"/></legend>
            <stripes:textarea class="mde-form-rich-text" name="content" value="${actionBean.content}"/>
            <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
        </fieldset>
    </stripes:layout-component>
    <stripes:layout-component name="portletFooter">
    </stripes:layout-component>
    <stripes:layout-component name="contentFooter">
        <portofino:buttons list="edit-content" cssClass="contentButton" />
    </stripes:layout-component>
</stripes:layout-render>
