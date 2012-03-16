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

package com.manydesigns.portofino.pageactions.timesheet;

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
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.util.MimeTypes;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.buttons.annotations.Buttons;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.pageactions.PageActionName;
import com.manydesigns.portofino.pageactions.annotations.ConfigurationClass;
import com.manydesigns.portofino.pageactions.annotations.ScriptTemplate;
import com.manydesigns.portofino.pageactions.timesheet.configuration.TimesheetConfiguration;
import com.manydesigns.portofino.pageactions.timesheet.model.*;
import com.manydesigns.portofino.pageactions.timesheet.util.TimesheetSelection;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresPermissions;
import com.manydesigns.portofino.stripes.NoCacheStreamingResolution;
import net.sourceforge.stripes.action.*;
import org.apache.commons.lang.StringUtils;
import org.joda.time.*;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.json.JSONException;
import org.json.JSONStringer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@RequiresPermissions(level = AccessLevel.VIEW)
@ConfigurationClass(TimesheetConfiguration.class)
@PageActionName("Timesheet")
@ScriptTemplate("script_template.txt")
public class TimesheetAction extends AbstractPageAction {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //**************************************************************************
    // Constants
    //**************************************************************************

    protected final static Pattern      hoursPattern       =
            Pattern.compile("(\\d+):(\\d+)");
    public static final    int          MINUTES_IN_A_DAY   = 24 * 60;
    public static final    String       ENTRY_INPUT_FORMAT = "cell-%d-%s";
    public static final    String       NOTE_INPUT_FORMAT  = "note-%d-%s";
    public static final    DateTimeZone dtz                = DateTimeZone.UTC;


