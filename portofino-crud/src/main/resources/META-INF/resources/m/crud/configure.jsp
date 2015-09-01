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
/><stripes:layout-render name="/theme/templates/${actionBean.pageTemplate}/modal.jsp">
    <stripes:layout-component name="pageTitle">
        <fmt:message key="configure.page._">
            <fmt:param value="<%= StringEscapeUtils.escapeHtml(actionBean.getPage().getTitle()) %>" />
        </fmt:message>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <stripes:form action="${actionBean.context.actionPath}" method="post" class="form-horizontal">
            <mde:write name="actionBean" property="pageConfigurationForm"/>
            <mde:write name="actionBean" property="crudConfigurationForm" />

            <!-- Properties -->
            <fieldset id="crudPropertiesFieldset">
                <legend><fmt:message key= "properties" /></legend>
                <c:if test="${not empty actionBean.propertiesTableForm}">
                    <mde:write name="actionBean" property="propertiesTableForm"/>
                </c:if>
                <c:if test="${empty actionBean.propertiesTableForm}">
                    <p><fmt:message key= "you.must.write.a.query.first" /></p>
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
                        firstCell.html("<div class='checkbox'><input type='checkbox' id='crudPropertiesSelectAll' /><label for='crudPropertiesSelectAll'></label></div>");
                        var allCheckbox = firstCell.find("input");
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
                <legend><fmt:message key="selection.providers" /></legend>
                <c:choose>
                    <c:when test="${not empty actionBean.selectionProvidersForm}">
                        <mde:write name="actionBean" property="selectionProvidersForm"/>
                    </c:when>
                    <c:otherwise>
                        <p><fmt:message key="none.available" /></p>
                    </c:otherwise>
                </c:choose>
            </fieldset>

            <jsp:include page="/m/pageactions/script-configuration.jsp" />
            <input type="hidden" name="returnUrl" value="<c:out value="${actionBean.returnUrl}"/>"/>
            <div class="form-group">
                <div class="col-sm-12 ">
                    <portofino:buttons list="configuration" />
                </div>
            </div>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>