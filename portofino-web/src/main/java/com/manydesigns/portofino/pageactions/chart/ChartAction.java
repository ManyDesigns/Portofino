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

package com.manydesigns.portofino.pageactions.chart;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.fields.Field;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.jfreechart.JFreeChartInstance;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.elements.util.RandomUtil;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.chart.ChartGenerator;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.model.database.Database;
import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.pageactions.PageActionName;
import com.manydesigns.portofino.pageactions.annotations.ConfigurationClass;
import com.manydesigns.portofino.pageactions.annotations.ScriptTemplate;
import com.manydesigns.portofino.pageactions.chart.configuration.ChartConfiguration;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresPermissions;
import com.manydesigns.portofino.stripes.NoCacheStreamingResolution;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.util.UrlBuilder;
import org.jfree.chart.JFreeChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@UrlBinding("/actions/chart")
@RequiresPermissions(level = AccessLevel.VIEW)
@ScriptTemplate("script_template.txt")
@ConfigurationClass(ChartConfiguration.class)
@PageActionName("Chart")
public class ChartAction extends AbstractPageAction {
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

    public ChartConfiguration chartConfiguration;

    //**************************************************************************
    // Presentation elements
    //**************************************************************************

    public Form form;
    public Form displayForm;
    public JFreeChart chart;
    public JFreeChartInstance jfreeChartInstance;
    public File file;
    public InputStream inputStream;

    public static final Logger logger =
            LoggerFactory.getLogger(ChartAction.class);

    //**************************************************************************
    // Action default execute method
    //**************************************************************************

    @DefaultHandler
    public Resolution execute() {
        if(chartConfiguration == null) {
            return forwardToPortletNotConfigured();
        }

        try {
            // Run/generate the chart
            try {
                Thread.currentThread().setContextClassLoader(Class.class.getClassLoader());
                generateChart();
            } finally {
                Thread.currentThread().setContextClassLoader(ChartAction.class.getClassLoader());
            }

            chartId = RandomUtil.createRandomId();

            String actionurl = dispatch.getAbsoluteOriginalPath();
            UrlBuilder chartResolution =
                    new UrlBuilder(actionurl, false)
                            .addParameter("chartId", chartId)
                            .addParameter("chart", "");
            String portletUrl = chartResolution.toString();

            file = RandomUtil.getTempCodeFile(CHART_FILENAME_FORMAT, chartId);

            jfreeChartInstance =
                    new JFreeChartInstance(chart, file, chartId, "alt", //TODO
                                           width, height, portletUrl);
        } catch (Throwable e) {
            logger.error("Portlet exception", e);
            return forwardToPortletError(e);
        }

        if (isEmbedded()) {
            return new ForwardResolution("/layouts/chart/chart.jsp");
        } else {
            setupReturnToParentTarget();
            return forwardToPortletPage("/layouts/chart/chart.jsp");
        }
    }

    public void generateChart() {
        ChartGenerator chartGenerator;

        if(chartConfiguration.getGeneratorClass() == null) {
            throw new IllegalStateException("Invalid chart type: " + chartConfiguration.getActualType());
        }
        try {
            chartGenerator = chartConfiguration.getGeneratorClass().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Invalid generator for chart", e);
        }

        chartGenerator.setAntiAlias(antiAlias);
        chartGenerator.setBorderVisible(borderVisible);
        chartGenerator.setHeight(height);
        chartGenerator.setWidth(width);
        chart = chartGenerator.generate(chartConfiguration, application);
    }

    public Resolution chart() throws FileNotFoundException {
        final File file = RandomUtil.getTempCodeFile(CHART_FILENAME_FORMAT, chartId);

        inputStream = new FileInputStream(file);
        return new NoCacheStreamingResolution("image/png", inputStream) {
            @Override
            protected void stream(HttpServletResponse response) throws Exception {
                super.stream(response);
                //file.delete(); //TODO delete (temporizzato?) + dicitura chart scaduto, ricaricare
            }
        };
    }


    public Resolution returnToParent() {
        PageInstance[] pageInstancePath =
                dispatch.getPageInstancePath();
        int previousPos = pageInstancePath.length - 2;
        RedirectResolution resolution;
        if (previousPos >= 0) {
            PageInstance previousPage = pageInstancePath[previousPos];
            String url = previousPage.getPath();
            return new RedirectResolution(url, true);
        } else {
            throw new Error("No parent for root page");
        }
    }



    //**************************************************************************
    // Configuration
    //**************************************************************************

    public static final String[][] CONFIGURATION_FIELDS =
            {{"name", "type", "orientation", "legend", "database", "query", "urlExpression"}};

    public static final String[] chartTypes1D = {
        ChartConfiguration.Type.PIE.name(),
        ChartConfiguration.Type.PIE3D.name(),
        ChartConfiguration.Type.RING.name()
    };

    public static final String[] chartTypes2D = {
        ChartConfiguration.Type.AREA.name(),
        ChartConfiguration.Type.BAR.name(),
        ChartConfiguration.Type.BAR3D.name(),
        ChartConfiguration.Type.LINE.name(),
        ChartConfiguration.Type.LINE3D.name(),
        ChartConfiguration.Type.STACKED_BAR.name(),
        ChartConfiguration.Type.STACKED_BAR_3D.name()
    };

