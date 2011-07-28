<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><stripes:layout-render name="/skins/${skin}/crud/common.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.CrudAction"/>
    <stripes:layout-component name="buttons">
        <c:if test="${not empty actionBean.returnToSearchTarget}">
            <stripes:submit id="Table_returnToSearch" name="returnToSearch" value="<< Return to ${actionBean.returnToSearchTarget}"/>
        </c:if>
        <stripes:submit id="Table_create" name="create" value="Create new"/>
        <stripes:submit id="Table_bulkEdit" name="bulkEdit" value="Edit"/>
        <stripes:submit id="Table_bulkDelete" name="bulkDelete" value="Delete"
                  onclick="return confirm ('Are you sure?');"/>
        <stripes:submit id="Table_print" name="print" value="Print" disabled="true"/>
        <stripes:submit id="Table_exportExcel" name="exportSearchExcel" value="Excel" disabled="false"/>
        <stripes:submit id="Table_exportPdf" name="exportSearchPdf" value="Pdf" disabled="false"/>
        <!-- TODO custom buttons -->
    </stripes:layout-component>
    <stripes:layout-component name="innerContent">
        <h1><c:out value="${actionBean.crud.searchTitle}"/></h1>
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

        <c:forEach var="embeddedChild" items="${actionBean.embeddedChildren}">
            <jsp:include page="${embeddedChild}" />
        </c:forEach>
    </stripes:layout-component>
</stripes:layout-render>