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
import com.manydesigns.portofino.chart.ChartGenerator;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.model.datamodel.Database;
import com.manydesigns.portofino.model.pages.ChartPage;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.util.UrlBuilder;
import org.jfree.chart.JFreeChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                    new JFreeChartInstance(chart, file, chartId, "alt", //TODO
                                           width, height, portletUrl);
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

    public void generateChart() {
        ChartGenerator chartGenerator = null;

        if(chartPage.getGeneratorClass() == null) {
            throw new IllegalStateException("Invalid chart type: " + chartPage.getType());
        }
        try {
            chartGenerator = chartPage.getGeneratorClass().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Invalid generator for chart", e);
        }

        chartGenerator.setAntiAlias(antiAlias);
        chartGenerator.setBorderVisible(borderVisible);
        chartGenerator.setHeight(height);
        chartGenerator.setWidth(width);
        chart = chartGenerator.generate(chartPage, application);
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
        prepareConfigurationForms();

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
