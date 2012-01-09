<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino" %>
<stripes:layout-render name="/skins/${skin}/modal-page.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.CrudAction"/>
    <stripes:layout-component name="contentHeader">
        <portofino:buttons list="configuration" cssClass="contentButton" />
        <div class="breadcrumbs">
            <div class="inner">
                <mde:write name="breadcrumbs"/>
            </div>
        </div>
    </stripes:layout-component>
    <stripes:layout-component name="portletHeader">
        <%@include file="../portlet-common-configuration.jsp" %>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <mde:write name="actionBean" property="crudConfigurationForm"/>
        <!-- Properties -->
        <fieldset id="crudPropertiesFieldset" class="mde-form-fieldset" style="padding-top: 1em; margin-top: 1em;">
            <legend><fmt:message key= "layouts.crud.configure.properties" /></legend>
            <c:if test="${not empty actionBean.propertiesTableForm}">
                <mde:write name="actionBean" property="propertiesTableForm"/>
            </c:if>
            <c:if test="${empty actionBean.propertiesTableForm}">
                <fmt:message key= "layouts.crud.configure.table_first" />
            </c:if>
        </fieldset>
        <c:if test="${not empty actionBean.propertiesTableForm}">
            <script type="text/javascript">
                var inputs = $("#crudPropertiesFieldset tr").find("td:first input[type=checkbox]");
                inputs.each(function(i, obj) {
                    obj = $(obj);
                    var rowInputs = obj.parent().siblings().find("input");
                    function toggleRow() {
                        if(!obj.is(':checked')) {
                            rowInputs.attr('disabled', 'disabled');
                        } else {
                            rowInputs.removeAttr('disabled');
                        }
                    }
                    obj.click(toggleRow);
                    toggleRow();
                });
            </script>
        </c:if>
        <!-- End properties -->

        <fieldset id="crudSelectionProvidersFieldset" class="mde-form-fieldset"
                  style="padding-top: 1em; margin-top: 1em;">
            <legend><fmt:message key="layouts.crud.configure.selectionProviders" /></legend>
            <c:choose>
                <c:when test="${not empty actionBean.selectionProvidersForm}">
                    <mde:write name="actionBean" property="selectionProvidersForm"/>
                </c:when>
                <c:otherwise>
                    <fmt:message key="layouts.crud.configure.noSelectionProviders" />
                </c:otherwise>
            </c:choose>
        </fieldset>

        <%@include file="../script-configuration.jsp" %>

        <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletFooter"/>
    <stripes:layout-component name="contentFooter">
        <portofino:buttons list="configuration" cssClass="contentButton" />
    </stripes:layout-component>
</stripes:layout-render>