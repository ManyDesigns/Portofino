<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><stripes:layout-render name="/skins/${skin}/portlet.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.chart.ChartAction"/>
    <stripes:layout-component name="portletTitle">
        <c:out value="${actionBean.chartPage.name}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletHeaderButtons">
        <button name="configure" class="wrench">Configure</button>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <mde:write name="actionBean" property="jfreeChartInstance"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletFooter">
        <input class="portletButton" type="submit" name="pdf" value="Pdf"/>
        <input type="hidden" name="cancelReturnUrl"
               value="<c:out value="${actionBean.cancelReturnUrl}"/> "/>
    </stripes:layout-component>
</stripes:layout-render>