    public static final String[] chartTypeValues =
            new String[chartTypes1D.length + chartTypes2D.length + 2];
    public static final String[] chartTypeLabels =
            new String[chartTypeValues.length];

    static {
        Properties props = new Properties();
        String prefix = "com.manydesigns.portofino.chart.type.";
        try {
            InputStream is = ChartAction.class.getResourceAsStream("chart-types.properties");
            props.load(is);
        } catch (Exception e) {
            logger.error("Couldn't load chart type labels", e);
        }
        chartTypeValues[0] = "--1D";
        chartTypeLabels[0] = "-- 1D charts --";
        for(int i = 0; i < chartTypes1D.length; i++) {
            chartTypeValues[i + 1] = chartTypes1D[i];
            chartTypeLabels[i + 1] = props.getProperty(prefix + chartTypes1D[i], chartTypes1D[i]);
        }
        chartTypeValues[chartTypes1D.length + 1] = "--2D";
        chartTypeLabels[chartTypes1D.length + 1] = "-- 2D charts --";
        for(int i = 0; i < chartTypes2D.length; i++) {
            chartTypeValues[i + 2 + chartTypes1D.length] = chartTypes2D[i];
            chartTypeLabels[i + 2 + chartTypes1D.length] =
                    props.getProperty(prefix + chartTypes2D[i], chartTypes2D[i]);
        }
    }

    @Button(list = "portletHeaderButtons", key = "commons.configure", order = 1, icon = "ui-icon-wrench")
    @RequiresPermissions(level = AccessLevel.EDIT)
    public Resolution configure() {
        prepareConfigurationForms();
        return new ForwardResolution("/layouts/chart/configure.jsp");
    }

    @Override
    protected void prepareConfigurationForms() {
        super.prepareConfigurationForms();
        SelectionProvider databaseSelectionProvider =
                createSelectionProvider("database",
                        model.getDatabases(),
                        Database.class,
                        null,
                        new String[] { "databaseName" });
        DefaultSelectionProvider typeSelectionProvider = new DefaultSelectionProvider("type");
        for(int i = 0; i < chartTypeValues.length; i++) {
            typeSelectionProvider.appendRow(chartTypeValues[i], chartTypeLabels[i], true);
        }
        String[] orientationValues =
                { ChartConfiguration.Orientation.HORIZONTAL.name(), ChartConfiguration.Orientation.VERTICAL.name() };
        String[] orientationLabels = { "Horizontal", "Vertical" };
        DefaultSelectionProvider orientationSelectionProvider = new DefaultSelectionProvider("orientation");
        for(int i = 0; i < orientationValues.length; i++) {
            orientationSelectionProvider.appendRow(orientationValues[i], orientationLabels[i], true);
        }
        form = new FormBuilder(ChartConfiguration.class)
                .configFields(CONFIGURATION_FIELDS)
                .configFieldSetNames("Chart")
                .configSelectionProvider(typeSelectionProvider, "type")
                .configSelectionProvider(orientationSelectionProvider, "orientation")
                .configSelectionProvider(databaseSelectionProvider, "database")
                .build();
        form.readFromObject(chartConfiguration);
    }

    @Button(list = "configuration", key = "commons.updateConfiguration")
    @RequiresPermissions(level = AccessLevel.EDIT)
    public Resolution updateConfiguration() {
        synchronized (application) {
            prepareConfigurationForms();
            form.readFromObject(chartConfiguration);
            form.readFromRequest(context.getRequest());
            readPageConfigurationFromRequest();
            boolean valid = form.validate();
            valid = validatePageConfiguration() && valid;
            Field typeField = form.findFieldByPropertyName("type");
            String typeValue = typeField.getStringValue();
            boolean placeHolderValue =
                    typeValue != null && typeValue.startsWith("--");
            if(placeHolderValue) {
                valid = false;
                String errorMessage =
                        ElementsThreadLocals.getTextProvider().getText("elements.error.field.required");
                typeField.getErrors().add(errorMessage);
                SessionMessages.addErrorMessage("");
            }
            if (valid) {
                updatePageConfiguration();
                if(chartConfiguration == null) {
                    chartConfiguration = new ChartConfiguration();
                }
                form.writeToObject(chartConfiguration);
                saveConfiguration(chartConfiguration);

                SessionMessages.addInfoMessage(getMessage("commons.configuration.updated"));
                return cancel();
            } else {
                return new ForwardResolution("/layouts/chart/configure.jsp");
            }
        }
    }

    public Resolution prepare(PageInstance pageInstance, ActionBeanContext context) {
        this.pageInstance = pageInstance;
        if(!pageInstance.getParameters().isEmpty()) {
            return new ErrorResolution(404);
        }
        chartConfiguration = (ChartConfiguration) pageInstance.getConfiguration();
        return null;
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

    public ChartConfiguration getChartConfiguration() {
        return chartConfiguration;
    }

    public void setChartConfiguration(ChartConfiguration chartConfiguration) {
        this.chartConfiguration = chartConfiguration;
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

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

}
