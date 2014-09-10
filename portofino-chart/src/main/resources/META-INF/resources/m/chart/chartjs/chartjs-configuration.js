Chart.defaults.global.responsive = true;

var portofino = portofino || {};

portofino.charts = portofino.charts || {};
portofino.charts.chartjs = portofino.charts.chartjs || {};

portofino.charts.chartjs.colorConfigurarions1D = [
    {
        highlight: "rgba(204,82,82,0.2)",
        color: "rgba(204,82,82,1)"
    },
    {
        highlight: "rgba(82,204,82,0.2)",
        color: "rgba(82,204,82,1)"
    },
    {
        highlight: "rgba(82,82,204,0.2)",
        color: "rgba(82,82,204,1)"
    },
    {
        highlight: "rgba(217,217,87,0.2)",
        color: "rgba(217,217,87,1)"
    },
    {
        highlight: "rgba(87,217,217,0.2)",
        color: "rgba(87,217,217,1)"
    },
    {
        highlight: "rgba(217,87,217,0.2)",
        color: "rgba(217,87,217,1)"
    },
    {
        highlight: "rgba(212,148,85,0.2)",
        color: "rgba(212,148,85,1)"
    },
    {
        highlight: "rgba(148,212,85,0.2)",
        color: "rgba(148,212,85,1)"
    },
    {
        highlight: "rgba(85,212,148,0.2)",
        color: "rgba(85,212,148,1)"
    },
    {
        highlight: "rgba(85,148,212,0.2)",
        color: "rgba(85,148,212,1)"
    },
    {
        highlight: "rgba(148,85,212,0.2)",
        color: "rgba(148,85,212,1)"
    },
    {
        highlight: "rgba(212,85,148,0.2)",
        color: "rgba(212,85,148,1)"
    }
];

portofino.charts.chartjs.colorConfigurarions2D = [
    {
        fillColor: "rgba(204,82,82,0.2)",
        strokeColor: "rgba(204,82,82,1)",
        pointColor: "rgba(204,82,82,1)",
        pointStrokeColor: "#fff",
        pointHighlightFill: "#fff",
        pointHighlightStroke: "rgba(204,82,82,1)"
    },
    {
        fillColor: "rgba(82,204,82,0.2)",
        strokeColor: "rgba(82,204,82,1)",
        pointColor: "rgba(82,204,82,1)",
        pointStrokeColor: "#fff",
        pointHighlightFill: "#fff",
        pointHighlightStroke: "rgba(82,204,82,1)"
    },
    {
        fillColor: "rgba(82,82,204,0.2)",
        strokeColor: "rgba(82,82,204,1)",
        pointColor: "rgba(82,82,204,1)",
        pointStrokeColor: "#fff",
        pointHighlightFill: "#fff",
        pointHighlightStroke: "rgba(82,82,204,1)"
    },
    {
        fillColor: "rgba(217,217,87,0.2)",
        strokeColor: "rgba(217,217,87,1)",
        pointColor: "rgba(217,217,87,1)",
        pointStrokeColor: "#fff",
        pointHighlightFill: "#fff",
        pointHighlightStroke: "rgba(217,217,87,1)"
    },
    {
        fillColor: "rgba(87,217,217,0.2)",
        strokeColor: "rgba(87,217,217,1)",
        pointColor: "rgba(87,217,217,1)",
        pointStrokeColor: "#fff",
        pointHighlightFill: "#fff",
        pointHighlightStroke: "rgba(87,217,217,1)"
    },
    {
        fillColor: "rgba(217,87,217,0.2)",
        strokeColor: "rgba(217,87,217,1)",
        pointColor: "rgba(217,87,217,1)",
        pointStrokeColor: "#fff",
        pointHighlightFill: "#fff",
        pointHighlightStroke: "rgba(217,87,217,1)"
    },
    {
        fillColor: "rgba(212,148,85,0.2)",
        strokeColor: "rgba(212,148,85,1)",
        pointColor: "rgba(212,148,85,1)",
        pointStrokeColor: "#fff",
        pointHighlightFill: "#fff",
        pointHighlightStroke: "rgba(212,148,85,1)"
    },
    {
        fillColor: "rgba(148,212,85,0.2)",
        strokeColor: "rgba(148,212,85,1)",
        pointColor: "rgba(148,212,85,1)",
        pointStrokeColor: "#fff",
        pointHighlightFill: "#fff",
        pointHighlightStroke: "rgba(148,212,85,1)"
    },
    {
        fillColor: "rgba(85,212,148,0.2)",
        strokeColor: "rgba(85,212,148,1)",
        pointColor: "rgba(85,212,148,1)",
        pointStrokeColor: "#fff",
        pointHighlightFill: "#fff",
        pointHighlightStroke: "rgba(85,212,148,1)"
    },
    {
        fillColor: "rgba(85,148,212,0.2)",
        strokeColor: "rgba(85,148,212,1)",
        pointColor: "rgba(85,148,212,1)",
        pointStrokeColor: "#fff",
        pointHighlightFill: "#fff",
        pointHighlightStroke: "rgba(85,148,212,1)"
    },
    {
        fillColor: "rgba(148,85,212,0.2)",
        strokeColor: "rgba(148,85,212,1)",
        pointColor: "rgba(148,85,212,1)",
        pointStrokeColor: "#fff",
        pointHighlightFill: "#fff",
        pointHighlightStroke: "rgba(148,85,212,1)"
    },
    {
        fillColor: "rgba(212,85,148,0.2)",
        strokeColor: "rgba(212,85,148,1)",
        pointColor: "rgba(212,85,148,1)",
        pointStrokeColor: "#fff",
        pointHighlightFill: "#fff",
        pointHighlightStroke: "rgba(212,85,148,1)"
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
            $(elem).mouseover(function() {
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
        });
        legend.mouseout(function() {
            chart.draw();
        });
    };
