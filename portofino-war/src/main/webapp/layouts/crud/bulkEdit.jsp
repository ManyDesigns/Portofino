<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><stripes:layout-render name="/layouts/portlet-page-definition.jsp">
    <stripes:layout-component name="portletPageHeader">
        <stripes:submit name="bulkUpdate" value="Update" class="portletPageButton"/>
        <stripes:submit name="cancel" value="Cancel" class="portletPageButton"/>
        <div class="breadcrumbs">
            <div class="inner">
                <mde:write name="breadcrumbs"/>
            </div>
        </div>
    </stripes:layout-component>
    <stripes:layout-component name="portletPageBody">
        <div class="portletWrapper first">
            <stripes:layout-render name="/skins/${skin}/portlet.jsp">
                <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.CrudAction"/>
                <stripes:layout-component name="portletTitle">
                    <c:out value="${actionBean.crud.editTitle}"/>
                </stripes:layout-component>
                <stripes:layout-component name="portletBody">
                    In the first column, select the fields you want to edit. Then, fill in their values.
                    <mde:write name="actionBean" property="form"/>
                    <stripes:hidden name="selection"/>
                    <c:if test="${not empty actionBean.searchString}">
                        <input type="hidden" name="searchString" value="<c:out value="${actionBean.searchString}"/>"/>
                    </c:if>
                    <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
                </stripes:layout-component>
            </stripes:layout-render>
        </div>
    </stripes:layout-component>
    <stripes:layout-component name="portletPageFooter">
        <stripes:submit name="bulkUpdate" value="Update" class="portletPageButton"/>
        <stripes:submit name="cancel" value="Cancel" class="portletPageButton"/>
    </stripes:layout-component>
</stripes:layout-render>