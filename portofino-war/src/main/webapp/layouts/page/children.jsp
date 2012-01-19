<%@ page import="com.manydesigns.portofino.logic.SecurityLogic" %>
<%@ page import="com.manydesigns.portofino.model.pages.AccessLevel" %>
<%@ page import="com.manydesigns.portofino.model.pages.Page" %>
<%@ page import="com.manydesigns.portofino.model.pages.Permissions" %>
<%@ page import="com.manydesigns.portofino.system.model.users.Group" %>
<%@ page import="com.manydesigns.portofino.system.model.users.annotations.SupportsPermissions" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.List" %>
<%@ page import="com.manydesigns.portofino.dispatcher.PageInstance" %>
<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino" %>
<stripes:layout-render name="/skins/${skin}/modal-page.jsp" formActionUrl="/actions/admin/page">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.admin.page.PageAdminAction"/>
    <stripes:layout-component name="contentHeader">
        <portofino:buttons list="page-children-edit" cssClass="contentButton" />
        <div class="breadcrumbs">
            <div class="inner">
                <mde:write name="breadcrumbs"/>
            </div>
        </div>
    </stripes:layout-component>
    <stripes:layout-component name="portletTitle">
        Page children for: <c:out value="${actionBean.page.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <input type="hidden" name="originalPath" value="${actionBean.dispatch.originalPath}" />
        <div>
            <mde:write name="actionBean" property="childPagesForm" />
        </div>
        <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletFooter"/>
    <stripes:layout-component name="contentFooter">
        <portofino:buttons list="page-children-edit" cssClass="contentButton" />
    </stripes:layout-component>
</stripes:layout-render>