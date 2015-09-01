<%@ page import="com.manydesigns.elements.ElementsThreadLocals" %>
<%@ page import="com.manydesigns.elements.xml.XhtmlBuffer" %>
<%@ page import="com.manydesigns.portofino.pageactions.calendar.Event" %>
<%@ page import="com.manydesigns.portofino.pageactions.calendar.EventWeek" %>
<%@ page import="com.manydesigns.portofino.pageactions.calendar.MonthView" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.joda.time.DateMidnight" %>
<%@ page import="org.joda.time.DateTime" %>
<%@ page import="org.joda.time.Interval" %>
<%@ page import="org.joda.time.format.DateTimeFormatter" %>
<%@ page import="org.joda.time.format.DateTimeFormatterBuilder" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Locale" %>
<%@ page import="com.manydesigns.portofino.calendar.PresentationHelper" %>
<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.calendar.CalendarAction"/>
<%
    int maxEventsPerCell = actionBean.getConfiguration().getMaxEventsPerCellInMonthView();
    MonthView monthView = actionBean.getMonthView();
    DateTimeFormatter dayOfWeekFormatter =
            new DateTimeFormatterBuilder()
                    .appendDayOfWeekShortText()
                    .toFormatter()
                    .withLocale(request.getLocale());

    DateTimeFormatter firstDayOfMonthFormatter =
            new DateTimeFormatterBuilder()
                    .appendMonthOfYearShortText()
                    .appendLiteral(" ")
                    .appendDayOfMonth(1)
                    .toFormatter()
                    .withLocale(request.getLocale());

    DateTimeFormatter monthFormatter =
            new DateTimeFormatterBuilder()
                    .appendMonthOfYearText()
                    .appendLiteral(" ")
                    .appendYear(4, 4)
                    .toFormatter()
                    .withLocale(request.getLocale());
    XhtmlBuffer xhtmlBuffer = new XhtmlBuffer(out);
%>
<style type="text/css">
    .calendar-container {
        position: relative; height: <%= ((maxEventsPerCell + 1) * 24) * 6 %>px;
    }
    .days-table td, .days-table th {
        margin: 0; padding: 0 0 0 10px; border: none; text-align: left;
    }
    .calendar-table {
        position: absolute; width: 100%; margin-top: 10px; height: 90%;
        border-right: 1px solid #DDDDDD;
        border-bottom: 1px solid #DDDDDD;
    }
    .calendar-table td, .calendar-table th {
        border-style: none;
        border-top: 1px solid #DDDDDD;
        border-left: 1px solid #DDDDDD;
    }
    .calendar-container table {
        width: 100%; padding: 0; margin: 0; table-layout: fixed;
    }
    .calendar-row {
        position: absolute; width: 100%; height: <%= 100.0 / 6.0 %>%;
    }
    .grid-table {
        width: 100%; height: 100%;
        position: absolute; left: 0px; top: 0px;
        border: none;
    }
    .grid-table td {
        border-left: 1px solid #DDDDDD;
    }
    .grid-table td.today {
        border: solid 1px black;
    }
    .events-table {
        position: relative; border: none;
    }
    .events-table td {
        padding: 1px 1px 0 2px; border: none;
    }
    .events-table th {
        padding: 0 0 3px 10px; border: none; text-align: left;
    }
    .event {
        border-radius: 3px;
        font-size: smaller;
        letter-spacing: 0.03em;
        text-align: center;
        padding: 0 0 0 4px; white-space: nowrap; overflow: hidden;
    }
    .event .more{
        color: #a9a9a9;
        text-shadow: 0px 0px 1px rgba(255,255,255,0.5);
    }
    .event a {
        color: white;
        text-shadow: 0px 0px 1px rgba(0,0,0,0.5);
    }
    .outOfMonth {
        color: #BBBBBB;
    }
    .event-dialog {
        display: none;
    }
</style>
<h1><%= StringUtils.capitalize(monthFormatter.print(monthView.getReferenceDateTime())) %></h1>
<div>
    <div class="pull-right" >
        <button type="submit" name="agendaView" class="btn btn-default btn-sm">
            <span class="glyphicon glyphicon-book"></span>
            <fmt:message key="agenda" />
        </button>
    </div>
    <div>
        <%
            Interval monthInterval = monthView.getMonthInterval();
            boolean todayDisabled = monthInterval.contains(new DateTime());
        %>
        <button type="submit" name="today" class="btn btn-default btn-sm"<%= todayDisabled ? " disabled='true'" : "" %>>
            <span class="glyphicon glyphicon-calendar"></span>
            <fmt:message key="current.month" />
        </button>
        <button type="submit" name="prevMonth" class="btn btn-default btn-sm">
            <em class="glyphicon glyphicon-chevron-left"></em>
            <fmt:message key="previous" />
        </button>
        <button type="submit" name="nextMonth" class="btn btn-default btn-sm">
            <fmt:message key="next" />
            <em class="glyphicon glyphicon-chevron-right"></em>
        </button>
    </div>
