<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><stripes:layout-definition><%--
--%><div class="modal" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" aria-hidden="true">&times;</button>
                <h4 class="modal-title">
                    <stripes:layout-component name="pageTitle">
                        <c:out value="${actionBean.pageInstance.description}"/>
                    </stripes:layout-component></h4>
            </div>
            <div class="modal-body">
                <stripes:layout-component name="pageBody" />
            </div>
            <div class="modal-footer">
                <stripes:layout-component name="pageFooter" />
            </div>
        </div>
    </div>
</div>
</stripes:layout-definition>