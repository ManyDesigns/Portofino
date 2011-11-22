<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"%>
<%@taglib prefix="mde" uri="/manydesigns-elements"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="portofino" %>
<stripes:layout-render name="/skins/${skin}/portlet.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.CrudAction"/>
    <stripes:layout-component name="portletTitle">
        <c:out value="${actionBean.crud.searchTitle}"/>
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
                        <%@include file="datatable.jsp"%>
                        <portofino:buttons list="crud-search" cssClass="portletButton" />
                    </div>
        <c:if test="${not empty actionBean.searchForm}">
                </div>
                <div class="yui-u">
                        <div class="search_form">
                            <mde:write name="actionBean" property="searchForm"/>
                            <div class="searchFormButtons">
                                <portofino:buttons list="crud-search-form" cssClass="portletButton" />
                            </div>
                        </div>
                </div>
                <div style="clear: both;">&nbsp;</div>
            </div>
        </c:if>

        <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>

    </stripes:layout-component>

    <script type="text/javascript">
        $(".search_results button[name=bulkDelete]").click(function() {
            return confirm ('Are you sure?');
        });
    </script>
</stripes:layout-render>