/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.manydesigns.elements.jfreechart;

import org.jfree.data.general.Dataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.chart.ChartRenderingInfo;

import java.util.Hashtable;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class ChartBuilder {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";
    Integer height;
    Integer width;
    String alt;
    Integer chartType;
    String title;
    Dataset dataset;
    String axisLabel;
    String valueLabel;

    ChartRenderingInfo info;
    String servletName;




    public ChartBuilder() {

    }

    public JFreeChartElement build ()
    {
        JFreeChartElement chart;
        chart = new JFreeChartElement();
        if (height != null)
            chart.setHeight(height);
        if (width != null)
            chart.setWidth(width);
        if (alt != null)
            chart.setAlt(alt);
        if (chartType!= null)
            chart.setChartType(chartType);
        if (title!= null)
            chart.setTitle(title);
        if (dataset!= null)
            chart.setDataset(dataset);
        if (axisLabel!= null)
            chart.setAxisName(axisLabel);
        if (valueLabel!= null)
            chart.setValueName(valueLabel);

        if (info!= null)
            chart.setInfo(info);
        return chart;
    }

    public ChartBuilder configHeight(Integer height) {
        this.height=height;
        return this;
    }

    public ChartBuilder  configWidth(Integer width) {
        this.width=width;
        return this;
    }

    public ChartBuilder configAlt(String alt) {
        this.alt=alt;
        return this;
    }

    public ChartBuilder configChartType(Integer chartType) {
        this.chartType=chartType;
        return this;
    }

    public ChartBuilder  configTitle(String title) {
        this.title=title;
        return this;
    }

    public ChartBuilder configDataset(Dataset dataset) {
        this.dataset=dataset;
        return this;
    }

    public ChartBuilder  configInfo(ChartRenderingInfo info) {
        this.info=info;
        return this;
    }

    public ChartBuilder  configServletName(String servletName) {
        this.servletName=servletName;
        return this;
    }

    public ChartBuilder createPieDataset(Hashtable<String, Number> table){
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (String key : table.keySet()) {
            dataset.setValue(key, table.get(key));
        }
        configDataset(dataset);
        return this;
    }

    public ChartBuilder createCategoryDataset(String axisLabel, String valueLabel,
                    String[] series, String[] categories, Number[][] values){
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (int i=0; i<series.length; i++){
            for (int j=0; j<categories.length; j++){
                dataset.addValue(values[j][i], categories[j], series[i]);
            }
        }
        this.axisLabel= axisLabel;
        this.valueLabel=valueLabel;
        configDataset(dataset);
        return this;
    }

    public ChartBuilder createXYDataset(String axisLabel, String valueLabel,
                    Hashtable<Number, Number> xy){
        XYSeries series = new XYSeries("Data");
        for (Number key : xy.keySet()) {
            series.add(key, xy.get(key));
        }
        this.axisLabel= axisLabel;
        this.valueLabel=valueLabel;
        final XYSeriesCollection dataset = new XYSeriesCollection(series);

        configDataset(dataset);
        return this;
    }

    public ChartBuilder createGantDataset(String axisLabel, String valueLabel,
                    List<TaskSeries> series){
        TaskSeriesCollection dataset = new TaskSeriesCollection();

        for (TaskSeries a_series : series){
            dataset.add(a_series);
        }
        this.axisLabel= axisLabel;
        this.valueLabel=valueLabel;
        configDataset(dataset);
        return this;
    }
}
