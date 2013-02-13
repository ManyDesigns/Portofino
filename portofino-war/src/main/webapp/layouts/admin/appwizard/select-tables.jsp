<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino" %>
<stripes:layout-render name="/skins/default/admin-page.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.admin.appwizard.ApplicationWizard"/>
    <stripes:layout-component name="pageTitle">
        <fmt:message key="appwizard.step4.title" />
    </stripes:layout-component>
    <stripes:layout-component name="contentHeaderContainer">
        <jsp:include page="/skins/default/wizard-content-header.jsp" />
    </stripes:layout-component>
    <stripes:layout-component name="portletTitle">
        <fmt:message key="appwizard.step4.title" />
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <style type="text/css">
            ul li {
                list-style-type: none;
            }
        </style>
        <mde:sessionMessages />
        <p><fmt:message key="appwizard.createPages.selectStrategy" /></p>
        <ul>
            <li><input type="radio" name="generationStrategy" value="NO" id="generationStrategy_no"
                       ${actionBean.generationStrategy eq 'NO' ? 'checked="checked"' : ''} />
                <label for="generationStrategy_no"><fmt:message key="appwizard.createPages.strategy.no" /></label></li>
            <li><input type="radio" name="generationStrategy" value="AUTO" id="generationStrategy_auto"
                       ${actionBean.generationStrategy eq 'AUTO' ? 'checked="checked"' : ''} />
                <label for="generationStrategy_auto"><fmt:message key="appwizard.createPages.strategy.auto" /></label></li>
            <li><input type="radio" name="generationStrategy" value="MANUAL" id="generationStrategy_manual"
                       ${actionBean.generationStrategy eq 'MANUAL' ? 'checked="checked"' : ''} />
                <label for="generationStrategy_manual"><fmt:message key="appwizard.createPages.strategy.manual" /></label></li>
        </ul>
        <div id="rootsFormContainer">
            <span style="font-weight: bold;"><mde:write name="actionBean" property="generateCalendarField" /></span>
            <h3><fmt:message key="appwizard.roots.select" /></h3>
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
    </stripes:layout-component>
    <stripes:layout-component name="contentFooter">
        <script type="text/javascript">
            function toggleRootsForm() {
                if($("#generationStrategy_manual").prop("checked")) {
                    $("#rootsFormContainer").show();
                } else {
                    $("#rootsFormContainer").hide();
                }
            }

            $(function() {
                var buttons = $(".contentFooter button");
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
        <portofino:buttons list="select-tables" cssClass="contentButton" />
    </stripes:layout-component>
</stripes:layout-render>