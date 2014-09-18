<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"
%><stripes:layout-render name="/theme/templates/${actionBean.pageInstance.layout.template}/normal.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.chart.chartjs.ChartJsAction"/>
    <stripes:layout-component name="pageTitle">
        <c:out value="${actionBean.pageInstance.page.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <fmt:message key="no.data.available" />
        <portofino:buttons list="chart-buttons" />
    </stripes:layout-component>
</stripes:layout-render>