</div>
<div class="horizontalSeparator"></div>
<div class="calendar-container">
    <table class="days-table">
        <tr>
            <%
                for(int i = 0; i < 7; i++) {
                    DateMidnight dayOfWeek = monthView.getWeek(0).getDay(i).getDayStart();
                    xhtmlBuffer.openElement("th");
                    xhtmlBuffer.write(dayOfWeekFormatter.print(dayOfWeek));
                    xhtmlBuffer.closeElement("th");
                }
            %>
        </tr>
    </table>
    <div class="calendar-table">
        <% for(int index = 0; index < 6; index++) {
            MonthView.MonthViewWeek week = monthView.getWeek(index);
        %>
            <div class="calendar-row" style="top: <%= index * 100.0 / 6.0 %>%;">
                <table class="grid-table">
                    <tr>
                        <%
                        for(int i = 0; i < 7; i++) {
                            MonthView.MonthViewDay day = week.getDay(i);
                            xhtmlBuffer.openElement("td");
                            if(day.getDayInterval().contains(new DateTime())) {
                                xhtmlBuffer.addAttribute("class", "today");
                            }
                            xhtmlBuffer.closeElement("td");
                        }
                        %>
                    </tr>
                </table>
                <table class="events-table">
                    <tr>
                        <%
                        for(int i = 0; i < 7; i++) {
                            MonthView.MonthViewDay day = week.getDay(i);
                            xhtmlBuffer.openElement("th");
                            if(!monthInterval.contains(day.getDayStart())) {
                                xhtmlBuffer.addAttribute("class", "outOfMonth");
                            }
                            int dayOfMonth = day.getDayStart().getDayOfMonth();
                            if(1 == dayOfMonth) {
                                xhtmlBuffer.write(firstDayOfMonthFormatter.print(day.getDayStart()));
                            } else {
                                xhtmlBuffer.write(dayOfMonth + "");
                            }
                            xhtmlBuffer.closeElement("th");
                        } %>
                    </tr>
                    <%
                    for(int row = 0; row < maxEventsPerCell - 1; row++) {
                        xhtmlBuffer.openElement("tr");
                        for(int dayOfWeek = 0; dayOfWeek < 7; dayOfWeek++) {
                            MonthView.MonthViewDay day = week.getDay(dayOfWeek);
                            writeEventCell(monthView, day, dayOfWeek, row, xhtmlBuffer);
                        }
                        xhtmlBuffer.closeElement("tr");
                    }

                    //Last row
                    xhtmlBuffer.openElement("tr");
                    boolean moreThanOneLeft = false;
                    for(int dayOfWeek = 0; dayOfWeek < 7; dayOfWeek++) {
                        MonthView.MonthViewDay day = week.getDay(dayOfWeek);
                        List<EventWeek> eventsOfTheDay = day.getSlots();

                        int numberOfEvents = 0;
                        for(EventWeek e : eventsOfTheDay) {
                            if(e != null) {
                                numberOfEvents++;
                            }
                        }
                        if(numberOfEvents > maxEventsPerCell) {
                            moreThanOneLeft = true;
                            break;
                        }
                    }
                    for(int dayOfWeek = 0; dayOfWeek < 7; dayOfWeek++) {
                        MonthView.MonthViewDay day = week.getDay(dayOfWeek);
                        if(moreThanOneLeft) {
                            writeMoreEventsLink(monthView, maxEventsPerCell, xhtmlBuffer, day, dayOfWeek);
                        } else {
                            writeEventCell(monthView, day, dayOfWeek, maxEventsPerCell - 1, xhtmlBuffer);
                        }
                    }
                    xhtmlBuffer.closeElement("tr");
                    %>
                </table>
            </div>
        <% } %>
    </div>
