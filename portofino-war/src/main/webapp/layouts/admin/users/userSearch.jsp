<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"%>
<%@taglib prefix="mde" uri="/manydesigns-elements"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="portofino" %>
<stripes:layout-render name="/skins/default/admin-page.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.crud.CrudAction"/>
    <stripes:layout-component name="pageTitle">
        <c:out value="${actionBean.crudConfiguration.searchTitle}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletTitle">
        <c:out value="${actionBean.crudConfiguration.searchTitle}"/>
    </stripes:layout-component>
    <stripes:layout-component name="contentHeader">
        <portofino:buttons list="contentButtons" cssClass="contentButton" />
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <c:if test="${not empty actionBean.searchForm}">
            <a class="search_form_toggle_link" href="#">
                <c:if test="${actionBean.searchVisible}"><fmt:message key="layouts.crud.search.hideSearch" /></c:if>
                <c:if test="${!actionBean.searchVisible}"><fmt:message key="layouts.crud.search.showSearch" /></c:if>
            </a>
            <div class="search_form" <c:if test="${!actionBean.searchVisible}">style="display: none;"</c:if>>
                <mde:write name="actionBean" property="searchForm"/>
                <div class="searchFormButtons">
                    <portofino:buttons list="crud-search-form" cssClass="portletButton" />
                </div>
            </div>
        </c:if>
        <div class="search_results">
            <%@include file="/layouts/crud/datatable.jsp"%>
            <portofino:buttons list="crud-search" cssClass="portletButton" />
        </div>

        <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>

        <script type="text/javascript">
            $(".search_results button[name=bulkDelete]").click(function() {
                return confirm ('<fmt:message key="commons.confirm" />');
            });
            $(".search_form_toggle_link").click(makeToggleFunction());
            function makeToggleFunction() {
                var visible = ${actionBean.searchVisible};
                return function(event) {
                    $(this).next().slideToggle(300);
                    visible = !visible;
                    if(visible) {
                        $(event.target).html('<fmt:message key="layouts.crud.search.hideSearch" />');
                    } else {
                        $(event.target).html('<fmt:message key="layouts.crud.search.showSearch" />');
                    }
                };
            }
        </script>
    </stripes:layout-component>
    <stripes:layout-component name="contentFooter">
        <portofino:buttons list="contentButtons" cssClass="contentButton" />
    </stripes:layout-component>
</stripes:layout-render>