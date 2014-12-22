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
        <fmt:message key="connect.to.your.database" />
    </stripes:layout-component>
    <stripes:layout-component name="pageHeader">
        <jsp:include page="wizard-content-header.jsp" />
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <stripes:form beanclass="com.manydesigns.portofino.actions.admin.appwizard.ApplicationWizard"
                      method="post" class="form-horizontal">
            <script type="text/javascript">
                function changeNewSPForm() {
                    $("#jndiCPForm").toggle();
                    $("#jdbcCPForm").toggle();
                }
                $(function() {
                    $("label[for='connectionProviderName']").css("width", "auto").css("margin-right", "20px");
                })
            </script>
            <c:if test="${not empty actionBean.persistence.model.databases}">
                <mde:write name="actionBean" property="connectionProviderField" />
                <fmt:message key="or.create.a.new.one.choose.its.type" />
            </c:if>
            <c:if test="${empty actionBean.persistence.model.databases}">
                <fmt:message key="create.a.new.connection.choose.type" />
            </c:if>
            <div id="connectionProviderTypeForm" class="radio">
                <label class="radio">
                    <input id="jdbc_radio" type="radio" value="JDBC"
                           name="connectionProviderType"
                           <%= actionBean.isJdbc() ? "checked='checked'" : "" %>
                           onchange="changeNewSPForm();" />
                    JDBC</label>
                <label class="radio">
                    <input id="jndi_radio" type="radio" value="JNDI"
                           name="connectionProviderType"
                           <%= actionBean.isJndi() ? "checked='checked'" : "" %>
                           onchange="changeNewSPForm();" />
                    JNDI</label>
            </div>
            <fieldset>
                <legend>Connection parameters</legend>
                <div id="jndiCPForm" style='display: <%= actionBean.isJndi() ? "inherit" : "none" %>'>
                    <mde:write name="actionBean" property="jndiCPForm"/>
                </div>
                <div id="jdbcCPForm" style='display: <%= actionBean.isJdbc() ? "inherit" : "none" %>'>
                    <mde:write name="actionBean" property="jdbcCPForm"/>
                </div>
            </fieldset>

            <script type="text/javascript">
                $(function() {
                    $("#jdbcdriver").change(function() {
                        var connectionUrlDefaults = {
                            <c:forEach var="db" items="${actionBean.persistence.databasePlatformsRegistry.databasePlatforms}"
                                       varStatus="status">
                                <c:out value="'${db.standardDriverClassName}': '${db.connectionStringTemplate}'" escapeXml="false"/>
                                <c:if test="${!status.last}">,</c:if>
                            </c:forEach>
                        };

                        $("input[name=jdbcurl]").val(connectionUrlDefaults[$(this).val()]);
                    });

                    var toggleNewSPForm = function() {
                        if(${empty actionBean.persistence.model.databases}) {
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
            <div class="form-group">
                <div class="col-md-offset-2 col-md-10">
                    <portofino:buttons list="connection-provider" />
                </div>
            </div>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>