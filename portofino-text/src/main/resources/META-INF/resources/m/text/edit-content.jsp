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
/><stripes:layout-render name="/theme/templates/${actionBean.pageInstance.layout.template}/modal.jsp">
    <stripes:layout-component name="pageTitle">
        <fmt:message key="configure.page._">
            <fmt:param value="<%= StringEscapeUtils.escapeHtml(actionBean.getPage().getTitle()) %>" />
        </fmt:message>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <script type="text/javascript" src="<stripes:url value="/theme/ckeditor/ckeditor.js"/>"></script>
        <script type="text/javascript" src="<stripes:url value="/theme/ckeditor/adapters/jquery.js"/>"></script>
        <script type="text/javascript">
            $(function() {
                portofino.setupRichTextEditors({
                    filebrowserBrowseUrl : '<c:out value="${pageContext.request.contextPath}${actionBean.context.actionPath}"/>?browse=',
                    filebrowserImageBrowseUrl : '<c:out value="${pageContext.request.contextPath}${actionBean.context.actionPath}"/>?browse=&images-only=',
                    filebrowserUploadUrl : '<c:out value="${pageContext.request.contextPath}${actionBean.context.actionPath}"/>?uploadAttachmentFromCKEditor='
                });
            });
        </script>
        <stripes:form action="${actionBean.context.actionPath}" method="post" enctype="multipart/form-data">
            <!-- Content editor -->
            <fieldset style="margin-bottom: 2em;">
                <legend><fmt:message key="edit.content"/></legend>
                <input type="text" name="title" value="<c:out value="${actionBean.title}"/>"
                       class="input-block-level"/>
                <stripes:textarea class="mde-form-rich-text" name="content" value="${actionBean.content}"/>
                <input type="hidden" name="returnUrl" value="<c:out value="${actionBean.returnUrl}"/>"/>
            </fieldset>
            <div class="form-actions">
                <portofino:buttons list="edit-content" />
            </div>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>
