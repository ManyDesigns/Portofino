<%@ page import="com.manydesigns.elements.xml.XhtmlBuffer" %>
<%@ page import="com.manydesigns.portofino.pageactions.calendar.Event" %>
<%@ page import="com.manydesigns.portofino.pageactions.calendar.EventWeek" %>
<%@ page import="com.manydesigns.portofino.pageactions.calendar.MonthView" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.joda.time.DateMidnight" %>
<%@ page import="org.joda.time.DateTime" %>
<%@ page import="org.joda.time.format.DateTimeFormatter" %>
<%@ page import="org.joda.time.format.DateTimeFormatterBuilder" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Locale" %>
<%@ page import="org.joda.time.Interval" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="java.text.MessageFormat" %>
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
    ResourceBundle resourceBundle = actionBean.getApplication().getBundle(request.getLocale());
%>
<style type="text/css">
    .calendar-container {
        position: relative; height: <%= maxEventsPerCell * 120 + 120 %>px;
    }
    .days-table td, .days-table th {
        margin: 0; padding: 0 0 0 10px; border: none; text-align: left;
    }
    .calendar-table {
        position: absolute; width: 100%; margin-top: 10px; height: 100%;
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
        padding: 0 0 0 8px; white-space: nowrap; overflow: hidden;
    }
    .event a {
        color: #222222;
    }
    .outOfMonth {
        color: #BBBBBB;
    }
    .event-dialog {
        display: none;
    }
</style>
<script type="text/javascript">
    $(function() {
        $( ".event-dialog" ).dialog({ autoOpen: false });
    });
