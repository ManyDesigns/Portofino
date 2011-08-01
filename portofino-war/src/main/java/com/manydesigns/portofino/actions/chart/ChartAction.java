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

package com.manydesigns.portofino.actions.chart;

import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.jfreechart.JFreeChartInstance;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.util.RandomUtil;
import com.manydesigns.portofino.actions.PortletAction;
import com.manydesigns.portofino.actions.PortofinoAction;
import com.manydesigns.portofino.annotations.InjectApplication;
import com.manydesigns.portofino.annotations.InjectSiteNodeInstance;
import com.manydesigns.portofino.context.Application;
import com.manydesigns.portofino.dispatcher.SiteNodeInstance;
import com.manydesigns.portofino.model.site.ChartNode;
import com.manydesigns.portofino.util.DesaturatedDrawingSupplier;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.util.UrlBuilder;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.chart.urls.PieURLGenerator;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.VerticalAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@UrlBinding("/chart.action")
public class ChartAction extends PortletAction {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************

    public static final String CHART_FILENAME_FORMAT = "chart-{0}.png";

    //**************************************************************************
    // Injections
    //**************************************************************************

    @InjectApplication
    public Application application;

    @InjectSiteNodeInstance
    public SiteNodeInstance siteNodeInstance;

    //**************************************************************************
    // Web parameters
    //**************************************************************************

    public String cancelReturnUrl;
    public String chartId;

    public int width = 400;
    public int height = 300;
    public boolean antiAlias = true;
    public boolean borderVisible = true;

    //**************************************************************************
    // Model metadata
    //**************************************************************************

    public ChartNode chartNode;

    //**************************************************************************
    // Presentation elements
    //**************************************************************************

    public Form form;
    public Form displayForm;
    public JFreeChart chart;
    public JFreeChartInstance jfreeChartInstance;
    public String fileName;
    public InputStream inputStream;

    //**************************************************************************
    // Other objects
    //**************************************************************************
    private final Font titleFont = new Font("SansSerif", Font.BOLD, 12);
    private final Font legendFont = new Font("SansSerif", Font.BOLD, 10);
    private final Font legendItemFont = new Font("SansSerif", Font.PLAIN, 10);
    private final Color transparentColor = new Color(0, true);

    public static final Logger logger =
            LoggerFactory.getLogger(ChartAction.class);

    //**************************************************************************
    // Action default execute method
    //**************************************************************************

    @DefaultHandler
    public Resolution execute() {
        chartNode = (ChartNode) siteNodeInstance.getSiteNode();

        // Run/generate the chart
        generateChart();

        chartId = RandomUtil.createRandomCode();

        String actionurl = dispatch.getAbsoluteOriginalPath();
        UrlBuilder chartResolution =
                new UrlBuilder(actionurl, false)
                        .addParameter("chartId", chartId)
                        .addParameter("chart", "");
        String portletUrl = chartResolution.toString();

        try {
            File file = RandomUtil.getTempCodeFile(CHART_FILENAME_FORMAT, chartId);
            fileName = file.getName();

            jfreeChartInstance =
                    new JFreeChartInstance(chart, file, width, height, portletUrl);
        } catch (java.io.IOException e) {
            logger.warn("Could not save portlet", e);
            SessionMessages.addErrorMessage(e.getMessage());
        }

        if (isEmbedded()) {
            return new ForwardResolution("/layouts/chart/chart.jsp");
        } else {
            setupReturnToParentTarget();
            getPortlets().add("/layouts/chart/chart.jsp");
            return new ForwardResolution("/layouts/portlet-page.jsp");
        }
    }

    private void generateChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        java.util.List<Object[]> result;
        String query = chartNode.getQuery();
        try {
            result = application.runSql(chartNode.getDatabase(), query);
            for (Object[] current : result) {
                dataset.setValue((Comparable)current[0], (Number)current[1]);
            }
        } catch (Throwable e) {
            logger.warn("Could not run portlet query: " + query, e);
            while (e != null) {
                SessionMessages.addErrorMessage(e.getMessage());
                e = e.getCause();
            }
        }

        chart = ChartFactory.createPieChart(
                chartNode.getTitle(), dataset, true, true, true);

        chart.setAntiAlias(antiAlias);

        // impostiamo il bordo invisibile
        // eventualmente è il css a fornirne uno
        chart.setBorderVisible(borderVisible);

        // impostiamo il titolo
        TextTitle title = chart.getTitle();
        title.setFont(titleFont);
        title.setMargin(10.0, 0.0, 0.0, 0.0);