    public static final Font tableHeaderFont =
            new Font(Font.HELVETICA, 10, Font.BOLD, Color.BLACK);
    public static final Font tableBodyFont   =
            new Font(Font.HELVETICA, 7, Font.NORMAL, Color.BLACK);
    public static final Font headerFont      =
            new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);

    public static final int totalAlignment = Element.ALIGN_CENTER;

    public static Color REPORT_NON_WORKING_COLOR = Color.LIGHT_GRAY;

    //**************************************************************************
    // Variables
    //**************************************************************************

    protected Form form;

    protected String personId;
    protected Date   referenceDate;

    protected final List<Person> availablePersons = new ArrayList<Person>();

    protected WeekEntryModel      weekEntryModel;
    protected NonWorkingDaysModel nonWorkingDaysModel;
    protected MonthReportModel    monthReportModel;

    protected Integer day;
    protected Integer month;
    protected Integer year;
    protected boolean nonWorking;

    protected Form configurationForm;

    /**
     * Format example: February 13, 2012
     */
    protected DateTimeFormatter longDateFormatter;

    /**
     * Format example: Mon, Tue, etc.
     */
    protected DateTimeFormatter dayOfWeekFormatter;

    /**
     * Format example: 2/13/12
     */
    protected DateTimeFormatter dateFormatter;

    /**
     * Format example: February 2012
     */
    protected DateTimeFormatter monthFormatter;

    /**
     * Format example: 1, 2, 3, etc.
     */
    protected DateTimeFormatter dayOfMonthFormatter;

    /**
     * yyyy M d
     */
    protected DateTimeFormatter referenceDateFormatter;


    /**
     * Color scheme
     */
    protected Color borderColor;
    protected Color todayColor;
    protected Color nonWorkingColor;
    protected Color headerBgColor;
    protected Color headerColor;
    protected Color footerBgColor;
    protected Color footerColor;
    protected Color dayTodayHeaderBgColor;
    protected Color dayNonWorkingHeaderBgColor;

    protected Color oddRowBgColor;
    protected Color evenRowBgColor;
    protected Color hoursTodayOddBgColor;
    protected Color hoursTodayEvenBgColor;
    protected Color hoursNonWorkingOddBgColor;
    protected Color hoursNonWorkingEvenBgColor;

    protected Color todayFooterBgColor;
    protected Color nonWorkingFooterBgColor;


    //**************************************************************************
    // Injections
    //**************************************************************************

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(TimesheetAction.class);

    //**************************************************************************
    // Setup & configuration
    //**************************************************************************

    public Resolution prepare(PageInstance pageInstance, ActionBeanContext context) {
        this.pageInstance = pageInstance;
        if (!pageInstance.getParameters().isEmpty()) {
            return new ErrorResolution(404);
        }
        if (pageInstance.getConfiguration() == null) {
            pageInstance.setConfiguration(new TimesheetConfiguration());
        }
        return null;
    }

    @Before
    public void prepareFormatters() {
        Locale locale = getContext().getRequest().getLocale();
        longDateFormatter = DateTimeFormat.longDate().withLocale(locale);
        dayOfWeekFormatter = DateTimeFormat.forPattern("E").withLocale(locale);
        dateFormatter = DateTimeFormat.shortDate().withLocale(locale);
        monthFormatter = new DateTimeFormatterBuilder()
                .appendMonthOfYearText()
                .appendLiteral(" ")
                .appendYear(4, 4)
                .toFormatter().withLocale(locale);
        dayOfMonthFormatter = new DateTimeFormatterBuilder()
                .appendDayOfMonth(1)
                .toFormatter()
                .withLocale(locale);
        referenceDateFormatter = DateTimeFormat.forPattern("yyyy M d");
    }

    @Before
    public void prepareColorScheme() {
        int colorScheme = 1;
        switch (colorScheme) {
            default:
                borderColor = new Color(0xCBCBCB);
                todayColor = new Color(0xfff7c6);
                nonWorkingColor = new Color(0xD7F7EC);
                headerBgColor = new Color(0x8899DD);
                headerColor = Color.WHITE;
                footerBgColor = new Color(0xF7EBD7);
                footerColor = new Color(0x333333);
        }
        dayTodayHeaderBgColor = ColorUtils.multiply(headerBgColor, todayColor);
        dayNonWorkingHeaderBgColor = ColorUtils.multiply(headerBgColor, nonWorkingColor);

        oddRowBgColor = Color.WHITE;
        evenRowBgColor = new Color(0xEDF5FF);
        hoursTodayOddBgColor = ColorUtils.multiply(oddRowBgColor, todayColor);
        hoursTodayEvenBgColor = ColorUtils.multiply(evenRowBgColor, todayColor);
        hoursNonWorkingOddBgColor = ColorUtils.multiply(oddRowBgColor, nonWorkingColor);
        hoursNonWorkingEvenBgColor = ColorUtils.multiply(evenRowBgColor, nonWorkingColor);

        todayFooterBgColor = ColorUtils.multiply(footerBgColor, todayColor);
        nonWorkingFooterBgColor = ColorUtils.multiply(footerBgColor, nonWorkingColor);

    }

    @Button(list = "portletHeaderButtons", key = "commons.configure", order = 1, icon = "ui-icon-wrench")
    @RequiresPermissions(level = AccessLevel.DEVELOP)
    public Resolution configure() {
        prepareConfigurationForms();
        return new ForwardResolution("/layouts/timesheet/configure.jsp");
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
            return new ForwardResolution("/layouts/timesheet/configure.jsp");
        }
    }

    @Override
    protected void prepareConfigurationForms() {
        super.prepareConfigurationForms();
        configurationForm = new FormBuilder(TimesheetConfiguration.class).build();
        configurationForm.readFromObject(pageInstance.getConfiguration());
    }


    //**************************************************************************
    // Index view
    //**************************************************************************

    @DefaultHandler
    public Resolution execute() {
        loadExecuteModel();
        TimesheetSelection timesheetSelection = new TimesheetSelection();
        FormBuilder formBuilder =
                new FormBuilder(TimesheetSelection.class);

        // selection provider persone
        DefaultSelectionProvider personSelectionProvider =
                new DefaultSelectionProvider("person");
        for (Person person : availablePersons) {
            String name;
            if (person.isMe()) {
                name = getMessage("timesheet.myself");
                timesheetSelection.personId = person.getId();
            } else {
                name = person.getLongName();
            }
            personSelectionProvider.appendRow(person.getId(), name, true);
        }
        formBuilder.configSelectionProvider(personSelectionProvider, "personId");

        // selection provider settimane
        DefaultSelectionProvider weeksAgoSelectionProvider =
                new DefaultSelectionProvider("week");

        DateMidnight today = new DateMidnight(dtz);
        DateMidnight monday = today.withDayOfWeek(DateTimeConstants.MONDAY);
        for (int i = 0; i < 10; i++) {
            DateMidnight sunday = monday.plusDays(6);
            String value = referenceDateFormatter.print(monday);
            if (i == 0) {
                // set default date
                timesheetSelection.referenceDate = value;
            }
            String label = dateFormatter.print(monday) + " - " + dateFormatter.print(sunday);
            weeksAgoSelectionProvider.appendRow(value, label, true);
            monday = monday.minusWeeks(1);
        }
        formBuilder.configSelectionProvider(weeksAgoSelectionProvider, "referenceDate");

        form = formBuilder.build();
        form.readFromObject(timesheetSelection);

        if (isEmbedded()) {
            return new ForwardResolution("/layouts/timesheet/index.jsp");
        } else {
            return forwardToPortletPage("/layouts/timesheet/index.jsp");
        }
    }

    public void loadExecuteModel() {
        logger.debug("Placeholder");
    }


    //**************************************************************************
    // Week data entry
    //**************************************************************************

    @Button(list = "timesheet-selection", key = "timesheet.go.to.week.entry", order = 1)
    public Resolution weekEntry() throws Exception {
        LocalDate referenceLocalDate = new LocalDate(referenceDate);
        weekEntryModel = new WeekEntryModel(referenceLocalDate);

        loadWeekEntryModel();

        return new ForwardResolution("/layouts/timesheet/week-entry.jsp");
    }

    @Button(list = "timesheet-we-navigation", key = "timesheet.previous.week", order = 1)
    public Resolution weekEntryPreviousWeek() throws Exception {
        LocalDate referenceLocalDate = new LocalDate(referenceDate);
        referenceLocalDate = referenceLocalDate.minusWeeks(1);
        referenceDate = new Date(referenceLocalDate.getYear() - 1900,
                referenceLocalDate.getMonthOfYear() - 1,
                referenceLocalDate.getDayOfMonth());
        return weekEntry();
    }

    @Button(list = "timesheet-we-navigation", key = "timesheet.next.week", order = 2)
    public Resolution weekEntryNextWeek() throws Exception {
        LocalDate referenceLocalDate = new LocalDate(referenceDate);
        referenceLocalDate = referenceLocalDate.plusWeeks(1);
        referenceDate = new Date(referenceLocalDate.getYear() - 1900,
                referenceLocalDate.getMonthOfYear() - 1,
                referenceLocalDate.getDayOfMonth());
        return weekEntry();
    }

    @Button(list = "timesheet-week-entry", key = "commons.save", order = 1)
    public Resolution saveWeekEntryModel() throws Exception {
        LocalDate referenceLocalDate = new LocalDate(referenceDate);

        weekEntryModel = new WeekEntryModel(referenceLocalDate);

        try {
            beginWeekEntryTransaction();

            loadWeekEntryModel();

            HttpServletRequest request = getContext().getRequest();

            boolean success = true;
            for (int dayIndex = 0; dayIndex < 7; dayIndex++) {
                WeekEntryModel.Day day = weekEntryModel.getDay(dayIndex);

                WeekEntryModel.DayStatus dayStatus =
                        day.getStatus();
                if (dayStatus == null || dayStatus == WeekEntryModel.DayStatus.LOCKED) {
                    continue;
                }
                for (Activity activity : weekEntryModel.getActivities()) {
                    success = saveOneWeekEntry(request, success, dayIndex, day, activity);
                }
            }
            if (success) {
                commitWeekEntryTransaction();
                String msg = getMessage("timesheet.saved.successuly");
                SessionMessages.addInfoMessage(msg);
            } else {
                rollbackWeekEntryTransaction();
            }
        } finally {
            endWeekEntryTransaction();
        }
        return weekEntry();
    }

    public boolean saveOneWeekEntry(HttpServletRequest request, boolean success, int dayIndex, WeekEntryModel.Day day, Activity activity) {
        WeekEntryModel.Entry oldEntry =
                day.findEntryByActivity(activity);
        String entryInputName = String.format(ENTRY_INPUT_FORMAT,
                dayIndex, activity.getId());
        String entryValue = StringUtils.trimToNull(request.getParameter(entryInputName));
        logger.debug("Week entry parameter: {}: {}", entryInputName, entryValue);

        String noteInputName = String.format(NOTE_INPUT_FORMAT,
                dayIndex, activity.getId());
        String noteValue = StringUtils.trimToNull(request.getParameter(noteInputName));

        int totalMinutes;
        if (entryValue == null) {
            totalMinutes = 0;
        } else {
            Matcher matcher = hoursPattern.matcher(entryValue);
            if (matcher.matches()) {
                try {
                    int hours = Integer.parseInt(matcher.group(1));
                    int minutes = Integer.parseInt(matcher.group(2));
                    totalMinutes = hours * 60 + minutes;
                } catch (NumberFormatException e) {
                    totalMinutes = 0;
                    success = false;
                    SessionMessages.addErrorMessage("Invalid week entry: " + entryValue);
                }
            } else {
                totalMinutes = 0;
                success = false;
                SessionMessages.addErrorMessage("Invalid week entry: " + entryValue);
            }
        }
        if (totalMinutes < 0) {
            totalMinutes = 0;
            success = false;
        }
        if (totalMinutes > MINUTES_IN_A_DAY) {
            totalMinutes = MINUTES_IN_A_DAY;
            success = false;
        }
        if (totalMinutes == 0 && noteValue == null) {
            if (oldEntry == null) {
                logger.debug("Empty entry. Do nothing.");
            } else {
                logger.debug("Deleting entry.");
                deleteWeekEntry(day, activity);
            }
        } else {
            if (oldEntry == null) {
                logger.debug("Saving new entry.");
                saveWeekEntry(day, activity, totalMinutes, noteValue);
            } else {
                logger.debug("Updating entry.");
                updateWeekEntry(day, activity, totalMinutes, noteValue);
            }
        }
        return success;
    }

    public void loadWeekEntryModel() throws Exception {
        logger.debug("Placeholder");
    }

    public void beginWeekEntryTransaction() {
        logger.debug("Placeholder");
    }

    public void commitWeekEntryTransaction() {
        logger.debug("Placeholder");
    }

    public void rollbackWeekEntryTransaction() {
        logger.debug("Placeholder");
    }

    public void endWeekEntryTransaction() {
        logger.debug("Placeholder");
    }

    public void saveWeekEntry(WeekEntryModel.Day day, Activity activity, int minutes, String note) {
        logger.debug("Placeholder");
    }

    public void updateWeekEntry(WeekEntryModel.Day day, Activity activity, int minutes, String note) {
        logger.debug("Placeholder");
    }

    public void deleteWeekEntry(WeekEntryModel.Day day, Activity activity) {
        logger.debug("Placeholder");
    }


    //**************************************************************************
    // Non working days view
    //**************************************************************************

    @Button(list = "timesheet-admin", key = "timesheet.manage.non.working.days", order = 1)
    public Resolution nonWorkingDays() {
        DateTime referenceDateTime = new DateTime(referenceDate, dtz);
        nonWorkingDaysModel = new NonWorkingDaysModel(referenceDateTime);
        loadNonWorkingDaysModel();
        return new ForwardResolution("/layouts/timesheet/non-working-days.jsp");
    }

    @Button(list = "timesheet-nwd-navigation", key = "timesheet.previous.month", order = 1)
    public Resolution nonWorkingDaysPreviousMonth() throws Exception {
        DateMidnight referenceDateMidnight =
                new DateMidnight(referenceDate, dtz);
        referenceDateMidnight = referenceDateMidnight.minusMonths(1);
        referenceDate = referenceDateMidnight.toDate();
        return nonWorkingDays();
    }

    @Button(list = "timesheet-nwd-navigation", key = "timesheet.next.month", order = 2)
    public Resolution nonWorkingDaysNextMonth() throws Exception {
        DateMidnight referenceDateMidnight =
                new DateMidnight(referenceDate, dtz);
        referenceDateMidnight = referenceDateMidnight.plusMonths(1);
        referenceDate = referenceDateMidnight.toDate();
        return nonWorkingDays();
    }

    public Resolution configureNonWorkingDay() throws JSONException {
        logger.info("Configuring non working day. Year/month/day: {}/{}/{}. Non-working: {}",
                new Object[]{year, month, day, nonWorking});
        LocalDate today = new LocalDate(year, month, day);
        saveNonWorkingDay(today, nonWorking);
        JSONStringer js = new JSONStringer();
        js.object()
                .key("result")
                .value("ok")
                .endObject();
        String jsonText = js.toString();
        return new NoCacheStreamingResolution("application/json", jsonText);
    }

    public void loadNonWorkingDaysModel() {
        logger.debug("Load non-working day");
    }

    public void saveNonWorkingDay(LocalDate date, boolean nonWorking) {
        logger.debug("Save non-working day");
    }

    //**************************************************************************
    // Month report
    //**************************************************************************

    @Button(list = "timesheet-admin", key = "timesheet.month.report", order = 2)
    public Resolution monthReport() throws Exception {
        DateMidnight referenceDateMidnight =
                new DateMidnight(referenceDate, dtz);
        monthReportModel = new MonthReportModel(referenceDateMidnight);
        loadMonthReportModel();

        final File tmpFile = File.createTempFile("report-", ".pdf");
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
        }.setFilename("month-report.pdf").setLength(tmpFile.length());
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
                monthFormatter.print(referenceDateMidnight));
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


    public void loadMonthReportModel() {
        logger.debug("Placeholder");
    }

    public MonthReportModel.Node addReportNode(MonthReportModel.Node rootNode, String id, String name, Color color) {
        MonthReportModel.Node node =
                monthReportModel.createNode(id, name);
        node.setColor(color);
        rootNode.getChildNodes().add(node);
        return node;
    }


    //**************************************************************************
    // Other action handlers
    //**************************************************************************

    @Override
    @Buttons({
            @Button(list = "timesheet-week-entry", key = "commons.ok", order = 99),
            @Button(list = "timesheet-non-working-days", key = "commons.ok", order = 99)
    })
    @RequiresPermissions(level = AccessLevel.VIEW)
    public Resolution cancel() {
        return super.cancel();
    }

    public static boolean checkSunday(LocalDate currentDay, boolean locked) {
        return locked || (currentDay.getDayOfWeek() == DateTimeConstants.SUNDAY);
    }

    public static LocalDate skipNonWorkingDays(LocalDate currentDay) {
        while (currentDay.getDayOfWeek() >= DateTimeConstants.SATURDAY) {
            currentDay = currentDay.minusDays(1);
        }
        return currentDay;
    }

    //--------------------------------------------------------------------------
    // Getters/setters
    //--------------------------------------------------------------------------


    public Form getForm() {
        return form;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public Date getReferenceDate() {
        return referenceDate;
    }

    public void setReferenceDate(Date referenceDate) {
        this.referenceDate = referenceDate;
    }

    public List<Person> getAvailablePersons() {
        return availablePersons;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public boolean getNonWorking() {
        return nonWorking;
    }

    public void setNonWorking(boolean nonWorking) {
        this.nonWorking = nonWorking;
    }

    public WeekEntryModel getWeekEntryModel() {
        return weekEntryModel;
    }

    public NonWorkingDaysModel getNonWorkingDaysModel() {
        return nonWorkingDaysModel;
    }

    public Form getConfigurationForm() {
        return configurationForm;
    }

    public DateTimeFormatter getLongDateFormatter() {
        return longDateFormatter;
    }

    public DateTimeFormatter getDayOfWeekFormatter() {
        return dayOfWeekFormatter;
    }

    public DateTimeFormatter getDateFormatter() {
        return dateFormatter;
    }

    public DateTimeFormatter getMonthFormatter() {
        return monthFormatter;
    }

    public DateTimeFormatter getDayOfMonthFormatter() {
        return dayOfMonthFormatter;
    }

    public DateTimeFormatter getReferenceDateFormatter() {
        return referenceDateFormatter;
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public Color getTodayColor() {
        return todayColor;
    }

    public Color getNonWorkingColor() {
        return nonWorkingColor;
    }

    public Color getHeaderBgColor() {
        return headerBgColor;
    }

    public Color getHeaderColor() {
        return headerColor;
    }

    public Color getFooterBgColor() {
        return footerBgColor;
    }

    public Color getFooterColor() {
        return footerColor;
    }

    public Color getDayTodayHeaderBgColor() {
        return dayTodayHeaderBgColor;
    }

    public Color getDayNonWorkingHeaderBgColor() {
        return dayNonWorkingHeaderBgColor;
    }

    public Color getOddRowBgColor() {
        return oddRowBgColor;
    }

    public Color getEvenRowBgColor() {
        return evenRowBgColor;
    }

    public Color getHoursTodayOddBgColor() {
        return hoursTodayOddBgColor;
    }

    public Color getHoursTodayEvenBgColor() {
        return hoursTodayEvenBgColor;
    }

    public Color getHoursNonWorkingOddBgColor() {
        return hoursNonWorkingOddBgColor;
    }

    public Color getHoursNonWorkingEvenBgColor() {
        return hoursNonWorkingEvenBgColor;
    }

    public Color getTodayFooterBgColor() {
        return todayFooterBgColor;
    }

    public Color getNonWorkingFooterBgColor() {
        return nonWorkingFooterBgColor;
    }
}
