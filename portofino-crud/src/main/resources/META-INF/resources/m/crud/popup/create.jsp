<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.crud.AbstractCrudAction"
/><stripes:layout-render name="/theme/templates/popup/modal.jsp">
    <stripes:layout-component name="pageTitle">
        <c:out value="${actionBean.createTitle}"/>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <mde:sessionMessages />
        <c:if test="${actionBean.formWithRichTextFields}">
            <script type="text/javascript" src="<stripes:url value="/webjars/ckeditor/4.5.3/standard/ckeditor.js"/>"></script>
            <script type="text/javascript" src="<stripes:url value="/webjars/ckeditor/4.5.3/standard/adapters/jquery.js"/>"></script>
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
            <p class="subtitle"><fmt:message key="fields.marked.with.a.star.are.required"/>.</p>
        </c:if>
        <stripes:form action="${actionBean.context.actionPath}" method="post"
                      id="${(not empty actionBean.crudConfiguration.name) ? actionBean.crudConfiguration.name : null}"
                      enctype="multipart/form-data" class="form-horizontal create">
            <mde:write name="actionBean" property="form"/>
            <input type="hidden" name="returnUrl" value="<c:out value="${actionBean.returnUrl}"/>"/>
            <input type="hidden" name="popup" value="true" />
            <input type="hidden" name="popupCloseCallback" value="${actionBean.popupCloseCallback}" />
            <div class="form-group">
                <div class="col-md-offset-2 col-md-10">
                    <portofino:buttons list="crud-create" />
                </div>
            </div>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>