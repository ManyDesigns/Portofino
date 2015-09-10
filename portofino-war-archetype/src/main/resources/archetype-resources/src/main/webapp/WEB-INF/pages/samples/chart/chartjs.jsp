<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"
        %><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
        %><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
        %><jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.custom.CustomAction"
        /><stripes:layout-render name="/theme/templates/${actionBean.pageTemplate}/normal.jsp">
    <stripes:layout-component name="pageTitle">
        <c:out value="${actionBean.page.title}"/>
        (<c:out value="${actionBean.page.description}"/>)
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">

        <style type="text/css">
            .graph-title {
                text-align: center;
                color: #666666;
                letter-spacing: 0.04em;
                font-family: tahoma , sans-serif ;
            }
        </style>

        <script type="text/javascript">
            var data_line = {
                labels: ["January", "February", "March", "April", "May", "June", "July"],
                datasets: [
                    {
                        label: "My First dataset",
                        fillColor: "rgba(220,220,220,0.2)",
                        strokeColor: "rgba(220,220,220,1)",
                        pointColor: "rgba(220,220,220,1)",
                        pointStrokeColor: "#fff",
                        pointHighlightFill: "#fff",
                        pointHighlightStroke: "rgba(220,220,220,1)",
                        data: [65, 59, 80, 81, 56, 55, 40]
                    },
                    {
                        label: "My Second dataset",
                        fillColor: "rgba(151,187,205,0.2)",
                        strokeColor: "rgba(151,187,205,1)",
                        pointColor: "rgba(151,187,205,1)",
                        pointStrokeColor: "#fff",
                        pointHighlightFill: "#fff",
                        pointHighlightStroke: "rgba(151,187,205,1)",
                        data: [28, 48, 40, 19, 86, 27, 90]
                    }
                ]
            };

            var data_bar = {
                labels: ["January", "February", "March", "April", "May", "June", "July"],
                datasets: [
                    {
                        label: "My First dataset",
                        fillColor: "rgba(220,220,220,0.5)",
                        strokeColor: "rgba(220,220,220,0.8)",
                        highlightFill: "rgba(220,220,220,0.75)",
                        highlightStroke: "rgba(220,220,220,1)",
                        data: [65, 59, 80, 81, 56, 55, 40]
                    },
                    {
                        label: "My Second dataset",
                        fillColor: "rgba(151,187,205,0.5)",
                        strokeColor: "rgba(151,187,205,0.8)",
                        highlightFill: "rgba(151,187,205,0.75)",
                        highlightStroke: "rgba(151,187,205,1)",
                        data: [28, 48, 40, 19, 86, 27, 90]
                    }
                ]
            };

            var data_pie = [
                {
                    value: 300,
                    color:"#F7464A",
                    highlight: "#FF5A5E",
                    label: "Red"
                },
                {
                    value: 50,
                    color: "#46BFBD",
                    highlight: "#5AD3D1",
                    label: "Green"
                },
                {
                    value: 100,
                    color: "#FDB45C",
                    highlight: "#FFC870",
                    label: "Yellow"
                }
            ]

            $(function(){
                var ctx_line = document.getElementById("line").getContext("2d");
                window.myLine = new Chart(ctx_line).Line(data_line, { responsive: true });

                var ctx_bar = document.getElementById("bar").getContext("2d");
                window.myBar = new Chart(ctx_bar).Bar(data_bar, { responsive: true });

                var ctx_pie = document.getElementById("pie").getContext("2d");
                window.myPie = new Chart(ctx_pie).Pie(data_pie, { responsive: true });

                var ctx_doughnut = document.getElementById("doughnut").getContext("2d");
                window.myPie = new Chart(ctx_doughnut).Doughnut(data_pie, { responsive: true });
            });
        </script>

        <div class="row"  >
            <div class="col-md-5" >
                <h3 class="graph-title">Line</h3>
                <canvas id="line" height="450" width="600"></canvas>
            </div>
            <div class="col-md-2" > </div>
            <div class="col-md-5" >
                <h3 class="graph-title">Bar </h3>
                <canvas id="bar" height="450" width="600"></canvas>
            </div>
        </div>
        
        <hr>
        
        <div class="row"  >
            <div class="col-md-5" >
                <h3 class="graph-title">Pie </h3>
                <canvas id="pie" height="450" width="600"></canvas>
            </div>
            <div class="col-md-2" > </div>
            <div class="col-md-5" >
                <h3 class="graph-title">Doughnut </h3>
                <canvas id="doughnut" height="450" width="600"></canvas>
            </div>
        </div>

    </stripes:layout-component>
</stripes:layout-render>