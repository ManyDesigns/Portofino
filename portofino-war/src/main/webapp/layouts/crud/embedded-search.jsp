<%@ page contentType="text/html;charset=ISO-8859-1" language="java" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<%@taglib prefix="mde" uri="/manydesigns-elements"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<stripes:layout-render name="/skins/${skin}/portlet.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.CrudAction"/>
    <stripes:layout-component name="portletTitle">
        <c:out value="${actionBean.crud.searchTitle}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletHeaderButtons">
        <button name="configure" class="wrench"><fmt:message key="commons.configure"/></button>
    </stripes:layout-component>
    <stripes:layout-component name="portletHeaderButtons">
        <button name="configure" class="wrench"><fmt:message key="commons.configure"/></button>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <div class="embedded-content">
            <div class="search_results">
                <%@include file="datatable.jsp"%>
                <stripes:link href="${actionBean.dispatch.originalPath}">&gt;&gt; <fmt:message key="commons.advanced_search"/></stripes:link>
            </div>
        </div>
    </stripes:layout-component>
</stripes:layout-render>