<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><stripes:layout-render name="/m/admin/admin-theme/admin-page.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.admin.database.ReloadModelAction"/>
    <stripes:layout-component name="pageTitle">
        <fmt:message key="layouts.admin.reload-model.title"/>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <stripes:form beanclass="com.manydesigns.portofino.actions.admin.database.ReloadModelAction"
                      method="post">
            <p>
                <fmt:message key="layouts.admin.reload-model.text"/>
            </p>
            <div class="form-group">
                <portofino:buttons list="reload-model" />
                <portofino:buttons list="reload-model-bar" />
            </div>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>