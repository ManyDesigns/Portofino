/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.manydesigns.elements.jfreechart;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.util.RandomUtil;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.File;
import java.io.IOException;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class ChartGenerator {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    public static final String CHART_FILENAME_FORMAT = "chart-{0}.png";

    public static String generateChart(int chartType, String title,
                                       String axisLabel, String valueLabel, Dataset dataset, ChartRenderingInfo info,
                                       int width, int height) {
        JFreeChart chart;

        switch (chartType) {
            case ChartUtil.PIE_CHART:
                chart = ChartFactory.createPieChart(title, (PieDataset) dataset,
                        true, true, true);

                break;
            case ChartUtil.PIE_CHART_3D:
                chart = ChartFactory.createPieChart3D(title, (PieDataset) dataset,
                        true, true, true);

                break;
            case ChartUtil.VERTICAL_BAR_CHART:
                chart = ChartFactory.createBarChart(title, axisLabel, valueLabel,
                        (CategoryDataset) dataset,
                        PlotOrientation.VERTICAL, true, true, true);
                break;
            case ChartUtil.VERTICAL_BAR_CHART_3D:
                chart = ChartFactory.createBarChart3D(title, axisLabel, valueLabel,
                        (CategoryDataset) dataset,
                        PlotOrientation.VERTICAL, true, true, true);
                break;
            case ChartUtil.HORIZONTAL_BAR_CHART:
                chart = ChartFactory.createBarChart(title, axisLabel, valueLabel,
                        (CategoryDataset) dataset,
                        PlotOrientation.HORIZONTAL, true, true, true);
                break;
            case ChartUtil.HORIZONTAL_BAR_CHART_3D:
                chart = ChartFactory.createBarChart3D(title, axisLabel, valueLabel,
                        (CategoryDataset) dataset,
                        PlotOrientation.HORIZONTAL, true, true, true);
                break;
            case ChartUtil.XY_CHART:
                chart = ChartFactory.createXYLineChart(title, axisLabel, valueLabel,
                        (XYSeriesCollection) dataset,
                        PlotOrientation.HORIZONTAL, true, true, true);
                break;
            case ChartUtil.RING_CHART:
                chart = ChartFactory.createRingChart(title, (PieDataset) dataset,
                        true, true, true);
                break;
            case ChartUtil.GANTT_CHART:
                    chart = ChartFactory.createGanttChart(title, axisLabel, valueLabel,
                        (IntervalCategoryDataset) dataset,
                         true, true, true);
                    break;
            default:
                throw new InternalError(ElementsThreadLocals.getText(
                        "Portlet_report_type_not_recognized"));
        }
        String code = RandomUtil.createRandomId();
        try {
            File tempFile = RandomUtil.getTempCodeFile(CHART_FILENAME_FORMAT, code);
            ChartUtilities.saveChartAsPNG(tempFile, chart, width, height, info);
        } catch (IOException e) {
            throw new InternalError("Cannot store images");
        }

        return code;
    }
}
