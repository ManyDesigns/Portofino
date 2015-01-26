Chart.defaults.global.responsive = true;

var portofino = portofino || {};

portofino.charts = portofino.charts || {};
portofino.charts.chartjs = portofino.charts.chartjs || {};

/* http://www.colorbrewer2.org/ */

portofino.charts.chartjs.colorConfigurarions1D = [
    {
        highlight: "rgba(141,211,199,0.5)",
        color: "rgba(141,211,199,1)"
    },
    {
        highlight: "rgba(255,255,179,0.5)",
        color: "rgba(255,255,179,1)"
    },
    {
        highlight: "rgba(190,186,218,0.5)",
        color: "rgba(190,186,218,1)"
    },
    {
        highlight: "rgba(251,128,114,0.5)",
        color: "rgba(251,128,114,1)"
    },
    {
        highlight: "rgba(128,177,211,0.5)",
        color: "rgba(128,177,211,1)"
    },
    {
        highlight: "rgba(253,180,98,0.5)",
        color: "rgba(253,180,98,1)"
    },
    {
        highlight: "rgba(179,222,105,0.5)",
        color: "rgba(179,222,105,1)"
    },
    {
        highlight: "rgba(252,205,229,0.5)",
        color: "rgba(252,205,229,1)"
    },
    {
        highlight: "rgba(217,217,217,0.5)",
        color: "rgba(217,217,217,1)"
    },
    {
        highlight: "rgba(188,128,189,0.5)",
        color: "rgba(188,128,189,1)"
    },
    {
        highlight: "rgba(204,235,197,0.5)",
        color: "rgba(204,235,197,1)"
    },
    {
        highlight: "rgba(255,237,111,0.5)",
        color: "rgba(255,237,111,1)"
    }
];

portofino.charts.chartjs.colorConfigurarions2D = [
    {
        fillColor: "rgba(141,211,199,0.5)",
        strokeColor: "rgba(141,211,199,1)",
        pointColor: "rgba(141,211,199,1)",
        pointStrokeColor: "#fff",
        pointHighlightFill: "#fff",
        pointHighlightStroke: "rgba(141,211,199,1)"
    },
    {
        fillColor: "rgba(255,255,179,0.5)",
        strokeColor: "rgba(255,255,179,1)",
        pointColor: "rgba(255,255,179,1)",
        pointStrokeColor: "#fff",
        pointHighlightFill: "#fff",
        pointHighlightStroke: "rgba(255,255,179,1)"
    },
    {
        fillColor: "rgba(190,186,218,0.5)",
        strokeColor: "rgba(190,186,218,1)",
        pointColor: "rgba(190,186,218,1)",
        pointStrokeColor: "#fff",
        pointHighlightFill: "#fff",
        pointHighlightStroke: "rgba(190,186,218,1)"
    },
    {
        fillColor: "rgba(251,128,114,0.5)",
        strokeColor: "rgba(251,128,114,1)",
        pointColor: "rgba(251,128,114,1)",
        pointStrokeColor: "#fff",
        pointHighlightFill: "#fff",
        pointHighlightStroke: "rgba(251,128,114,1)"
    },
    {
        fillColor: "rgba(128,177,211,0.5)",
        strokeColor: "rgba(128,177,211,1)",
        pointColor: "rgba(128,177,211,1)",
        pointStrokeColor: "#fff",
        pointHighlightFill: "#fff",
        pointHighlightStroke: "rgba(128,177,211,1)"
    },
    {
        fillColor: "rgba(253,180,98,0.5)",
        strokeColor: "rgba(253,180,98,1)",
        pointColor: "rgba(253,180,98,1)",
        pointStrokeColor: "#fff",
        pointHighlightFill: "#fff",
        pointHighlightStroke: "rgba(253,180,98,1)"
    },
    {
        fillColor: "rgba(179,222,105,0.5)",
        strokeColor: "rgba(179,222,105,1)",
        pointColor: "rgba(179,222,105,1)",
        pointStrokeColor: "#fff",
        pointHighlightFill: "#fff",
        pointHighlightStroke: "rgba(179,222,105,1)"
    },
    {
        fillColor: "rgba(252,205,229,0.5)",
        strokeColor: "rgba(252,205,229,1)",
        pointColor: "rgba(252,205,229,1)",
        pointStrokeColor: "#fff",
        pointHighlightFill: "#fff",
        pointHighlightStroke: "rgba(252,205,229,1)"
    },
    {
        fillColor: "rgba(217,217,217,0.5)",
        strokeColor: "rgba(217,217,217,1)",
        pointColor: "rgba(217,217,217,1)",
        pointStrokeColor: "#fff",
        pointHighlightFill: "#fff",
        pointHighlightStroke: "rgba(217,217,217,1)"
    },
    {
        fillColor: "rgba(188,128,189,0.5)",
        strokeColor: "rgba(188,128,189,1)",
        pointColor: "rgba(188,128,189,1)",
        pointStrokeColor: "#fff",
        pointHighlightFill: "#fff",
        pointHighlightStroke: "rgba(188,128,189,1)"
    },
    {
        fillColor: "rgba(204,235,197,0.5)",
        strokeColor: "rgba(204,235,197,1)",
        pointColor: "rgba(204,235,197,1)",
        pointStrokeColor: "#fff",
        pointHighlightFill: "#fff",
        pointHighlightStroke: "rgba(204,235,197,1)"
    },
    {
        fillColor: "rgba(255,237,111,0.5)",
        strokeColor: "rgba(255,237,111,1)",
        pointColor: "rgba(255,237,111,1)",
        pointStrokeColor: "#fff",
        pointHighlightFill: "#fff",
        pointHighlightStroke: "rgba(255,237,111,1)"
    }];