</script>
<div class="yui-gc" style="width: 100%;">
    <div class="yui-u first">
        <%
            Interval monthInterval = monthView.getMonthInterval();
            boolean todayDisabled = monthInterval.contains(new DateTime());
        %>
        <button type="submit" name="today" <%= todayDisabled ? "disabled='true'" : "" %>
                class="ui-button ui-widget <%= todayDisabled ? "ui-state-disabled" : "ui-state-default" %> ui-corner-all ui-button-text-only">
            <span class="ui-button-text"><fmt:message key="calendar.currentMonth" /></span>
        </button>
        <button class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-icon-primary"
                type="submit" name="prevMonth" role="button" aria-disabled="false">
            <span class="ui-button-icon-primary ui-icon ui-icon-carat-1-w"></span>
            <span class="ui-button-text"><fmt:message key="calendar.previous" /></span>
        </button>
        <button class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-icon-secondary"
                         type="submit" name="nextMonth" role="button" aria-disabled="false">
            <span class="ui-button-text"><fmt:message key="calendar.next" /></span>
            <span class="ui-button-icon-secondary ui-icon ui-icon-carat-1-e"></span>
        </button>
        <span style="margin-left: 1em;">
            <h3 style="margin: 0; display: inline;"><%= StringUtils.capitalize(monthFormatter.print(monthView.getReferenceDateTime())) %></h3>
        </span>
    </div>
    <div class="yui-u" style="text-align: right">
        <button class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-icon-secondary"
                         type="submit" name="agendaView" role="button" aria-disabled="false">
            <span class="ui-button-text"><fmt:message key="calendar.agendaView" /></span>
            <span class="ui-button-icon-secondary ui-icon ui-icon-carat-1-e"></span>
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
                            writeEventCell(monthView, day, dayOfWeek, row, xhtmlBuffer, resourceBundle);
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
                            writeMoreEventsLink(monthView, maxEventsPerCell, xhtmlBuffer, day, dayOfWeek, resourceBundle);
                        } else {
                            writeEventCell(monthView, day, dayOfWeek, maxEventsPerCell - 1, xhtmlBuffer, resourceBundle);
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

    protected DateTimeFormatter getHoursMinutesFormatter(ResourceBundle resourceBundle) {
        return new DateTimeFormatterBuilder()
                .appendHourOfDay(2)
                .appendLiteral(":")
                .appendMinuteOfHour(2)
                .toFormatter()
                .withLocale(resourceBundle.getLocale());
    }

    private void writeMoreEventsLink
            (MonthView monthView, int maxEventsPerCell, XhtmlBuffer xhtmlBuffer, MonthView.MonthViewDay day, int dayOfWeek,
             ResourceBundle resourceBundle) {
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
            dialogId = "more-events-dialog-" + day.getDayStart().getMillis();
            xhtmlBuffer.openElement("div");
            xhtmlBuffer.addAttribute("id", dialogId);
            xhtmlBuffer.addAttribute("class", "event-dialog");
            int howMany = Math.min(eventsOfTheDay.size(), maxExtraEvents);
            for(int i = maxEventsPerCell - 1; i < howMany; i++) {
                writeEventDiv(monthView, day, dayOfWeek, i, xhtmlBuffer, resourceBundle);
            }
            xhtmlBuffer.closeElement("div");
        } else if(more == 1) {
            EventWeek eventWeek = eventsOfTheDay.get(eventsOfTheDay.size() - 1);
            //Event content
            Event event = eventWeek.getEvent();
            DateTime start = event.getInterval().getStart();
            DateTime end = event.getInterval().getEnd();
            DateTimeFormatter hhmmFormatter = getHoursMinutesFormatter(resourceBundle);
            dialogId = writeEventDialog(monthView, day, xhtmlBuffer, resourceBundle,
                                        eventWeek, start, end, hhmmFormatter);

        }
        if(dialogId != null) {
            xhtmlBuffer.openElement("a");
            xhtmlBuffer.addAttribute("href", "#");
            xhtmlBuffer.addAttribute("onclick", "$('#" + dialogId + "').dialog('open'); return false;");
            xhtmlBuffer.write(MessageFormat.format(resourceBundle.getString("calendar.moreEvents"), more));
            xhtmlBuffer.closeElement("a");
        }
        xhtmlBuffer.closeElement("div");
        xhtmlBuffer.closeElement("td");
    }

    private String writeEventDialog
            (MonthView monthView, MonthView.MonthViewDay day, XhtmlBuffer xhtmlBuffer, ResourceBundle resourceBundle,
             EventWeek eventWeek, DateTime start, DateTime end, DateTimeFormatter hhmmFormatter) {
        Event event = eventWeek.getEvent();
        Locale locale = resourceBundle.getLocale();
        String dialogId = "event-dialog-" + event.getId();

        xhtmlBuffer.openElement("div");
        xhtmlBuffer.addAttribute("id", dialogId);
        xhtmlBuffer.addAttribute("class", "event-dialog");
        xhtmlBuffer.openElement("h3");
        if(event.getReadUrl() != null) {
            xhtmlBuffer.writeAnchor(event.getReadUrl(), event.getDescription());
        } else {
            xhtmlBuffer.write(event.getDescription());
        }
        xhtmlBuffer.closeElement("h3");

        //Print time interval
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
            String editText = resourceBundle.getString("calendar.event.edit");
            xhtmlBuffer.writeAnchor(event.getEditUrl(), editText);
            xhtmlBuffer.closeElement("p");
        }

        //Print calendar info
        xhtmlBuffer.openElement("p");
        xhtmlBuffer.addAttribute("style", "font-weight: bold; color: " + event.getCalendar().getForegroundHtmlColor());
        xhtmlBuffer.write(event.getCalendar().getName());
        xhtmlBuffer.closeElement("p");

        xhtmlBuffer.closeElement("div");
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

    private void writeEventCell
            (MonthView monthView, MonthView.MonthViewDay day, int dayOfWeek, int index, XhtmlBuffer xhtmlBuffer, ResourceBundle resourceBundle) {
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

        DateTimeFormatter hhmmFormatter = getHoursMinutesFormatter(resourceBundle);

        //Dialog
        String dialogId = writeEventDialog
                (monthView, day, xhtmlBuffer, resourceBundle, eventWeek, start, end, hhmmFormatter);

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
        xhtmlBuffer.addAttribute("onclick", "$('#" + dialogId + "').dialog('open'); return false;");
        if(start.getMillisOfDay() > 0) {
            xhtmlBuffer.write(hhmmFormatter.print(start) + " ");
        }
        xhtmlBuffer.write(event.getDescription());
        xhtmlBuffer.closeElement("a");
        if(eventWeek.isContinues()) {
            xhtmlBuffer.openElement("div");
            xhtmlBuffer.addAttribute("style", "float: right; margin-right: 1em;");
            xhtmlBuffer.write(resourceBundle.getString("calendar.event.continues"));
            xhtmlBuffer.closeElement("div");
        }
        xhtmlBuffer.closeElement("div");

        xhtmlBuffer.closeElement(enclosingTag);
    }

    private void writeEventDiv
            (MonthView monthView, MonthView.MonthViewDay day, int dayOfWeek, int index, XhtmlBuffer xhtmlBuffer, ResourceBundle resourceBundle) {
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
        DateTime end = event.getInterval().getEnd();

        DateTimeFormatter hhmmFormatter = getHoursMinutesFormatter(resourceBundle);

        //Dialog
        String dialogId = writeEventDialog
                (monthView, day, xhtmlBuffer, resourceBundle, eventWeek, start, end, hhmmFormatter);

        //Cell contents
        xhtmlBuffer.openElement("div");
        xhtmlBuffer.openElement("a");
        xhtmlBuffer.addAttribute("style", "font-weight: bold; color: " + event.getCalendar().getForegroundHtmlColor());
        xhtmlBuffer.addAttribute("href", "#");
        xhtmlBuffer.addAttribute("onclick", "$('#" + dialogId + "').dialog('open'); return false;");
        if(start.getMillisOfDay() > 0) {
            xhtmlBuffer.write(hhmmFormatter.print(start) + " ");
        }
        xhtmlBuffer.write(event.getDescription());
        xhtmlBuffer.closeElement("a");
        xhtmlBuffer.closeElement("div");

        xhtmlBuffer.closeElement(enclosingTag);
    }

%>