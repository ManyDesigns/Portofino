<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.crud.AbstractCrudAction"
/><stripes:layout-render name="/theme/templates/${actionBean.pageTemplate}/normal.jsp">
    <stripes:layout-component name="pageTitle">
        <c:out value="${actionBean.searchTitle}"/>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <stripes:form
                action="${actionBean.context.actionPath}" method="post"
                id="${(not empty actionBean.crudConfiguration.name) ? actionBean.crudConfiguration.name : null}"
                class="form-inline crud-search-form dont-prompt-on-page-abandon search">
            <%-- Hidden submit so that ENTER on a form executes the default action --%>
            <div class="hidden-submit"><portofino:buttons list="crud-search-form-default-button" /></div>
            <input type="hidden" name="returnUrl" value="<c:out value="${actionBean.returnUrl}"/>"/>
            <c:if test="${not empty actionBean.searchForm}">
                <c:if test="${actionBean.searchVisible}">
                    <a href="${pageContext.request.contextPath}${actionBean.context.actionPath}" class="search_form_toggle_link" data-search-visible="true">
                        <span><div style="display: inline;" class="glyphicon glyphicon-menu-up" aria-hidden="true"></div> <fmt:message key="hide.search" /></span>
                        <span style="display: none;"><div style="display: inline;" class="glyphicon glyphicon-menu-down" aria-hidden="true"></div> <fmt:message key="show.search" /></span>
                    </a>
                </c:if>
                <c:if test="${!actionBean.searchVisible}">
                    <a href="${pageContext.request.contextPath}${actionBean.context.actionPath}?search=" class="search_form_toggle_link" data-search-visible="false">
                        <span style="display: none;"><div style="display: inline;" class="glyphicon glyphicon-menu-up" aria-hidden="true"></div> <fmt:message key="hide.search" /></span>
                        <span><div style="display: inline;" class="glyphicon glyphicon-menu-down" aria-hidden="true"></div> <fmt:message key="show.search" /></span>
                    </a>
                </c:if>
                <div class="well search_form" <c:if test="${!actionBean.searchVisible}">style="display: none;"</c:if>>
                    <mde:write name="actionBean" property="searchForm"/>
                    <div class="searchFormButtons">
                        <portofino:buttons list="crud-search-form" />
                    </div>
                </div>
            </c:if>
            <input type="hidden" name="searchString" value="<c:out value="${actionBean.searchString}"/>"/>
            <div class="portofino-datatable"></div>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>