portofino.charts.chartjs.create = function (chartId, chartKind, chartJsMethod, data) {
    var canvas = document.getElementById(chartId);
    var ctx = canvas.getContext("2d");
    var index = 0;
    var cfgs, ds, cc;
    if (chartKind == 1) { //1D charts
        cfgs = portofino.charts.chartjs.colorConfigurarions1D;
        for (ds in data) {
            var datum = data[ds];
            cc = cfgs[index % cfgs.length];
            $.extend(datum, cc);
            index++;
        }
    } else if (chartKind == 2) { //2D charts
        cfgs = portofino.charts.chartjs.colorConfigurarions2D;
        for (ds in data.datasets) {
            var dataset = data.datasets[ds];
            cc = cfgs[index % cfgs.length];
            $.extend(dataset, cc);
            index++;
        }
    }
    var chart = ((new Chart(ctx))[chartJsMethod])(data);
    var legend = $(chart.generateLegend());
    $(canvas).parent().siblings(".legend-container").append(legend);
    legend.find("li").each(function(index, elem) {
        elem = $(elem);
        elem.mouseover(function() {
            if (chart.segments) {
                var activeSegment = chart.segments[index];
                activeSegment.save();
                activeSegment.fillColor = activeSegment.highlightColor;
                chart.showTooltip([activeSegment], true);
                activeSegment.restore();
            } else if (chart.datasets) {
                var activeDs = chart.datasets[index];
                var k, array, object;
                if (activeDs.points) {
                    array = activeDs.points;
                } else if (activeDs.bars) {
                    array = activeDs.bars;
                } else {
                    array = []; //Not supported
                }
                for (k in array) {
                    object = array[k];
                    object.save();
                    object.fillColor = object.highlightFill;
                    object.strokeColor = object.highlightStroke;
                }
                chart.draw();
                for (k in array) {
                    object = array[k];
                    object.restore();
                }
            }
        });
        elem.find("span").html("&nbsp;").after(" ");
    });
    legend.mouseout(function() {
        chart.draw();
    });
    return {
        chart: chart,
        canvas: canvas,
        legend: legend
    };
};