        // ottieni il Plot
        PiePlot plot = (PiePlot) chart.getPlot();

        PieURLGenerator urlGenerator =
                new ChartPieUrlGenerator(chartNode.getUrlExpression());
        plot.setURLGenerator(urlGenerator);


        // il plot ha sfondo e bordo trasparente
        // (quindi si vede il colore del chart)
        plot.setBackgroundPaint(transparentColor);
        plot.setOutlinePaint(transparentColor);

        //Modifico il toolTip
//        plot.setToolTipGenerator(new StandardPieToolTipGenerator("{0} = {1} ({2})"));

        // imposta la distanza delle etichette dal plot
        plot.setLabelGap(0.03);
//        plot.setLabelGenerator(new MyPieSectionLabelGenerator());

        // imposta il messaggio se non ci sono dati
        plot.setNoDataMessage("TODO i18n: No_data_available");

        plot.setCircular(true);

        plot.setBaseSectionOutlinePaint(Color.BLACK);

        DrawingSupplier supplier =
                new DesaturatedDrawingSupplier(plot.getDrawingSupplier());
        plot.setDrawingSupplier(supplier);

        // impostiamo il titolo della legenda
        String legendString = chartNode.getLegend();
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
    }

    public Resolution chart() throws FileNotFoundException {
        File file = RandomUtil.getTempCodeFile(CHART_FILENAME_FORMAT, chartId);

        inputStream = new FileInputStream(file);
        return new StreamingResolution("image/png", inputStream);
    }


    public void design() {
        /*
        form = new FormBuilder(PortletNode.class)
                .configFields("name", "title", "legend", "database",
                        "query", "urlExpression")
                .configMode(Mode.VIEW)
                .build();
        form.readFromObject(portlet);

        displayForm = new FormBuilder(PortletDesignAction.class)
                .configFields("width", "height", "antiAlias", "borderVisible")
                .configMode(Mode.EDIT)
                .build();
        displayForm.readFromObject(this);
        */
    }

    public Resolution returnToParent() {
        SiteNodeInstance[] siteNodeInstancePath =
                dispatch.getSiteNodeInstancePath();
        int previousPos = siteNodeInstancePath.length - 2;
        RedirectResolution resolution;
        if (previousPos >= 0) {
            SiteNodeInstance previousNode = siteNodeInstancePath[previousPos];
            String url = dispatch.getPathUrl(previousPos + 1);
            return new RedirectResolution(url, true);
        } else {
            throw new Error("No parent for root node");
        }
    }



    //**************************************************************************
    // Cancel
    //**************************************************************************

    public String cancel() {
        return PortofinoAction.CANCEL;
    }

    //**************************************************************************
    // Getter/setter
    //**************************************************************************


    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public SiteNodeInstance getSiteNodeInstance() {
        return siteNodeInstance;
    }

    public void setSiteNodeInstance(SiteNodeInstance siteNodeInstance) {
        this.siteNodeInstance = siteNodeInstance;
    }

    public String getCancelReturnUrl() {
        return cancelReturnUrl;
    }

    public void setCancelReturnUrl(String cancelReturnUrl) {
        this.cancelReturnUrl = cancelReturnUrl;
    }

    public String getChartId() {
        return chartId;
    }

    public void setChartId(String chartId) {
        this.chartId = chartId;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isAntiAlias() {
        return antiAlias;
    }

    public void setAntiAlias(boolean antiAlias) {
        this.antiAlias = antiAlias;
    }

    public boolean isBorderVisible() {
        return borderVisible;
    }

    public void setBorderVisible(boolean borderVisible) {
        this.borderVisible = borderVisible;
    }

    public ChartNode getChartNode() {
        return chartNode;
    }

    public void setChartNode(ChartNode chartNode) {
        this.chartNode = chartNode;
    }

    public Form getForm() {
        return form;
    }

    public void setForm(Form form) {
        this.form = form;
    }

    public Form getDisplayForm() {
        return displayForm;
    }

    public void setDisplayForm(Form displayForm) {
        this.displayForm = displayForm;
    }

    public JFreeChart getChart() {
        return chart;
    }

    public void setChart(JFreeChart chart) {
        this.chart = chart;
    }

    public JFreeChartInstance getJfreeChartInstance() {
        return jfreeChartInstance;
    }

    public void setJfreeChartInstance(JFreeChartInstance jfreeChartInstance) {
        this.jfreeChartInstance = jfreeChartInstance;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

}
