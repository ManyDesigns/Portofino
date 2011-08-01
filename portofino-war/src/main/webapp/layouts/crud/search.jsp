<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><stripes:layout-render name="/skins/${skin}/portlet.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.CrudAction"/>
    <stripes:layout-component name="portletTitle">
        <c:out value="${actionBean.crud.searchTitle}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <div class="yui-ge">
            <div class="yui-u first">
                <div class="search_results">
                    <mde:write name="actionBean" property="tableForm"/>
                </div>
            </div>
            <div class="yui-u">
                <c:if test="${not empty actionBean.searchForm}">
                    <div class="search_form">
                        <mde:write name="actionBean" property="searchForm"/>
                        <stripes:submit name="search" value="Search"/>
                        <stripes:submit name="resetSearch" value="Reset form"/>
                    </div>
                </c:if>
            </div>
        </div>
        <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletFooter">
        <stripes:submit name="create" value="Create new"/>
        <stripes:submit name="bulkEdit" value="Edit"/>
        <stripes:submit name="bulkDelete" value="Delete" onclick="return confirm ('Are you sure?');"/>
        <stripes:submit name="print" value="Print" disabled="true"/>
        <stripes:submit name="exportSearchExcel" value="Excel" disabled="false"/>
        <stripes:submit name="exportSearchPdf" value="Pdf" disabled="false"/>
        <!-- TODO custom buttons -->
    </stripes:layout-component>
</stripes:layout-render>