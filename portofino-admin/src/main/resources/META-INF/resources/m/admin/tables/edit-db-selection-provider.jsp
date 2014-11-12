<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<stripes:layout-render name="/m/admin/admin-theme/admin-page.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.admin.database.TablesAction"/>
    <stripes:layout-component name="pageTitle">
        <c:if test="${empty actionBean.selectionProviderName}">
            <fmt:message key="layouts.admin.tables.addSelectionProvider.title">
                <fmt:param value="${actionBean.table.qualifiedName}" />
            </fmt:message>
        </c:if>
        <c:if test="${not empty actionBean.selectionProviderName}">
            <fmt:message key="layouts.admin.tables.editSelectionProvider.title">
                <fmt:param value="${actionBean.selectionProviderName}" />
            </fmt:message>
        </c:if>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <script type="text/javascript">
            $(function() {
                $("button[name=removeSelectionProvider]").click(
                        function() {
                            return confirm('<fmt:message key="are.you.sure" />');
                        });
            });
        </script>
        <stripes:form action="${actionBean.actionPath}"
                      method="post" class="form-horizontal">
            <mde:write name="actionBean" property="dbSelectionProviderForm" />
            <mde:write name="actionBean" property="tableForm" />
            <input name="selectionProviderName" type="hidden" value="${actionBean.selectionProviderName}" />
            <input name="selectedTabId" type="hidden" value="tab-fk-sp" />
            <div class="form-group">
                <div class="col-md-offset-2 col-md-10">
                    <portofino:buttons list="table-selection-provider" />
                </div>
            </div>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>