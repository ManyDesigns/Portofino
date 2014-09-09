<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><stripes:layout-render name="/theme/templates/${actionBean.pageInstance.layout.template}/normal.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.chart.chartjs.ChartJsAction"/>
    <stripes:layout-component name="pageTitle">
        <c:out value="${actionBean.pageInstance.page.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <canvas id="${actionBean.chartId}" width="${actionBean.width}" height="${actionBean.height}"></canvas>
        <script type="text/javascript">
            $(function() {
                var ctx = document.getElementById("${actionBean.chartId}").getContext("2d");
                var data = ${actionBean.chartData};
                var chartKind = ${actionBean.chartConfiguration.actualType.kind};

                var index = 0;
                var cfgs, ds, cc;
                if(chartKind == 1) { //2D charts
                    cfgs = portofino.charts.chartjs.colorConfigurarions1D;
                    for(ds in data) {
                        var datum = data[ds];
                        cc = cfgs[index % cfgs.length];
                        $.extend(datum, cc);
                        index++;
                    }
                } else if(chartKind == 2) { //2D charts
                    cfgs = portofino.charts.chartjs.colorConfigurarions2D;
                    for(ds in data.datasets) {
                        var dataset = data.datasets[ds];
                        cc = cfgs[index % cfgs.length];
                        $.extend(dataset, cc);
                        index++;
                    }
                }
                new Chart(ctx).${actionBean.chartConfiguration.actualType.jsName}(data);
            });
        </script>
        <portofino:buttons list="chart-buttons" />
    </stripes:layout-component>
</stripes:layout-render>