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

package com.manydesigns.portofino.chart;

import com.manydesigns.portofino.actions.chart.configuration.ChartConfiguration;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.database.QueryUtils;
import com.manydesigns.portofino.util.DesaturatedDrawingSupplier;
import org.hibernate.Session;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.text.TextBlockAnchor;
import org.jfree.ui.*;

import java.awt.*;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public abstract class Chart2DGenerator extends AbstractChartGenerator {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //**************************************************************************
    // Other objects
    //**************************************************************************
    private final Font titleFont = new Font("SansSerif", Font.BOLD, 12);
    private final Font axisFont = new Font("SansSerif", Font.PLAIN, 12);
    private final Font legendFont = new Font("SansSerif", Font.BOLD, 10);
    private final Font legendItemFont = new Font("SansSerif", Font.PLAIN, 10);
    private final Color transparentColor = new Color(0, true);

    public JFreeChart generate(ChartConfiguration chartConfiguration, Application application) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        java.util.List<Object[]> result;
        String query = chartConfiguration.getQuery();
        Session session = application.getSession(chartConfiguration.getDatabase());
        result = QueryUtils.runSql(session, query);
        for (Object[] current : result) {
            ComparableWrapper x = new ComparableWrapper((Comparable)current[1]);
            ComparableWrapper y = new ComparableWrapper((Comparable)current[2]);
            if(current.length > 3) {
                x.setLabel(current[3].toString());
            }
            if(current.length > 4) {
                y.setLabel(current[4].toString());
            }
            dataset.setValue((Number)current[0], x, y);
        }

        PlotOrientation plotOrientation = PlotOrientation.HORIZONTAL;
        if (chartConfiguration.getActualOrientation() == ChartConfiguration.Orientation.VERTICAL) {
                plotOrientation = PlotOrientation.VERTICAL;
        }

        JFreeChart chart = createChart(chartConfiguration, dataset, plotOrientation);

        chart.setAntiAlias(antiAlias);

        // impostiamo il bordo invisibile
        // eventualmente è il css a fornirne uno
        chart.setBorderVisible(borderVisible);

        // impostiamo il titolo
        TextTitle title = chart.getTitle();
        title.setFont(titleFont);
        title.setMargin(10.0, 0.0, 0.0, 0.0);

        // ottieni il Plot
        CategoryPlot plot = (CategoryPlot) chart.getPlot();

        CategoryURLGenerator urlGenerator =
                new ChartBarUrlGenerator(chartConfiguration.getUrlExpression());
        CategoryItemRenderer renderer = plot.getRenderer();
    //        renderer.setItemURLGenerator(urlGenerator);
        renderer.setBaseItemURLGenerator(urlGenerator);
        renderer.setBaseOutlinePaint(Color.BLACK);

        /////////////////
        if (renderer instanceof BarRenderer) {
            BarRenderer barRenderer = (BarRenderer)renderer;

            barRenderer.setDrawBarOutline(true);
            barRenderer.setShadowVisible(false);
            barRenderer.setBarPainter(new StandardBarPainter());
        }
        /////////////////

        // il plot ha sfondo e bordo trasparente
        // (quindi si vede il colore del chart)
        plot.setBackgroundPaint(transparentColor);
        plot.setOutlinePaint(transparentColor);

        //Modifico il toolTip
//        plot.setToolTipGenerator(new StandardPieToolTipGenerator("{0} = {1} ({2})"));

        // imposta il messaggio se non ci sono dati
        plot.setNoDataMessage("TODO i18n: No_data_available");
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.GRAY);
        plot.setAxisOffset(new RectangleInsets(0,0,0,0));

        // Category axis
        CategoryAxis categoryAxis = plot.getDomainAxis();
        categoryAxis.setAxisLinePaint(Color.BLACK);
        categoryAxis.setLabelFont(axisFont);
        categoryAxis.setAxisLineVisible(true);

        // impostiamo la rotazione dell'etichetta
        if (plot.getOrientation() == PlotOrientation.VERTICAL) {
            CategoryLabelPosition pos =
                    new CategoryLabelPosition(RectangleAnchor.TOP_LEFT,
                            TextBlockAnchor.TOP_RIGHT,
                            TextAnchor.TOP_RIGHT, -Math.PI / 4.0,
                            CategoryLabelWidthType.CATEGORY, 100);
            CategoryLabelPositions positions =
                    new CategoryLabelPositions(pos, pos, pos, pos);
            categoryAxis.setCategoryLabelPositions(positions);
            categoryAxis.setMaximumCategoryLabelWidthRatio(6.0f);
            height = 333;
        } else {
            categoryAxis.setMaximumCategoryLabelWidthRatio(0.4f);

            // recuperiamo 8 pixel a sinistra
            plot.setInsets(new RectangleInsets(4.0, 0.0, 4.0, 8.0));

            height = 74;

            // contiamo gli elementi nel dataset
            height += 23 * dataset.getColumnCount();

            height += 57;
        }

        Axis rangeAxis = plot.getRangeAxis();
        rangeAxis.setAxisLinePaint(Color.BLACK);
        rangeAxis.setLabelFont(axisFont);

        DrawingSupplier supplier =
                new DesaturatedDrawingSupplier(plot.getDrawingSupplier());
        plot.setDrawingSupplier(supplier);

        // impostiamo il titolo della legenda
        String legendString = chartConfiguration.getLegend();
        Title subtitle = new TextTitle(legendString, legendFont, Color.BLACK,
                RectangleEdge.BOTTOM, HorizontalAlignment.CENTER,
                VerticalAlignment.CENTER, new RectangleInsets(0, 0, 0, 0));
        subtitle.setMargin(0, 0, 5, 0);
        chart.addSubtitle(subtitle);

        // impostiamo la legenda
        LegendTitle legend = chart.getLegend();
        legend.setBorder(0, 0, 0, 0);
        legend.setItemFont(legendItemFont);
        int legendMargin = 10;
        legend.setMargin(0.0, legendMargin, legendMargin, legendMargin);
        legend.setBackgroundPaint(transparentColor);

        // impostiamo un gradiente orizzontale
        Paint chartBgPaint = new GradientPaint(0, 0, new Color(255, 253, 240),
                0, height, Color.WHITE);
        chart.setBackgroundPaint(chartBgPaint);
        return chart;
    }

    protected abstract JFreeChart createChart
            (ChartConfiguration chartConfiguration, CategoryDataset dataset, PlotOrientation plotOrientation);


}
