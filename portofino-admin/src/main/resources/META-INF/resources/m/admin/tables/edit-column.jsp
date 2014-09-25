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
        <fmt:message key="edit.column._">
            <fmt:param value="${actionBean.column.qualifiedName}" />
        </fmt:message>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <stripes:form action="${actionBean.actionPath}"
                      method="post" class="form-horizontal">
            <mde:write name="actionBean" property="columnForm" />
            <mde:write name="actionBean" property="tableForm" />
            <div class="form-group">
                <div class="col-md-offset-2 col-md-10">
                    <portofino:buttons list="column-edit" />
                </div>
            </div>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>