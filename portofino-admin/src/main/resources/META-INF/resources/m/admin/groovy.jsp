<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><stripes:layout-render name="/m/admin/admin-theme/admin-page.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.admin.groovy.GroovyAdminAction"/>
    <stripes:layout-component name="pageTitle">
        <fmt:message key="groovy"/>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <stripes:form beanclass="com.manydesigns.portofino.actions.admin.groovy.GroovyAdminAction"
                      method="post">
            <p>
                <fmt:message key="layouts.admin.groovy.text"/>
            </p>
            <div class="form-group">
                <portofino:buttons list="groovy" />
            </div>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>