</div><%!
    protected int maxExtraEvents = 30;

    protected DateTimeFormatter getHoursMinutesFormatter() {
        return new DateTimeFormatterBuilder()
                .appendHourOfDay(2)
                .appendLiteral(":")
                .appendMinuteOfHour(2)
                .toFormatter()
                .withLocale(ElementsThreadLocals.getHttpServletRequest().getLocale());
    }

    private void writeMoreEventsLink
            (MonthView monthView, int maxEventsPerCell, XhtmlBuffer xhtmlBuffer,
             MonthView.MonthViewDay day, int dayOfWeek) {
        List<EventWeek> eventsOfTheDay = day.getSlots();
        xhtmlBuffer.openElement("td");
        xhtmlBuffer.openElement("div");
        xhtmlBuffer.addAttribute("class", "event");
        int more = 1 - maxEventsPerCell;
        for(EventWeek e : eventsOfTheDay) {
            if(e != null) {
                more++;
            }
        }
        String dialogId = null;
        if(more > 1) {
            int howMany = Math.min(eventsOfTheDay.size(), maxExtraEvents);
            String[] dialogIds = new String[howMany];
            DateTimeFormatter hhmmFormatter = getHoursMinutesFormatter();
            for(int i = maxEventsPerCell - 1; i < howMany; i++) {
                if(i >= eventsOfTheDay.size()) {
                    continue;
                }

                EventWeek eventWeek = eventsOfTheDay.get(i);
                if(eventWeek == null) {
                    continue;
                }
                Event event = eventWeek.getEvent();
                DateTime start = event.getInterval().getStart();
                if(day.getDayStart().isAfter(start)) {
                    start = day.getDayStart().toDateTime();
                }
                DateTime end = event.getInterval().getEnd();
                dialogIds[i] = writeEventDialog(
                    monthView, day, xhtmlBuffer, eventWeek, start, end, hhmmFormatter);
            }

            dialogId = "more-events-dialog-" + day.getDayStart().getMillis();
            PresentationHelper.openDialog(xhtmlBuffer, dialogId, null);

            // modal-header
            xhtmlBuffer.openElement("div");
            xhtmlBuffer.addAttribute("class", "modal-header");

            PresentationHelper.writeDialogCloseButtonInHeader(xhtmlBuffer);

            xhtmlBuffer.openElement("h1");
            xhtmlBuffer.addAttribute("class", "modal-title");
            xhtmlBuffer.write(ElementsThreadLocals.getText("more.events"));
            xhtmlBuffer.closeElement("h1");
            xhtmlBuffer.closeElement("div"); // modal-header


            // modal-body
            xhtmlBuffer.openElement("div");
            xhtmlBuffer.addAttribute("class", "modal-body");

            for(int i = 0; i < dialogIds.length; i++) {
                if(dialogIds[i] != null) {
                    writeEventDiv(dialogIds[i], monthView, day, dayOfWeek, i, xhtmlBuffer);
                }
            }
            xhtmlBuffer.closeElement("div"); // modal-body

            // modal-footer
            xhtmlBuffer.openElement("div");
            xhtmlBuffer.addAttribute("class", "modal-footer");
            PresentationHelper.writeDialogCloseButtonInFooter(xhtmlBuffer);
            xhtmlBuffer.closeElement("div"); // modal-footer

            PresentationHelper.closeDialog(xhtmlBuffer);
        } else if(more == 1) {
            EventWeek eventWeek = eventsOfTheDay.get(eventsOfTheDay.size() - 1);
            //Event content
            Event event = eventWeek.getEvent();
            DateTime start = event.getInterval().getStart();
            DateTime end = event.getInterval().getEnd();
            DateTimeFormatter hhmmFormatter = getHoursMinutesFormatter();
            dialogId = writeEventDialog(monthView, day, xhtmlBuffer,
                                        eventWeek, start, end, hhmmFormatter);

        }
        if(dialogId != null) {
            xhtmlBuffer.openElement("a");
            xhtmlBuffer.addAttribute("class", "more");
            xhtmlBuffer.addAttribute("href", "#");
            xhtmlBuffer.addAttribute("data-target", "#" + dialogId);
            xhtmlBuffer.addAttribute("data-toggle", "modal");
            xhtmlBuffer.write(MessageFormat.format(ElementsThreadLocals.getText("_.more"), more));
            xhtmlBuffer.closeElement("a");
        }
        xhtmlBuffer.closeElement("div");
        xhtmlBuffer.closeElement("td");
    }

    private String writeEventDialog
            (MonthView monthView, MonthView.MonthViewDay day, XhtmlBuffer xhtmlBuffer,
             EventWeek eventWeek, DateTime start, DateTime end, DateTimeFormatter hhmmFormatter) {
        Event event = eventWeek.getEvent();
        Locale locale = ElementsThreadLocals.getHttpServletRequest().getLocale();
        String dialogId = "event-dialog-" + event.getId();
        String dialogLabelId = "event-dialog-label-" + event.getId();

        PresentationHelper.openDialog(xhtmlBuffer, dialogId, dialogLabelId);

        // modal-header
        xhtmlBuffer.openElement("div");
        xhtmlBuffer.addAttribute("class", "modal-header");

        PresentationHelper.writeDialogCloseButtonInHeader(xhtmlBuffer);

        xhtmlBuffer.openElement("h1");
        xhtmlBuffer.addAttribute("id", dialogLabelId);
        xhtmlBuffer.addAttribute("class", "modal-title");
        if(event.getReadUrl() != null) {
            xhtmlBuffer.writeAnchor(event.getReadUrl(), event.getDescription());
        } else {
            xhtmlBuffer.write(event.getDescription());
        }
        xhtmlBuffer.closeElement("h1");
        xhtmlBuffer.closeElement("div"); // modal-header

        // modal-body
        //Print time interval
        xhtmlBuffer.openElement("div");
        xhtmlBuffer.addAttribute("class", "modal-body");
        xhtmlBuffer.openElement("p");
        String timeDescription;
        DateTimeFormatter startFormatter =
                makeEventDateTimeFormatter
                        (start, monthView.getMonthInterval(), locale);
        timeDescription = startFormatter.print(start);
        if(end.minus(1).getDayOfYear() != start.getDayOfYear()) {
            DateTime formatEnd = end;
            if(formatEnd.getMillisOfDay() == 0) {
                formatEnd = formatEnd.minusDays(1);
            }
            DateTimeFormatter endFormatter =
                makeEventDateTimeFormatter
                        (formatEnd, monthView.getMonthInterval(), locale);
            timeDescription += " - " + endFormatter.print(formatEnd);
        } else if(end.getMillisOfDay() != start.getMillisOfDay()) {
            timeDescription += " - " + hhmmFormatter.print(end);
        }
        xhtmlBuffer.write(timeDescription);
        xhtmlBuffer.closeElement("p");

        //Print edit link
        if(event.getEditUrl() != null) {
            xhtmlBuffer.openElement("p");
            String editText = ElementsThreadLocals.getText("edit");
            xhtmlBuffer.writeAnchor(event.getEditUrl(), editText);
            xhtmlBuffer.closeElement("p");
        }

        //Print calendar info
        xhtmlBuffer.openElement("p");
        xhtmlBuffer.addAttribute("style", "font-weight: bold; color: " + event.getCalendar().getForegroundHtmlColor());
        xhtmlBuffer.write(event.getCalendar().getName());
        xhtmlBuffer.closeElement("p");
        xhtmlBuffer.closeElement("div"); // modal-body

        // modal-footer
        xhtmlBuffer.openElement("div");
        xhtmlBuffer.addAttribute("class", "modal-footer");
        PresentationHelper.writeDialogCloseButtonInFooter(xhtmlBuffer);
        xhtmlBuffer.closeElement("div"); // modal-footer

        PresentationHelper.closeDialog(xhtmlBuffer);
        return dialogId;
    }

    private DateTimeFormatter makeEventDateTimeFormatter(DateTime start, Interval monthInterval, Locale locale) {
        DateTimeFormatterBuilder builder =
            new DateTimeFormatterBuilder()
                    .appendDayOfWeekShortText()
                    .appendLiteral(", ")
                    .appendDayOfMonth(1)
                    .appendLiteral(" ")
                    .appendMonthOfYearText();
        if(start.getYear() != monthInterval.getStart().getYear()) {
            builder.appendLiteral(" ").appendYear(4, 4);
        }
        if(start.getSecondOfDay() > 0) {
            builder
                .appendLiteral(", ")
                .appendHourOfDay(2)
                .appendLiteral(":")
                .appendMinuteOfHour(2);
        }
        return builder.toFormatter().withLocale(locale);
    }

    private void writeEventCell(
            MonthView monthView, MonthView.MonthViewDay day, int dayOfWeek, int index, XhtmlBuffer xhtmlBuffer) {
        String enclosingTag = "td";
        List<EventWeek> eventsOfTheDay = day.getSlots();
        if(index >= eventsOfTheDay.size()) {
            xhtmlBuffer.openElement(enclosingTag);
            xhtmlBuffer.closeElement(enclosingTag);
            return;
        }

        EventWeek eventWeek = eventsOfTheDay.get(index);
        if(eventWeek == null) {
            xhtmlBuffer.openElement(enclosingTag);
            xhtmlBuffer.closeElement(enclosingTag);
            return;
        }
        if(eventWeek.getStartDay() < dayOfWeek) {
            return;
        }
        xhtmlBuffer.openElement(enclosingTag);
        int days = eventWeek.getEndDay() + 1 - dayOfWeek;
        xhtmlBuffer.addAttribute("colspan", days + "");

        //Event content
        Event event = eventWeek.getEvent();
        DateTime start = event.getInterval().getStart();
        DateTime end = event.getInterval().getEnd();

        DateTimeFormatter hhmmFormatter = getHoursMinutesFormatter();

        //Dialog
        String dialogId = writeEventDialog(
                monthView, day, xhtmlBuffer, eventWeek, start, end, hhmmFormatter);

        //Cell contents
        xhtmlBuffer.openElement("div");
        xhtmlBuffer.addAttribute("class", "event");
        boolean eventLastsLessThanOneDay =
                days == 1 &&
                (event.getInterval().getStart().getMillisOfDay() != 0 ||
                 event.getInterval().getEnd().getMillisOfDay() != 0);
        if(!eventLastsLessThanOneDay) {
            xhtmlBuffer.addAttribute("style", "background-color: " + event.getCalendar().getBackgroundHtmlColor());
        }
        xhtmlBuffer.openElement("a");
        if(eventLastsLessThanOneDay) {
            xhtmlBuffer.addAttribute
                    ("style", "float: left; color: " + event.getCalendar().getForegroundHtmlColor());
        } else {
            xhtmlBuffer.addAttribute("style", "float: left;");
        }
        xhtmlBuffer.addAttribute("href", "#");
        xhtmlBuffer.addAttribute("data-target", "#" + dialogId);
        xhtmlBuffer.addAttribute("data-toggle", "modal");
//        xhtmlBuffer.addAttribute("onclick", "$('#" + dialogId + "').modal(); return false;");
        if(start.getMillisOfDay() > 0) {
            xhtmlBuffer.write(hhmmFormatter.print(start) + " ");
        }
        xhtmlBuffer.write(event.getDescription());
        xhtmlBuffer.closeElement("a");
        if(eventWeek.isContinues()) {
            xhtmlBuffer.openElement("div");
            xhtmlBuffer.addAttribute("style", "float: right; margin-right: 1em;");
            xhtmlBuffer.write(ElementsThreadLocals.getText("continues"));
            xhtmlBuffer.closeElement("div");
        }
        xhtmlBuffer.closeElement("div");

        xhtmlBuffer.closeElement(enclosingTag);
    }

    private void writeEventDiv(
            String dialogId, MonthView monthView, MonthView.MonthViewDay day, int dayOfWeek, int index, XhtmlBuffer xhtmlBuffer) {
        String enclosingTag = "div";
        List<EventWeek> eventsOfTheDay = day.getSlots();
        if(index >= eventsOfTheDay.size()) {
            return;
        }

        EventWeek eventWeek = eventsOfTheDay.get(index);
        if(eventWeek == null) {
            return;
        }
        xhtmlBuffer.openElement(enclosingTag);

        //Event content
        Event event = eventWeek.getEvent();
        DateTime start = event.getInterval().getStart();
        if(day.getDayStart().isAfter(start)) {
            start = day.getDayStart().toDateTime();
        }

        DateTimeFormatter hhmmFormatter = getHoursMinutesFormatter();

        //Cell contents
        xhtmlBuffer.openElement("div");
        xhtmlBuffer.openElement("a");
        xhtmlBuffer.addAttribute("style", "font-weight: bold; color: " + event.getCalendar().getForegroundHtmlColor());
        xhtmlBuffer.addAttribute("href", "#");
        xhtmlBuffer.addAttribute("data-target", "#" + dialogId);
        xhtmlBuffer.addAttribute("data-dismiss", "modal"); //Dismiss parent dialog
        xhtmlBuffer.addAttribute("data-toggle", "modal");
        if(start.getMillisOfDay() > 0) {
            xhtmlBuffer.write(hhmmFormatter.print(start) + " ");
        }
        xhtmlBuffer.write(event.getDescription());
        xhtmlBuffer.closeElement("a");
        xhtmlBuffer.closeElement("div");

        xhtmlBuffer.closeElement(enclosingTag);
    }

%>
