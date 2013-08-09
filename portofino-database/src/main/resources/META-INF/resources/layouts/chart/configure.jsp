<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ page import="org.apache.commons.lang.StringEscapeUtils"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><jsp:useBean id="actionBean" scope="request"
               type="com.manydesigns.portofino.pageactions.chart.ChartAction"
/><stripes:layout-render name="/skins/${skin}${actionBean.pageTemplate}/modal.jsp">
    <stripes:layout-component name="portletTitle">
        <fmt:message key="com.manydesigns.portofino.pageactions.configure">
            <fmt:param value="<%= StringEscapeUtils.escapeHtml(actionBean.getPage().getTitle()) %>" />
        </fmt:message>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <stripes:form action="${actionBean.dispatch.originalPath}" method="post"
                      class="form-horizontal">
            <mde:write name="actionBean" property="pageConfigurationForm"/>
            <mde:write name="actionBean" property="form" />
            <br />
            <h4>SQL Query parameters</h4>
            <ul>
                <li><strong>1D Charts:</strong>
                    key, value, (optional) label</li>
                <li><strong>2D Charts:</strong>
                    x axis key, y axis key, value, (optional) x axis label, (optional) y axis label</li>
            </ul>
            <%@include file="../script-configuration.jsp" %>
            <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
            <div class="form-actions">
                <portofino:buttons list="configuration" />
            </div>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>