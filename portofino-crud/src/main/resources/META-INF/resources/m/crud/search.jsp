<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.crud.AbstractCrudAction"
/><stripes:layout-render name="/theme/templates/${actionBean.pageInstance.layout.template}/normal.jsp">
    <stripes:layout-component name="portletTitle">
        <c:out value="${actionBean.searchTitle}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <stripes:form
                action="${actionBean.context.actualServletPath}" method="post" class="form-inline crud-search-form"
                data-search-visible="${actionBean.searchVisible}">
            <%-- Hidden submit so that ENTER on a form executes the default action --%>
            <div class="hidden-submit"><portofino:buttons list="portlet-default-button" /></div>
            <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
            <c:if test="${not empty actionBean.searchForm}">
                <c:if test="${actionBean.searchVisible}">
                    <a class="search_form_toggle_link" href="${pageContext.request.contextPath}${actionBean.context.actualServletPath}">
                        <fmt:message key="layouts.crud.search.hideSearch" />
                    </a>
                </c:if>
                <c:if test="${!actionBean.searchVisible}">
                    <a class="search_form_toggle_link" href="${pageContext.request.contextPath}${actionBean.context.actualServletPath}?search=">
                        <fmt:message key="layouts.crud.search.showSearch" />
                    </a>
                </c:if>
                <div class="search_form" <c:if test="${!actionBean.searchVisible}">style="display: none;"</c:if>>
                    <mde:write name="actionBean" property="searchForm"/>
                    <div class="searchFormButtons">
                        <portofino:buttons list="crud-search-form" />
                    </div>
                </div>
            </c:if>
            <c:if test="${not empty actionBean.searchString}">
                <input type="hidden" name="searchString" value="<c:out value="${actionBean.searchString}"/>"/>
            </c:if>
            <jsp:include page="/m/crud/datatable.jsp" />
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>