<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<%@taglib prefix="mde" uri="/manydesigns-elements"%>
<stripes:layout-render name="/skins/default/admin-page.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.CrudAction"/>
    <stripes:layout-component name="pageTitle">
        Users
    </stripes:layout-component>
    <stripes:layout-component name="portletTitle">
        Users
    </stripes:layout-component>
    <stripes:layout-component name="contentHeader"><br />
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
                        <stripes:submit name="create" value="Create new" class="portletButton"/>
                        <stripes:submit name="bulkEdit" value="Edit" class="portletButton"/>
                        <stripes:submit name="bulkDelete" value="Delete"  class="portletButton" onclick="return confirm ('Are you sure?');"/>
                        <stripes:submit name="print" value="Print" disabled="true" class="portletButton"/>
                        <stripes:submit name="exportSearchExcel" value="Excel" class="portletButton" disabled="true"/>
                        <stripes:submit name="exportSearchPdf" value="Pdf" class="portletButton" disabled="true"/>
                    </div>
                    <!-- TODO custom buttons -->
        <c:if test="${not empty actionBean.searchForm}">
                </div>
                <div class="yui-u">
                        <div class="search_form">
                            <mde:write name="actionBean" property="searchForm"/>
                            <div class="searchFormButtons">
                                <stripes:submit name="search" value="Search" class="portletButton"/>
                                <stripes:submit name="resetSearch" value="Reset form" class="portletButton"/>
                            </div>
                        </div>
                </div>
                <div style="clear: both;">&nbsp;</div>
            </div>
        </c:if>

        <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>

    </stripes:layout-component>
</stripes:layout-render>