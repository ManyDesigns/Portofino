<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.crud.AbstractCrudAction"
/><stripes:layout-render name="/skins/${skin}${actionBean.pageTemplate}/modal.jsp">
    <stripes:layout-component name="contentHeader">
        <portofino:buttons list="configuration" cssClass="contentButton" />
        <jsp:include page="/skins/${skin}/breadcrumbs.jsp" />
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
                $(function() {
                    function updateRow(obj) {
                        var rowInputs = obj.parent().siblings().find("input");
                        if(!obj.is(':checked')) {
                            rowInputs.prop('disabled', 'disabled');
                        } else {
                            rowInputs.removeProp('disabled');
                        }
                    }

                    var inputs = $("#crudPropertiesFieldset tr").find("td:first input[type=checkbox]");
                    inputs.each(function() {
                        var obj = $(this);
                        obj.click(function() { updateRow(obj) });
                        updateRow(obj);
                    });

                    var firstCell = $("#crudPropertiesFieldset tr:first th:first");
                    firstCell.append("<input type='checkbox' />")
                    var allCheckbox = firstCell.find("input")
                    allCheckbox.click(function() {
                        inputs.each(function() {
                            var checkbox = $(this);
                            checkbox.click();
                            if(!allCheckbox.is(':checked')) {
                                checkbox.removeProp('checked');
                            } else {
                                checkbox.prop('checked', 'checked');
                            }
                            updateRow(checkbox);
                        })
                    });
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