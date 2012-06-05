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

package com.manydesigns.portofino.pageactions.monthreport;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.gfx.ColorUtils;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.util.MimeTypes;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.pageactions.PageActionName;
import com.manydesigns.portofino.pageactions.annotations.ConfigurationClass;
import com.manydesigns.portofino.pageactions.annotations.ScriptTemplate;
import com.manydesigns.portofino.pageactions.monthreport.configuration.MonthReportConfiguration;
import com.manydesigns.portofino.pageactions.monthreport.model.MonthReportModel;
import com.manydesigns.portofino.pageactions.monthreport.util.MonthSelection;
import com.manydesigns.portofino.pageactions.monthreport.util.MonthSelectionHelper;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresPermissions;
import net.sourceforge.stripes.action.*;
import org.joda.time.DateMidnight;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.Locale;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@RequiresPermissions(level = AccessLevel.VIEW)
@ConfigurationClass(MonthReportConfiguration.class)
@PageActionName("Month report")
@ScriptTemplate("script_template.groovy")
public class MonthReportAction extends AbstractPageAction {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************

    public static final DateTimeZone dtz = DateTimeZone.UTC;


    //**************************************************************************
    // Variables
    //**************************************************************************

    protected DateMidnight     referenceDateMidnight;
    protected MonthReportModel monthReportModel;

    protected Form parametersForm;
    protected Form configurationForm;

    /**
     * Format example: Mon, Tue, etc.
     */
    protected DateTimeFormatter dayOfWeekFormatter;

    /**
     * Format example: February 2012
     */
    protected DateTimeFormatter monthYearFormatter;

    /**
     * Format example: 1, 2, 3, etc.
     */
    protected DateTimeFormatter dayOfMonthFormatter;

    /**
     * Format example: February
     */
    protected DateTimeFormatter month;




    /**
     * Color scheme
     */
    public static Color REPORT_NON_WORKING_COLOR = Color.LIGHT_GRAY;

    public int totalAlignment = Element.ALIGN_CENTER;

