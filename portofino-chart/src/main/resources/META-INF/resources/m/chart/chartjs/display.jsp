<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><stripes:layout-render name="/theme/templates/${actionBean.pageInstance.layout.template}/normal.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.chart.chartjs.ChartJsAction"/>
    <stripes:layout-component name="pageTitle">
        <c:out value="${actionBean.pageInstance.page.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <div class="chart-container">
            <button type="button" class="btn btn-link" onclick="$('#legend-${actionBean.chartId}').toggle();"><fmt:message key="legend" /></button>
            <div id="legend-${actionBean.chartId}" class="legend-container" style="display: none;"></div>
            <div class="canvas-container">
                <canvas id="${actionBean.chartId}" width="${actionBean.width}" height="${actionBean.height}"></canvas>
            </div>
        </div>
        <script type="text/javascript">
            $(function() {
                var chartId = "${actionBean.chartId}";
                var chartKind = ${actionBean.chartConfiguration.actualType.kind};
                var chartJsMethod = "${actionBean.chartConfiguration.actualType.jsName}";
                var data = ${actionBean.chartData};

                portofino.charts.chartjs.create(chartId, chartKind, chartJsMethod, data);
            });
        </script>
        <portofino:buttons list="chart-buttons" />
    </stripes:layout-component>
</stripes:layout-render>