<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino" %>
<stripes:layout-render name="/m/admin/admin-theme/admin-page.jsp">
    <jsp:useBean id="actionBean" scope="request"
                 type="com.manydesigns.portofino.actions.admin.appwizard.ApplicationWizard"/>
    <stripes:layout-component name="pageTitle">
        <fmt:message key="generate.pages" />
    </stripes:layout-component>
    <stripes:layout-component name="pageHeader">
        <jsp:include page="wizard-content-header.jsp" />
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <stripes:form beanclass="com.manydesigns.portofino.actions.admin.appwizard.ApplicationWizard"
                      method="post">
            <style type="text/css">
                ul li {
                    list-style-type: none;
                }
                #calendarField label.control-label {
                    width: auto;
                    float: left;
                    padding-right: 30px;
                }
            </style>
            <p><fmt:message key="select.the.generation.strategy" /></p>
            <div class="form-group">
                <div class="radio">
                    <label>
                        <input type="radio" name="generationStrategy" value="AUTO" id="generationStrategy_auto"
                               ${actionBean.generationStrategy eq 'AUTO' ? 'checked="checked"' : ''} />
                        <fmt:message key="automatic" />
                    </label>
                </div>
                <div class="radio">
                    <label>
                        <input type="radio" name="generationStrategy" value="MANUAL" id="generationStrategy_manual"
                               ${actionBean.generationStrategy eq 'MANUAL' ? 'checked="checked"' : ''} />
                        <fmt:message key="manual.choose.which.pages.will.be.created" />
                    </label>
                </div>
                <div class="radio">
                    <label>
                        <input type="radio" name="generationStrategy" value="NO" id="generationStrategy_no"
                               ${actionBean.generationStrategy eq 'NO' ? 'checked="checked"' : ''} />
                        <fmt:message key="dont.generate.anything" />
                    </label>
                </div>
            </div>
            <div id="rootsFormContainer">
                <span id="calendarField"><mde:write name="actionBean" property="generateCalendarField" /></span>
                <h4><fmt:message key="select.root.tables" /></h4>
                <mde:write name="actionBean" property="rootsForm"/>
            </div>
            <div style="display: none;">
                <mde:write name="actionBean" property="userManagementSetupForm"/>
                <mde:write name="actionBean" property="userAndGroupTablesForm"/>
                <mde:write name="actionBean" property="schemasForm"/>
                <input type="hidden" name="connectionProviderType" value="${actionBean.connectionProviderType}" />
                <mde:write name="actionBean" property="connectionProviderField" />
                <mde:write name="actionBean" property="jndiCPForm"/>
                <mde:write name="actionBean" property="jdbcCPForm"/>
            </div>
            <script type="text/javascript">
                function toggleRootsForm() {
                    if($("#generationStrategy_manual").prop("checked")) {
                        $("#rootsFormContainer").show();
                    } else {
                        $("#rootsFormContainer").hide();
                    }
                }

                $(function() {
                    toggleRootsForm();
                    $("input[name=generationStrategy]").change(toggleRootsForm);
                });
            </script>
            <div>
                <portofino:buttons list="select-tables" />
            </div>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>