<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"%>
<%@taglib prefix="mde" uri="/manydesigns-elements"%>

<stripes:layout-render name="/skins/default/admin-page.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.CrudAction"/>
    <stripes:layout-component name="pageTitle">
        <c:out value="${actionBean.crud.searchTitle}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletTitle">
        <c:out value="${actionBean.crud.searchTitle}"/>
    </stripes:layout-component>
    <stripes:layout-component name="contentHeader">
        <stripes:layout-render name="/layouts/page/buttons.jsp" list="contentButtons" cssClass="contentButton" />
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <c:if test="${not empty actionBean.searchForm}">
            <div class="yui-gc">
                <div class="yui-u first">
                    <div class="search_results withSearchForm">
        </c:if>
        <c:if test="${empty actionBean.searchForm}">
                    <div class="search_results">
        </c:if>
                        <%@include file="/layouts/crud/datatable.jsp"%>
                        <stripes:layout-render name="/layouts/page/buttons.jsp" list="crud-search" cssClass="portletButton" />
                    </div>
                    <!-- TODO custom buttons -->
        <c:if test="${not empty actionBean.searchForm}">
                </div>
                <div class="yui-u">
                        <div class="search_form">
                            <mde:write name="actionBean" property="searchForm"/>
                            <div class="searchFormButtons">
                                <stripes:layout-render name="/layouts/page/buttons.jsp" list="crud-search-form" cssClass="portletButton" />
                            </div>
                        </div>
                </div>
                <div style="clear: both;">&nbsp;</div>
            </div>
        </c:if>

        <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>

    </stripes:layout-component>
    <stripes:layout-component name="contentFooter">
        <stripes:layout-render name="/layouts/page/buttons.jsp" list="contentButtons" cssClass="contentButton" />
    </stripes:layout-component>
</stripes:layout-render>