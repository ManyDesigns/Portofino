<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.admin.database.ConnectionProvidersAction"/>
<stripes:layout-render name="/m/admin/admin-theme/admin-page.jsp">
    <stripes:layout-component name="pageTitle">
        <fmt:message key="layouts.admin.connectionProviders" />
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <stripes:form beanclass="com.manydesigns.portofino.actions.admin.database.ConnectionProvidersAction"
                      method="post" class="dont-prompt-on-page-abandon">
            <mde:write name="actionBean" property="tableForm"/>
            <h4><fmt:message key="available.database.platforms"/></h4>
            <mde:write name="actionBean" property="databasePlatformsTableForm"/>
            <div class="form-group">
                <portofino:buttons list="connectionProviders-search" />
            </div>
            <script type="text/javascript">
                $(function() {
                    $("button[name=bulkDelete]").click(function() {
                        return confirm ('<fmt:message key="are.you.sure" />');
                    });
                });
            </script>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>