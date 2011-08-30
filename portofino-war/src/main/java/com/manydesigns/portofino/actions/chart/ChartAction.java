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
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.jfreechart.JFreeChartInstance;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.elements.util.RandomUtil;
import com.manydesigns.portofino.actions.PortletAction;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.model.datamodel.Database;
import com.manydesigns.portofino.model.pages.ChartPage;
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
    // Web parameters
    //**************************************************************************

    public String chartId;

    public int width = 400;
    public int height = 300;
    public boolean antiAlias = true;
    public boolean borderVisible = true;

    //**************************************************************************
    // Model metadata
    //**************************************************************************

    public ChartPage chartPage;

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


    @Before
    @Override
    public void prepare() {
        super.prepare();
        chartPage = (ChartPage) pageInstance.getPage();
    }

    //**************************************************************************
    // Action default execute method
    //**************************************************************************

    @DefaultHandler
    public Resolution execute() {

        try {
            // Run/generate the chart
            generateChart();

            chartId = RandomUtil.createRandomId();

            String actionurl = dispatch.getAbsoluteOriginalPath();
            UrlBuilder chartResolution =
                    new UrlBuilder(actionurl, false)
                            .addParameter("chartId", chartId)
                            .addParameter("chart", "");
            String portletUrl = chartResolution.toString();

            File file = RandomUtil.getTempCodeFile(CHART_FILENAME_FORMAT, chartId);
            fileName = file.getName();

            jfreeChartInstance =
                    new JFreeChartInstance(chart, file, width, height, portletUrl);
        } catch (Throwable e) {
            logger.error("Portlet exception", e);
            return portletError(e);
        }

        if (isEmbedded()) {
            return new ForwardResolution("/layouts/chart/chart.jsp");
        } else {
            setupReturnToParentTarget();

            return forwardToPortletPage("/layouts/chart/chart.jsp");
        }
    }

    private void generateChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        java.util.List<Object[]> result;
        String query = chartPage.getQuery();
        result = application.runSql(chartPage.getDatabase(), query);
        for (Object[] current : result) {
            dataset.setValue((Comparable)current[0], (Number)current[1]);
        }

        chart = ChartFactory.createPieChart(
                chartPage.getName(), dataset, true, true, true);

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
                new ChartPieUrlGenerator(chartPage.getUrlExpression());
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
        String legendString = chartPage.getLegend();
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


    public Resolution returnToParent() {
        PageInstance[] pageInstancePath =
                dispatch.getPageInstancePath();
        int previousPos = pageInstancePath.length - 2;
        RedirectResolution resolution;
        if (previousPos >= 0) {
            PageInstance previousPage = pageInstancePath[previousPos];
            String url = dispatch.getPathUrl(previousPos + 1);
            return new RedirectResolution(url, true);
        } else {
            throw new Error("No parent for root page");
        }
    }



    //**************************************************************************
    // Configuration
    //**************************************************************************

    public static final String[][] CONFIGURATION_FIELDS =
            {{"name", "legend", "database", "query", "urlExpression"}};

    public Resolution configure() {
        setupPageConfiguration();

        return new ForwardResolution("/layouts/chart/configure.jsp");
    }

    @Override
    protected void prepareConfigurationForms() {
        super.prepareConfigurationForms();
        SelectionProvider databaseSelectionProvider =
                DefaultSelectionProvider.create("database",
                        model.getDatabases(),
                        Database.class,
                        null,
                        "databaseName");
        form = new FormBuilder(ChartPage.class)
                .configFields(CONFIGURATION_FIELDS)
                .configFieldSetNames("Chart")
                .configSelectionProvider(databaseSelectionProvider, "database")
                .build();
        form.readFromObject(chartPage);
    }

    public Resolution updateConfiguration() {
        synchronized (application) {
            prepareConfigurationForms();
            form.readFromObject(chartPage);
            form.readFromRequest(context.getRequest());
            readPageConfigurationFromRequest();
            boolean valid = form.validate();
            valid = validatePageConfiguration() && valid;
            if (valid) {
                updatePageConfiguration();
                form.writeToObject(chartPage);
                saveModel();
                SessionMessages.addInfoMessage("Configuration updated successfully");
                return cancel();
            } else {
                return new ForwardResolution("/layouts/chart/configure.jsp");
            }
        }
    }

    //**************************************************************************
    // Getter/setter
    //**************************************************************************


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

    public ChartPage getChartPage() {
        return chartPage;
    }

    public void setChartPage(ChartPage chartPage) {
        this.chartPage = chartPage;
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
