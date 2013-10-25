<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ page import="org.apache.commons.lang.StringEscapeUtils"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><jsp:useBean id="actionBean" scope="request"
               type="com.manydesigns.portofino.pageactions.crud.AbstractCrudAction"
/><stripes:layout-render name="/theme/templates/${actionBean.pageInstance.layout.template}/modal.jsp">
    <stripes:layout-component name="pageTitle">
        <fmt:message key="com.manydesigns.portofino.pageactions.configure">
            <fmt:param value="<%= StringEscapeUtils.escapeHtml(actionBean.getPage().getTitle()) %>" />
        </fmt:message>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <stripes:form action="${actionBean.context.actualServletPath}" method="post"
                      class="form-horizontal">
            <mde:write name="actionBean" property="pageConfigurationForm"/>
            <mde:write name="actionBean" property="crudConfigurationForm" />

            <!-- Properties -->
            <fieldset id="crudPropertiesFieldset">
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

            <fieldset id="crudSelectionProvidersFieldset">
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

            <%@include file="/m/pageactions/script-configuration.jsp" %>
            <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
            <div class="form-actions">
                <portofino:buttons list="configuration" />
            </div>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>