<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ page import="org.apache.commons.lang.StringEscapeUtils"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><jsp:useBean id="actionBean" scope="request"
               type="com.manydesigns.portofino.pageactions.text.TextAction"
/><stripes:layout-render name="/skins/${skin}${actionBean.pageTemplate}/modal.jsp">
    <stripes:layout-component name="customScripts">
        <script type="text/javascript" src="<stripes:url value="/elements/ckeditor/ckeditor.js"/>"></script>
        <script type="text/javascript" src="<stripes:url value="/elements/ckeditor/adapters/jquery.js"/>"></script>
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
    <stripes:layout-component name="portletTitle">
        <fmt:message key="com.manydesigns.portofino.pageactions.configure">
            <fmt:param value="<%= StringEscapeUtils.escapeHtml(actionBean.getPage().getTitle()) %>" />
        </fmt:message>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <stripes:form action="${actionBean.dispatch.originalPath}" method="post" enctype="multipart/form-data">
            <!-- Content editor -->
            <fieldset style="margin-bottom: 2em;">
                <legend><fmt:message key="layouts.text.configure.content"/></legend>
                <input type="text" name="title" value="<c:out value="${actionBean.title}"/>"/>
                <stripes:textarea class="mde-form-rich-text" name="content" value="${actionBean.content}"/>
                <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
            </fieldset>
            <div class="form-actions">
                <portofino:buttons list="edit-content" />
            </div>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>
