/*
 * Copyright (C) 2005-2010 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.actions.model;

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.jfreechart.JBla;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.struts2.Struts2Utils;
import com.manydesigns.elements.util.RandomUtil;
import com.manydesigns.elements.util.Util;
import com.manydesigns.portofino.actions.PortofinoAction;
import com.manydesigns.portofino.annotations.InjectContext;
import com.manydesigns.portofino.annotations.InjectNavigation;
import com.manydesigns.portofino.context.Context;
import com.manydesigns.portofino.context.ModelObjectNotFoundError;
import com.manydesigns.portofino.model.site.PortletNode;
import com.manydesigns.portofino.navigation.Navigation;
import com.manydesigns.portofino.util.DesaturatedDrawingSupplier;
import com.opensymphony.xwork2.ActionSupport;
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
import java.util.HashMap;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class PortletDesignAction extends ActionSupport {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************

    public static final String CHART_FILENAME_FORMAT = "chart-{0}.png";

    //**************************************************************************
    // Injections
    //**************************************************************************

    @InjectContext
    public Context context;

    @InjectNavigation
    public Navigation navigation;

    //**************************************************************************
    // Web parameters
    //**************************************************************************

    public String portletName;
    public String cancelReturnUrl;
    public String chartId;

    public int width = 400;
    public int height = 300;
    public boolean antiAlias = true;
    public boolean borderVisible = true;

    //**************************************************************************
    // Web parameters setters (for struts.xml inspections in IntelliJ)
    //**************************************************************************

    public void setPortletName(String portletName) {
        this.portletName = portletName;
    }

    //**************************************************************************
    // Model metadata
    //**************************************************************************

    public PortletNode portlet;

    //**************************************************************************
    // Presentation elements
    //**************************************************************************

    public Form form;
    public Form displayForm;
    public JFreeChart chart;
    public JBla bla;
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
            LoggerFactory.getLogger(PortletDesignAction.class);

    //**************************************************************************
    // Action default execute method
    //**************************************************************************

    public String execute() {
        /*if (portletName == null) {
            portletName = model.getPortlets().get(0).getName();
            return REDIRECT_TO_FIRST;
        }*/

        setupPortlet();

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

        // Run/generate the chart
        generateChart();

        chartId = RandomUtil.createRandomCode();

        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("chartId", chartId);
        String portletUrl = Util.getAbsoluteUrl(Struts2Utils.buildActionUrl("chart", parameters));

        try {
            File file = RandomUtil.getTempCodeFile(CHART_FILENAME_FORMAT, chartId);
            fileName = file.getName();

            bla = new JBla(chart, file, width, height, portletUrl);
        } catch (java.io.IOException e) {
            logger.warn("Could not save portlet", e);
            SessionMessages.addErrorMessage(e.getMessage());
        }

        return "summary";
    }

    private void generateChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        java.util.List<Object[]> result;
        try {
            result = context.runSql(portlet.getDatabase(), portlet.getQuery());
            for (Object[] current : result) {
                dataset.setValue((Comparable)current[0], (Number)current[1]);
            }
        } catch (Throwable e) {
            logger.warn("Could not run portlet sql", e);
            while (e != null) {
                SessionMessages.addErrorMessage(e.getMessage());
                e = e.getCause();
            }
        }

        chart = ChartFactory.createPieChart(portlet.getTitle(), dataset,
                                true, true, true);

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
                new PortletPieUrlGenerator(portlet.getUrlExpression());
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
        plot.setNoDataMessage(getText("No_data_available"));

        plot.setCircular(true);

        plot.setBaseSectionOutlinePaint(Color.BLACK);

        DrawingSupplier supplier =
                new DesaturatedDrawingSupplier(plot.getDrawingSupplier());
        plot.setDrawingSupplier(supplier);

        // impostiamo il titolo della legenda
        String legendString = portlet.getLegend();
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

    public String chart() {
        File file = RandomUtil.getTempCodeFile(CHART_FILENAME_FORMAT, chartId);

        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            logger.warn("Could not create chart input stream", e);
        }

        return "chart";
    }


    //**************************************************************************
    // Common methods
    //**************************************************************************

    public void setupPortlet() {
        portlet = (PortletNode) navigation.getSelectedNavigationNode()
                .getActualSiteNode();
        if (portlet == null) {
            throw new ModelObjectNotFoundError(portletName);
        }
    }

    //**************************************************************************
    // Cancel
    //**************************************************************************

    public String cancel() {
        return PortofinoAction.CANCEL;
    }

}
