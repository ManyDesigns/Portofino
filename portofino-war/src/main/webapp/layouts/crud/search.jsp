<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"%>
<%@taglib prefix="mde" uri="/manydesigns-elements"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="portofino" %>
<stripes:layout-render name="/skins/${skin}/portlet.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.crud.AbstractCrudAction"/>
    <stripes:layout-component name="portletTitle">
        <c:out value="${actionBean.searchTitle}"/>
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
        <c:if test="${not empty actionBean.searchString}">
            <input type="hidden" name="searchString" value="<c:out value="${actionBean.searchString}"/>"/>
        </c:if>
        <div class="search_results">
            <%@include file="datatable.jsp"%>
            <portofino:buttons list="crud-search" cssClass="portletButton" />
        </div>
        <script type="text/javascript">
            <c:set var="pageId" value="${actionBean.pageInstance.page.id}" />
            $(function() {
                $("#portlet_${pageId} .search_results button[name=bulkDelete]").click(function() {
                    return confirm ('<fmt:message key="commons.confirm" />');
                });
                $("#portlet_${pageId} .search_form_toggle_link").click(makeToggleFunction());
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
                        return false;
                    };
                }
            });
        </script>
    </stripes:layout-component>
</stripes:layout-render>