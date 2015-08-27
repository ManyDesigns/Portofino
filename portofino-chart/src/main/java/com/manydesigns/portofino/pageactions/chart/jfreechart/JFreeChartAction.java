/*
 * Copyright (C) 2005-2015 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.pageactions.chart.jfreechart;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.fields.Field;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.elements.util.RandomUtil;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.chart.ChartGenerator;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.logic.SelectionProviderLogic;
import com.manydesigns.portofino.model.database.Database;
import com.manydesigns.portofino.modules.DatabaseModule;
import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.pageactions.PageActionName;
import com.manydesigns.portofino.pageactions.annotations.ConfigurationClass;
import com.manydesigns.portofino.pageactions.annotations.ScriptTemplate;
import com.manydesigns.portofino.pageactions.chart.jfreechart.configuration.JFreeChartConfiguration;
import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresPermissions;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.util.UrlBuilder;
import org.jfree.chart.JFreeChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
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
@RequiresPermissions(level = AccessLevel.VIEW)
@ScriptTemplate("script_template.groovy")
@ConfigurationClass(JFreeChartConfiguration.class)
@PageActionName("Chart (with JFreeChart)")
public class JFreeChartAction extends AbstractPageAction {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************

    public static final String CHART_FILENAME_FORMAT = "chart-{0}.png";

    //**************************************************************************
    // Injections
    //**************************************************************************

    @Inject(DatabaseModule.PERSISTENCE)
    public Persistence persistence;

    //**************************************************************************
    // Web parameters
    //**************************************************************************

    public String chartId;

    public int width = 470;
    public int height = 354;
    public boolean antiAlias = true;
    public boolean borderVisible = true;

    //**************************************************************************
    // Model metadata
    //**************************************************************************

    public JFreeChartConfiguration chartConfiguration;

    //**************************************************************************
    // Presentation elements
    //**************************************************************************

    public Form form;
    public JFreeChart chart;
    public JFreeChartInstance jfreeChartInstance;
    public File file;

    public static final Logger logger =
            LoggerFactory.getLogger(JFreeChartAction.class);

    //**************************************************************************
    // Action default execute method
    //**************************************************************************

    @DefaultHandler
    public Resolution execute() {
        if(chartConfiguration == null) {
            return forwardToPageActionNotConfigured();
        }

        try {
            // Run/generate the chart
            try {
                Thread.currentThread().setContextClassLoader(Class.class.getClassLoader());
                generateChart();
            } finally {
                Thread.currentThread().setContextClassLoader(JFreeChartAction.class.getClassLoader());
            }

            chartId = RandomUtil.createRandomId();

            String actionurl = context.getActionPath();
            UrlBuilder chartResolution =
                    new UrlBuilder(context.getLocale(), actionurl, false)
                            .addParameter("chartId", chartId)
                            .addParameter("chart", "");
            String url = context.getRequest().getContextPath() + chartResolution.toString();

            file = RandomUtil.getTempCodeFile(CHART_FILENAME_FORMAT, chartId);

            jfreeChartInstance =
                    new JFreeChartInstance(chart, file, chartId, "Chart: " + chartConfiguration.getName(),
                                           width, height, url);
        } catch (Throwable e) {
            logger.error("Chart exception", e);
            return forwardToPageActionError(e);
        }

        return new ForwardResolution("/m/chart/jfreechart/display.jsp");
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
        chart = chartGenerator.generate(chartConfiguration, persistence, context.getLocale());
    }

    public Resolution chart() throws FileNotFoundException {
        final File file = RandomUtil.getTempCodeFile(CHART_FILENAME_FORMAT, chartId);
        if(!file.exists()) {
            return new ErrorResolution(404);
        }
        final InputStream inputStream = new FileInputStream(file);

        //Cache the file, expire after 12h
        int expiresAfter = 12 * 60 * 60 * 1000;
        long now = System.currentTimeMillis();
        HttpServletResponse response = context.getResponse();
        response.setHeader("Cache-Control", "max-age=" + expiresAfter);
        response.setDateHeader("Last-Modified", now);
        response.setDateHeader("Expires", now + expiresAfter);
        response.setHeader("Pragma", "");

        return new StreamingResolution("image/png", inputStream) {
            @Override
            protected void stream(HttpServletResponse response) throws Exception {
                super.stream(response);
                if(!file.delete()) {
                    logger.warn("Could not delete temporary file for chart: " + file.getAbsolutePath());
                }
            }
        };
    }

    //**************************************************************************
    // Configuration
    //**************************************************************************

    public static final String[][] CONFIGURATION_FIELDS =
            {{"name", "type", "orientation", "legend", "database", "query", "urlExpression"}};

    public static final String[] chartTypes1D = {
        JFreeChartConfiguration.Type.PIE.name(),
        JFreeChartConfiguration.Type.PIE3D.name(),
        JFreeChartConfiguration.Type.RING.name()
    };

    public static final String[] chartTypes2D = {
        JFreeChartConfiguration.Type.AREA.name(),
        JFreeChartConfiguration.Type.BAR.name(),
        JFreeChartConfiguration.Type.BAR3D.name(),
        JFreeChartConfiguration.Type.LINE.name(),
        JFreeChartConfiguration.Type.LINE3D.name(),
        JFreeChartConfiguration.Type.STACKED_BAR.name(),
        JFreeChartConfiguration.Type.STACKED_BAR_3D.name()
    };

    public final String[] chartTypeValues = new String[chartTypes1D.length + chartTypes2D.length + 2];
    public final String[] chartTypeLabels = new String[chartTypeValues.length];

    @Button(list = "pageHeaderButtons", titleKey = "configure", order = 1, icon = Button.ICON_WRENCH)
    @RequiresPermissions(level = AccessLevel.DEVELOP)
    public Resolution configure() {
        prepareConfigurationForms();
        return new ForwardResolution("/m/chart/jfreechart/configure.jsp");
    }

    @Override
    protected void prepareConfigurationForms() {
        super.prepareConfigurationForms();

        String prefix = "com.manydesigns.portofino.jfreechart.type.";
        chartTypeValues[0] = "--1D";
        chartTypeLabels[0] = "-- 1D charts --";
        for(int i = 0; i < chartTypes1D.length; i++) {
            chartTypeValues[i + 1] = chartTypes1D[i];
            chartTypeLabels[i + 1] = ElementsThreadLocals.getText(prefix + chartTypes1D[i], chartTypes1D[i]);
        }
        chartTypeValues[chartTypes1D.length + 1] = "--2D";
        chartTypeLabels[chartTypes1D.length + 1] = "-- 2D charts --";
        for(int i = 0; i < chartTypes2D.length; i++) {
            chartTypeValues[i + 2 + chartTypes1D.length] = chartTypes2D[i];
            chartTypeLabels[i + 2 + chartTypes1D.length] =
                    ElementsThreadLocals.getText(prefix + chartTypes2D[i], chartTypes2D[i]);
        }

        SelectionProvider databaseSelectionProvider =
                SelectionProviderLogic.createSelectionProvider("database",
                        persistence.getModel().getDatabases(),
                        Database.class,
                        null,
                        new String[]{"databaseName"});
        DefaultSelectionProvider typeSelectionProvider = new DefaultSelectionProvider("type");
        for(int i = 0; i < chartTypeValues.length; i++) {
            typeSelectionProvider.appendRow(chartTypeValues[i], chartTypeLabels[i], true);
        }
        String[] orientationValues =
                { JFreeChartConfiguration.Orientation.HORIZONTAL.name(), JFreeChartConfiguration.Orientation.VERTICAL.name() };
        String[] orientationLabels = { "Horizontal", "Vertical" };
        DefaultSelectionProvider orientationSelectionProvider = new DefaultSelectionProvider("orientation");
        for(int i = 0; i < orientationValues.length; i++) {
            orientationSelectionProvider.appendRow(orientationValues[i], orientationLabels[i], true);
        }
        form = new FormBuilder(JFreeChartConfiguration.class)
                .configFields(CONFIGURATION_FIELDS)
                .configFieldSetNames("Chart")
                .configSelectionProvider(typeSelectionProvider, "type")
                .configSelectionProvider(orientationSelectionProvider, "orientation")
                .configSelectionProvider(databaseSelectionProvider, "database")
                .build();
        form.readFromObject(chartConfiguration);
    }

    @Button(list = "configuration", key = "update.configuration", order = 1, type = Button.TYPE_PRIMARY)
    @RequiresPermissions(level = AccessLevel.DEVELOP)
    public Resolution updateConfiguration() {
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
                chartConfiguration = new JFreeChartConfiguration();
            }
            form.writeToObject(chartConfiguration);
            saveConfiguration(chartConfiguration);

            SessionMessages.addInfoMessage(ElementsThreadLocals.getText("configuration.updated.successfully"));
            return cancel();
        } else {
            return new ForwardResolution("/m/chart/jfreechart/configure.jsp");
        }
    }

    public Resolution preparePage() {
        if(!pageInstance.getParameters().isEmpty()) {
            return new ErrorResolution(404);
        }
        chartConfiguration = (JFreeChartConfiguration) pageInstance.getConfiguration();
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

    public JFreeChartConfiguration getChartConfiguration() {
        return chartConfiguration;
    }

    public void setChartConfiguration(JFreeChartConfiguration chartConfiguration) {
        this.chartConfiguration = chartConfiguration;
    }

    public Form getForm() {
        return form;
    }

    public void setForm(Form form) {
        this.form = form;
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

}
