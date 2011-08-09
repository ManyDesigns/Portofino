/*
 * Copyright (C) 2005-2011 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * There are special exceptions to the terms and conditions of the GPL
 * as it is applied to this software. View the full text of the
 * exception in file OPEN-SOURCE-LICENSE.txt in the directory of this
 * software distribution.
 *
 * This program is distributed WITHOUT ANY WARRANTY; and without the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see http://www.gnu.org/licenses/gpl.txt
 * or write to:
 * Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307  USA
 *
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
            "Copyright (c) 2005-2011, ManyDesigns srl";
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
