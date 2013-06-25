<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino" %>
<stripes:layout-render name="/portofino-base/admin-theme/admin-page.jsp">
    <jsp:useBean id="actionBean" scope="request"
                 type="com.manydesigns.portofino.actions.admin.appwizard.ApplicationWizard"/>
    <stripes:layout-component name="pageTitle">
        <fmt:message key="appwizard.step4.title" />
    </stripes:layout-component>
    <stripes:layout-component name="portletHeader">
        <jsp:include page="/skins/default/wizard-content-header.jsp" />
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <stripes:form beanclass="com.manydesigns.portofino.actions.admin.appwizard.ApplicationWizard"
                      method="post" enctype="multipart/form-data" class="form-horizontal">
            <style type="text/css">
                ul li {
                    list-style-type: none;
                }
                #calendarField label {
                    width: auto; margin-right: 20px;
                }
            </style>
            <p><fmt:message key="appwizard.createPages.selectStrategy" /></p>
            <label class="radio">
                <input type="radio" name="generationStrategy" value="AUTO" id="generationStrategy_auto"
                       ${actionBean.generationStrategy eq 'AUTO' ? 'checked="checked"' : ''} />
                <fmt:message key="appwizard.createPages.strategy.auto" />
            </label>
            <label class="radio">
                <input type="radio" name="generationStrategy" value="MANUAL" id="generationStrategy_manual"
                       ${actionBean.generationStrategy eq 'MANUAL' ? 'checked="checked"' : ''} />
                <fmt:message key="appwizard.createPages.strategy.manual" />
            </label>
            <label class="radio">
                <input type="radio" name="generationStrategy" value="NO" id="generationStrategy_no"
                       ${actionBean.generationStrategy eq 'NO' ? 'checked="checked"' : ''} />
                <fmt:message key="appwizard.createPages.strategy.no" />
            </label>
            <div id="rootsFormContainer">
                <span id="calendarField"><mde:write name="actionBean" property="generateCalendarField" /></span>
                <h4><fmt:message key="appwizard.roots.select" /></h4>
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
                    var buttons = $(".form-actions button");
                    buttons.click(function() {
                        buttons.unbind("click");
                        buttons.click(function() {
                            alert("<fmt:message key='commons.waitOperation' />");
                            return false;
                        });
                    });
                    toggleRootsForm();
                    $("input[name=generationStrategy]").change(toggleRootsForm);
                });
            </script>
            <div class="form-actions" style="padding-left: 20px;">
                <portofino:buttons list="select-tables" />
            </div>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>