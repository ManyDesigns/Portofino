<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino" %>
<stripes:layout-render name="/skins/default/wizard-page.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.admin.appwizard.ApplicationWizard"/>
    <stripes:layout-component name="pageTitle">
        <fmt:message key="appwizard.step1.title" />
    </stripes:layout-component>
    <stripes:layout-component name="contentHeader">
    </stripes:layout-component>
    <stripes:layout-component name="portletTitle">
        <fmt:message key="appwizard.step1.title" />
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <mde:sessionMessages />
        <script type="text/javascript">
            function changeSPForm() {
                $("#jndiCPForm").toggle();
                $("#jdbcCPForm").toggle();
            }
        </script>
        <fmt:message key="appwizard.connectionType" />
        <ul>
            <li><input id="jdbc_radio" type="radio" value="JDBC"
                       name="connectionProviderType"
                       <%= actionBean.isJdbc() ? "checked='checked'" : "" %>
                       onchange="changeSPForm();"
                    />
                <label for="jdbc_radio">JDBC</label></li>
            <li><input id="jndi_radio" type="radio" value="JNDI"
                       name="connectionProviderType"
                       <%= actionBean.isJndi() ? "checked='checked'" : "" %>
                       onchange="changeSPForm();"/>
                <label for="jndi_radio">JNDI</label></li>
        </ul>
        <div id="jndiCPForm" style='display: <%= actionBean.isJndi() ? "inherit" : "none" %>'>
            <mde:write name="actionBean" property="jndiCPForm"/>
        </div>
        <div id="jdbcCPForm" style='display: <%= actionBean.isJdbc() ? "inherit" : "none" %>'>
            <mde:write name="actionBean" property="jdbcCPForm"/>
        </div>

        <mde:write name="actionBean" property="advancedOptionsForm" />
    </stripes:layout-component>
    <stripes:layout-component name="contentFooter">
        <script type="text/javascript">
            $(function() {
                var buttons = $(".contentFooter button");
                buttons.click(function() {
                    buttons.unbind("click");
                    buttons.click(function() {
                        alert("Please wait for the operation to complete");
                        return false;
                    });
                });
            });
        </script>
        <portofino:buttons list="create-connection-provider" cssClass="contentButton" />
    </stripes:layout-component>
</stripes:layout-render>