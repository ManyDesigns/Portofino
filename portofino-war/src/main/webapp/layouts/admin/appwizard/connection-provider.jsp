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
        <fmt:message key="appwizard.step1.title" />
    </stripes:layout-component>
    <stripes:layout-component name="contentHeaderContainer">
        <jsp:include page="/skins/default/wizard-content-header.jsp" />
    </stripes:layout-component>
    <stripes:layout-component name="portletTitle">
        <fmt:message key="appwizard.step1.title" />
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <mde:sessionMessages />
        <script type="text/javascript">
            function changeNewSPForm() {
                $("#jndiCPForm").toggle();
                $("#jdbcCPForm").toggle();
            }
        </script>
        <c:if test="${not empty actionBean.application.model.databases}">
            <mde:write name="actionBean" property="connectionProviderField" /><br />
            <fmt:message key="appwizard.orNewConnectionProvider" />
        </c:if>
        <c:if test="${empty actionBean.application.model.databases}">
            <fmt:message key="appwizard.newConnectionProvider" />
        </c:if>
        <ul id="connectionProviderTypeForm">
            <li><input id="jdbc_radio" type="radio" value="JDBC"
                       name="connectionProviderType"
                       <%= actionBean.isJdbc() ? "checked='checked'" : "" %>
                       onchange="changeNewSPForm();"
                    />
                <label for="jdbc_radio">JDBC</label></li>
            <li><input id="jndi_radio" type="radio" value="JNDI"
                       name="connectionProviderType"
                       <%= actionBean.isJndi() ? "checked='checked'" : "" %>
                       onchange="changeNewSPForm();"/>
                <label for="jndi_radio">JNDI</label></li>
        </ul>
        <div id="jndiCPForm" style='display: <%= actionBean.isJndi() ? "inherit" : "none" %>'>
            <mde:write name="actionBean" property="jndiCPForm"/>
        </div>
        <div id="jdbcCPForm" style='display: <%= actionBean.isJdbc() ? "inherit" : "none" %>'>
            <mde:write name="actionBean" property="jdbcCPForm"/>
        </div>

    </stripes:layout-component>
    <stripes:layout-component name="contentFooter">
        <script type="text/javascript">
            $(function() {
                $("#jdbcdriver").change(function() {
                    var connectionUrlDefaults = {
                        <c:forEach var="db" items="${actionBean.application.databasePlatformsManager.databasePlatforms}"
                                   varStatus="status">
                            <c:out value="'${db.standardDriverClassName}': '${db.connectionStringTemplate}'" escapeXml="false"/>
                            <c:if test="${!status.last}">,</c:if>
                        </c:forEach>
                    };

                    $("input[name=jdbcurl]").val(connectionUrlDefaults[$(this).val()]);
                });

                var buttons = $(".contentFooter button");
                buttons.click(function() {
                    buttons.unbind("click");
                    buttons.click(function() {
                        alert("<fmt:message key='commons.waitOperation' />");
                        return false;
                    });
                });

                var toggleNewSPForm = function() {
                    if(${empty actionBean.application.model.databases}) {
                        return;
                    }

                    var inputs = $("#jdbcCPForm input, #jdbcCPForm select, #connectionProviderTypeForm input");
                    if("" == $("#connectionProviderName").val()) {
                        inputs.removeAttr("disabled");
                        inputs.removeAttr("disabled");
                    } else {
                        inputs.attr("disabled", "disabled");
                        inputs.attr("disabled", "disabled");
                        inputs.attr("disabled", "disabled");
                    }
                };

                $("#connectionProviderName").change(toggleNewSPForm);

                toggleNewSPForm();
            });
        </script>
        <portofino:buttons list="connection-provider" cssClass="contentButton" />
    </stripes:layout-component>
</stripes:layout-render>