    public Font tableHeaderFont =
            new Font(Font.HELVETICA, 10, Font.BOLD, Color.BLACK);
    public Font tableBodyFont   =
            new Font(Font.HELVETICA, 7, Font.NORMAL, Color.BLACK);
    public Font headerFont      =
            new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);


    //**************************************************************************
    // Injections
    //**************************************************************************

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(MonthReportAction.class);

    //**************************************************************************
    // Setup & configuration
    //**************************************************************************

    public Resolution prepare(PageInstance pageInstance, ActionBeanContext context) {
        Resolution resolution = super.prepare(pageInstance, context);
        if(resolution != null) {
            return resolution;
        }
        if (!pageInstance.getParameters().isEmpty()) {
            return new ErrorResolution(404);
        }
        if (pageInstance.getConfiguration() == null) {
            pageInstance.setConfiguration(new MonthReportConfiguration());
        }
        return null;
    }

    @Before
    public void prepareFormatters() {
        Locale locale = getContext().getRequest().getLocale();
        dayOfWeekFormatter = DateTimeFormat.forPattern("E").withLocale(locale);
        monthYearFormatter = new DateTimeFormatterBuilder()
                .appendMonthOfYearText()
                .appendLiteral(" ")
                .appendYear(4, 4)
                .toFormatter().withLocale(locale);
        dayOfMonthFormatter = new DateTimeFormatterBuilder()
                .appendDayOfMonth(1)
                .toFormatter()
                .withLocale(locale);
    }

    @Before
    public void prepareColorScheme() {
        int colorScheme = 1;
        switch (colorScheme) {
            default:
        }
    }

    @Button(list = "portletHeaderButtons", key = "commons.configure", order = 1, icon = "ui-icon-wrench")
    @RequiresPermissions(level = AccessLevel.DEVELOP)
    public Resolution configure() {
        prepareConfigurationForms();
        return new ForwardResolution("/layouts/monthreport/configure.jsp");
    }

    @Button(list = "configuration", key = "commons.updateConfiguration")
    @RequiresPermissions(level = AccessLevel.DEVELOP)
    public Resolution updateConfiguration() {
        prepareConfigurationForms();
        readPageConfigurationFromRequest();
        configurationForm.readFromRequest(context.getRequest());
        boolean valid = validatePageConfiguration();
        valid = valid && configurationForm.validate();
        if (valid) {
            updatePageConfiguration();
            configurationForm.writeToObject(pageInstance.getConfiguration());
            saveConfiguration(pageInstance.getConfiguration());
            SessionMessages.addInfoMessage(getMessage("commons.configuration.updated"));
            return cancel();
        } else {
            SessionMessages.addErrorMessage(getMessage("commons.configuration.notUpdated"));
            return new ForwardResolution("/layouts/monthreport/configure.jsp");
        }
    }

    @Override
    protected void prepareConfigurationForms() {
        super.prepareConfigurationForms();
        configurationForm = new FormBuilder(MonthReportConfiguration.class).build();
        configurationForm.readFromObject(pageInstance.getConfiguration());
    }


    //**************************************************************************
    // Index view
    //**************************************************************************

    @DefaultHandler
    public Resolution execute() {
        setupParametersForm();
        if (isEmbedded()) {
            return new ForwardResolution("/layouts/monthreport/index.jsp");
        } else {
            return forwardToPortletPage("/layouts/monthreport/index.jsp");
        }
    }

    public void setupParametersForm() {
        FormBuilder formBuilder = new FormBuilder(MonthSelection.class);
        formBuilder.configSelectionProvider(
                MonthSelectionHelper.getMonthSelectionProvider(),
                "month"
        );
        formBuilder.configSelectionProvider(
                MonthSelectionHelper.getYearSelectionProvider(2000, 2012),
                "year"
        );
        parametersForm = formBuilder.build();

        LocalDate today = new LocalDate();
        MonthSelection monthSelection = new MonthSelection();
        monthSelection.year = today.getYear();
        monthSelection.month = today.getMonthOfYear();
        parametersForm.readFromObject(monthSelection);
    }


    //**************************************************************************
    // Month report
    //**************************************************************************

    @Button(list = "report-list", key = "month.report.default.report", order = 1)
    public Resolution defaultReport() throws Exception {
        setupParametersForm();
        if (parametersForm != null) {
            parametersForm.readFromRequest(context.getRequest());
            if (!parametersForm.validate()) {
                return forwardToPortletPage("/layouts/monthreport/index.jsp");
            }
            if (!parametersFormValidate()) {
                return forwardToPortletPage("/layouts/monthreport/index.jsp");
            }
        }

        setupReferenceDateMidnight();
        monthReportModel = new MonthReportModel(referenceDateMidnight);
        loadMonthReportModel();

        final File tmpFile = File.createTempFile("month-report-", ".pdf");
        FileOutputStream fos = new FileOutputStream(tmpFile);
        OutputStream os = new BufferedOutputStream(fos);

        Document document = new Document();

        // set page size and orientation
        document.setPageSize(PageSize.A4.rotate());

        // Create the writer and open the document
        PdfWriter writer = PdfWriter.getInstance(
                document, os);
        document.open();

        // Add content
        addMonthReportHeader(document);


        int daysCount = monthReportModel.getDaysCount();
        int columnCount = daysCount + 3;
        float[] colsWidth = new float[columnCount];
        colsWidth[0] = 5f;
        for (int i = 0; i < daysCount; i++) {
            colsWidth[i + 1] = 1f;
        }
        colsWidth[columnCount - 2] = 1.5f;
        colsWidth[columnCount - 1] = 2.5f;

        PdfPTable table = new PdfPTable(colsWidth); // Code 1
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);
        table.setWidthPercentage(100.0f);

        // table headers

        // Date row
        addCell(table, "Date", Element.ALIGN_LEFT);
        for (int i = 0; i < daysCount; i++) {
            MonthReportModel.Day day = monthReportModel.getDay(i);
            String text = dayOfMonthFormatter.print(day.getDayStart());
            PdfPCell cell = createCell(text, Element.ALIGN_CENTER);
            if (day.isNonWorking()) {
                cell.setBackgroundColor(REPORT_NON_WORKING_COLOR);
            }
            table.addCell(cell);
        }
        addCell(table, "Total", totalAlignment);
        addCell(table, "Notes", Element.ALIGN_CENTER);

        // Day of week row
        addCell(table, "Day", Element.ALIGN_LEFT);
        for (int i = 0; i < daysCount; i++) {
            MonthReportModel.Day day = monthReportModel.getDay(i);
            String text = dayOfWeekFormatter.print(day.getDayStart());
            PdfPCell cell = createCell(text, Element.ALIGN_CENTER);
            if (day.isNonWorking()) {
                cell.setBackgroundColor(REPORT_NON_WORKING_COLOR);
            }
            table.addCell(cell);
        }
        addCell(table, "", totalAlignment);
        addCell(table, "", Element.ALIGN_CENTER);


        // table body
        MonthReportModel.Node rootNode = monthReportModel.getRootNode();
        if (rootNode == null) {
            logger.warn("Empty root node");
        } else {
            printNode(table, rootNode, 0);
        }

        document.add(table);

        // close the document
        document.close();

        // Send the result
        FileInputStream fileInputStream = new FileInputStream(tmpFile);
        return new StreamingResolution(MimeTypes.APPLICATION_PDF, fileInputStream) {
            @Override
            protected void stream(HttpServletResponse response) throws Exception {
                super.stream(response);
                if (!tmpFile.delete()) {
                    logger.warn("Could not delete tmp file: {}", tmpFile);
                }
            }
        }.setFilename(getDefaultReportFilename()).setLength(tmpFile.length());
    }

    public String getDefaultReportFilename() {
        return String.format("month-report-%s-%d.pdf",
                referenceDateMidnight.getMonthOfYear(),
                referenceDateMidnight.getYear());
    }

    public void addMonthReportHeader(Document document) throws Exception {
        PdfPTable headerTable = new PdfPTable(3);
        headerTable.setWidthPercentage(100.0f);

        addMonthReportHeaderLeft(headerTable);
        addMonthReportHeaderCenter(headerTable);
        addMonthReportHeaderRight(headerTable);

        document.add(headerTable);
    }

    public void addMonthReportHeaderLeft(PdfPTable headerTable) throws Exception {
        PdfPCell headerCell = new PdfPCell(new Phrase(""));
        headerCell.setBorder(Rectangle.NO_BORDER);
        headerTable.addCell(headerCell);
    }

    public void addMonthReportHeaderCenter(PdfPTable headerTable) throws Exception {
        PdfPCell headerCell = new PdfPCell(new Phrase("Timesheet"));
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setBorder(Rectangle.NO_BORDER);
        headerTable.addCell(headerCell);
    }

    public void addMonthReportHeaderRight(PdfPTable headerTable) throws Exception {
        DateMidnight referenceDateMidnight =
                monthReportModel.getReferenceDateMidnight();
        Paragraph headerParagraph = new Paragraph();
        String personTitle = String.format("Name: %s",
                monthReportModel.getPersonName());
        String monthTitle = String.format("Month: %s",
                monthYearFormatter.print(referenceDateMidnight));
        headerParagraph.add(new Chunk(personTitle));
        headerParagraph.add(Chunk.NEWLINE);
        headerParagraph.add(new Chunk(monthTitle));

        PdfPCell headerCell = new PdfPCell(headerParagraph);
        headerCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        headerCell.setBorder(Rectangle.NO_BORDER);
        headerTable.addCell(headerCell);
    }

    public void printNode(PdfPTable table, MonthReportModel.Node node, int indentation)
            throws Exception {
        List<MonthReportModel.Node> childNodes = node.getChildNodes();
        Color color = node.getColor();
        Color nonWorkingColor = ColorUtils.multiply(color, REPORT_NON_WORKING_COLOR);
        if (childNodes.isEmpty()) {
            logger.debug("Leaf node: {}", node.getId());
            PdfPCell cell = createCell(node.getName(), Element.ALIGN_LEFT);
            float paddingLeft = cell.getPaddingLeft() + indentation * 2;
            cell.setPaddingLeft(paddingLeft);
            cell.setBackgroundColor(color);
            table.addCell(cell);

            printNodeMinutes(table, node, color, nonWorkingColor);

            addCell(table, Integer.toString(node.getMinutesTotal()), totalAlignment);
            addCell(table, "", Element.ALIGN_CENTER);
        } else {
            logger.debug("Non-leaf node: {}", node.getId());
            logger.debug("Printing node header", node.getId());
            PdfPCell cell = createCell(node.getName(), Element.ALIGN_LEFT);
            float paddingLeft = cell.getPaddingLeft() + indentation * 2;
            cell.setPaddingLeft(paddingLeft);
            cell.setBackgroundColor(color);
            table.addCell(cell);

            printNodeBlankMinutes(table, node, color, nonWorkingColor);

            addCell(table, "", Element.ALIGN_CENTER);
            addCell(table, "", Element.ALIGN_CENTER);

            logger.debug("Printing child nodes", node.getId());
            for (MonthReportModel.Node current : childNodes) {
                printNode(table, current, indentation + 1);
            }

            logger.debug("Printing node footer", node.getId());
            addCell(table, "Total (" + node.getName() + ")", Element.ALIGN_RIGHT);
            printNodeMinutes(table, node, null, REPORT_NON_WORKING_COLOR);
            addCell(table, Integer.toString(node.getMinutesTotal()), totalAlignment);
            addCell(table, "", Element.ALIGN_CENTER);
        }
    }

    public void printNodeMinutes(PdfPTable table, MonthReportModel.Node node,
                                 Color color, Color nonWorkingColor)
            throws Exception {
        for (int i = 0; i < monthReportModel.getDaysCount(); i++) {
            MonthReportModel.Day day = monthReportModel.getDay(i);
            Color actualBackgroundColor = (day.isNonWorking())
                    ? nonWorkingColor
                    : color;
            int minutes = node.getMinutes(i);
            addCell(table, Integer.toString(minutes), Element.ALIGN_CENTER, actualBackgroundColor);
        }
    }

    public void printNodeBlankMinutes(PdfPTable table,
                                      MonthReportModel.Node node,
                                      Color color,
                                      Color nonWorkingColor)
            throws Exception {
        if (color == null) {
            color = Color.WHITE;
        }
        for (int i = 0; i < monthReportModel.getDaysCount(); i++) {
            MonthReportModel.Day day = monthReportModel.getDay(i);
            Color actualBackgroundColor = (day.isNonWorking())
                    ? nonWorkingColor
                    : color;
            addCell(table, "", Element.ALIGN_CENTER, actualBackgroundColor);
        }
    }

    public PdfPCell addCell(PdfPTable table, String text, int alignment)
            throws Exception {
        return addCell(table, text, alignment, null);
    }

    public PdfPCell addCell(PdfPTable table, String text, int alignment, Color backgroundColor)
            throws Exception {
        PdfPCell cell = createCell(text, alignment);
        if (backgroundColor != null) {
            cell.setBackgroundColor(backgroundColor);
        }
        table.addCell(cell);
        return cell;
    }

    public PdfPCell createCell(String text, int alignment) throws Exception {
        PdfPCell cell = new PdfPCell(new Phrase(text, tableBodyFont));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }


    public MonthReportModel.Node addReportNode(MonthReportModel.Node rootNode, String id, String name, Color color) {
        MonthReportModel.Node node =
                monthReportModel.createNode(id, name);
        node.setColor(color);
        rootNode.getChildNodes().add(node);
        return node;
    }

    public void loadMonthReportModel() {
        logger.debug("Placeholder");
    }

    public void setupReferenceDateMidnight() {
        MonthSelection monthSelection = new MonthSelection();
        parametersForm.writeToObject(monthSelection);

        referenceDateMidnight = new DateMidnight(
                monthSelection.year, monthSelection.month, 1, dtz);
    }

    public boolean parametersFormValidate() {
        logger.debug("Placeholder");
        return true;
    }


    //--------------------------------------------------------------------------
    // Getters/setters
    //--------------------------------------------------------------------------


    public DateMidnight getReferenceDateMidnight() {
        return referenceDateMidnight;
    }

    public void setReferenceDateMidnight(DateMidnight referenceDateMidnight) {
        this.referenceDateMidnight = referenceDateMidnight;
    }

    public Form getParametersForm() {
        return parametersForm;
    }

    public void setParametersForm(Form parametersForm) {
        this.parametersForm = parametersForm;
    }

    public MonthReportModel getMonthReportModel() {
        return monthReportModel;
    }

    public void setMonthReportModel(MonthReportModel monthReportModel) {
        this.monthReportModel = monthReportModel;
    }

    public Form getConfigurationForm() {
        return configurationForm;
    }

    public void setConfigurationForm(Form configurationForm) {
        this.configurationForm = configurationForm;
    }

    public DateTimeFormatter getDayOfWeekFormatter() {
        return dayOfWeekFormatter;
    }

    public void setDayOfWeekFormatter(DateTimeFormatter dayOfWeekFormatter) {
        this.dayOfWeekFormatter = dayOfWeekFormatter;
    }

    public DateTimeFormatter getMonthYearFormatter() {
        return monthYearFormatter;
    }

    public void setMonthYearFormatter(DateTimeFormatter monthYearFormatter) {
        this.monthYearFormatter = monthYearFormatter;
    }

    public DateTimeFormatter getDayOfMonthFormatter() {
        return dayOfMonthFormatter;
    }

    public void setDayOfMonthFormatter(DateTimeFormatter dayOfMonthFormatter) {
        this.dayOfMonthFormatter = dayOfMonthFormatter;
    }

    public int getTotalAlignment() {
        return totalAlignment;
    }

    public void setTotalAlignment(int totalAlignment) {
        this.totalAlignment = totalAlignment;
    }

    public Font getTableHeaderFont() {
        return tableHeaderFont;
    }

    public void setTableHeaderFont(Font tableHeaderFont) {
        this.tableHeaderFont = tableHeaderFont;
    }

    public Font getTableBodyFont() {
        return tableBodyFont;
    }

    public void setTableBodyFont(Font tableBodyFont) {
        this.tableBodyFont = tableBodyFont;
    }

    public Font getHeaderFont() {
        return headerFont;
    }

    public void setHeaderFont(Font headerFont) {
        this.headerFont = headerFont;
    }
}
