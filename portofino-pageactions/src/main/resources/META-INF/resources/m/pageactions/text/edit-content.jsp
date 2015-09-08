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
/><stripes:layout-render name="/theme/templates/${actionBean.pageTemplate}/modal.jsp">
    <stripes:layout-component name="pageTitle">
        <fmt:message key="configure.page._">
            <fmt:param value="<%= StringEscapeUtils.escapeHtml(actionBean.getPage().getTitle()) %>" />
        </fmt:message>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <script type="text/javascript" src="<stripes:url value="/webjars/ckeditor/4.5.3/standard/ckeditor.js"/>"></script>
        <script type="text/javascript" src="<stripes:url value="/webjars/ckeditor/4.5.3/standard/adapters/jquery.js"/>"></script>
        <script type="text/javascript">
            $(function() {
                portofino.setupRichTextEditors({
                    filebrowserBrowseUrl : '<c:out value="${pageContext.request.contextPath}${actionBean.context.actionPath}"/>?browse=',
                    filebrowserImageBrowseUrl : '<c:out value="${pageContext.request.contextPath}${actionBean.context.actionPath}"/>?browse=&images-only=',
                    filebrowserUploadUrl : '<c:out value="${pageContext.request.contextPath}${actionBean.context.actionPath}"/>?uploadAttachmentFromCKEditor='
                });
            });
        </script>
        <stripes:form action="${actionBean.context.actionPath}" method="post" enctype="multipart/form-data" class="form-horizontal">
            <!-- Content editor -->
            <fieldset class="mde-columns-1" style="margin-bottom: 2em;">
                <legend><fmt:message key="edit.header"/></legend>


                <div class="row">
                    <div class="col-md-12 mde-colspan-1">
                        <div class="form-group readwrite required">
                            <label  class="control-label"><fmt:message key="title"/></label>
                            <div class="input-container fill-row">
                                <input type="text" name="title" value="<c:out value="${actionBean.title}"/>" class="form-control"/>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-12 mde-colspan-1">
                        <div class="form-group readwrite required">
                            <label  class="control-label"><fmt:message key="description"/></label>
                            <div class="input-container fill-row">
                                <input type="text" name="title" value="<c:out value="${actionBean.description}"/>" class="form-control"/>
                            </div>
                        </div>
                    </div>
                </div>

                 <legend><fmt:message key="edit.content"/></legend>

                <div class="row">
                    <div class="col-md-12 mde-colspan-1" >
                        <stripes:textarea class="mde-form-rich-text" name="content" value="${actionBean.content}"/>
                        <input type="hidden" name="returnUrl" value="<c:out value="${actionBean.returnUrl}"/>"/>
                    </div>
                </div>
            </fieldset>

            <div class="form-group">
                <div class="col-sm-12">
                    <portofino:buttons list="edit-content" />
                </div>
            </div>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>
