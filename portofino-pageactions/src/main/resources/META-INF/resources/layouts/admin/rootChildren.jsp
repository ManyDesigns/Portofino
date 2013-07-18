<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<stripes:layout-render name="/portofino-base/admin-theme/admin-page.jsp">
    <jsp:useBean id="actionBean" scope="request"
                 type="com.manydesigns.portofino.actions.admin.page.RootConfigurationAction"/>
    <stripes:layout-component name="customScripts">
        <link rel="stylesheet" type="text/css"
              href="<stripes:url value="/jquery-ui/css/no-theme/jquery-ui-1.10.3.custom.min.css"/>"/>
        <script type="text/javascript"
                src="<stripes:url value="/jquery-ui/js/jquery-ui-1.10.3.custom.min.js"/>"></script>
    </stripes:layout-component>
    <stripes:layout-component name="pageTitle">
        <fmt:message key="layouts.admin.rootChildren" />
    </stripes:layout-component>
    <stripes:layout-component name="portletTitle">
        <fmt:message key="layouts.admin.rootChildren" />
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <stripes:form beanclass="com.manydesigns.portofino.actions.admin.page.RootChildrenAction"
                      method="post" enctype="multipart/form-data">
            <%@include file="/layouts/page/children-tables.jsp"%>
            <div class="form-actions">
                <portofino:buttons list="root-children" />
            </div>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>