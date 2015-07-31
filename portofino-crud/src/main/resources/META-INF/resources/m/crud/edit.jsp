<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.crud.AbstractCrudAction"
/><stripes:layout-render name="/theme/templates/${actionBean.pageTemplate}/modal.jsp">
    <stripes:layout-component name="pageTitle">
        <c:out value="${actionBean.editTitle}"/>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <c:if test="${actionBean.formWithRichTextFields}">
            <script type="text/javascript" src="<stripes:url value="/theme/ckeditor/ckeditor.js"/>"></script>
            <script type="text/javascript" src="<stripes:url value="/theme/ckeditor/adapters/jquery.js"/>"></script>
            <script type="text/javascript">
                $(function() {
                    portofino.setupRichTextEditors({
                        toolbarCanCollapse: true,
                        height: null
                    });
                });
            </script>
        </c:if>
        <c:if test="${actionBean.requiredFieldsPresent}">
            <p><fmt:message key="fields.marked.with.a.star.are.required"/>.</p>
        </c:if>
        <stripes:form action="${actionBean.context.actionPath}" method="post"
                      id="${(not empty actionBean.crudConfiguration.name) ? actionBean.crudConfiguration.name : null}"
                      enctype="multipart/form-data" class="form-horizontal edit">
            <mde:write name="actionBean" property="form"/>
            <input type="hidden" name="searchString" value="<c:out value="${actionBean.searchString}"/>"/>
            <input type="hidden" name="returnUrl" value="<c:out value="${actionBean.returnUrl}"/>"/>
            <div class="form-group">
                <div class="col-md-offset-2 col-md-10">
                    <portofino:buttons list="crud-edit" />
                </div>
            </div>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>