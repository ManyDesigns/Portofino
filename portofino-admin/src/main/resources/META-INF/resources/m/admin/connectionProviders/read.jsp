<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.admin.database.ConnectionProvidersAction"/>
<stripes:layout-render name="/m/admin/admin-theme/admin-page.jsp">
    <stripes:layout-component name="pageTitle">
        Connection provider: <c:out value="${actionBean.databaseName}"/>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <stripes:form beanclass="com.manydesigns.portofino.actions.admin.database.ConnectionProvidersAction"
                      method="post" class="form-horizontal">
            <mde:write name="actionBean" property="form"/>
            <c:if test="${not empty actionBean.connectionProvider.database.schemas}">
                <div class="horizontalSeparator"></div>
                <h4><fmt:message key="schemas"/></h4>
                <c:forEach var="schema" items="${actionBean.connectionProvider.database.schemas}"
                           varStatus="status">
                    <c:out value="${schema.schemaName}" /><c:if test="${!status.last}">, </c:if>
                </c:forEach>
            </c:if>
            <c:if test="${actionBean.detectedValuesForm != null}">
                <div class="horizontalSeparator"></div>
                <h4><fmt:message key="detected.values"/></h4>
                <div id="detectedValuesForm">
                    <mde:write name="actionBean" property="detectedValuesForm"/>
                </div>
            </c:if>
            <stripes:hidden name="databaseName" value="${actionBean.databaseName}"/>
            <div class="form-group">
                <portofino:buttons list="connectionProviders-read" />
            </div>
            <script type="text/javascript">
                $(function() {
                    $("button[name=delete]").click(function() {
                        return confirm ('<fmt:message key="are.you.sure" />');
                    });
                });
            </script>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>