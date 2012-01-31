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
<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"%>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.calendar.CalendarAction"/>
<%
    int maxEventsPerCell = 3;
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
            boolean todayDisabled = monthView.getMonthInterval().contains(new DateTime());
        %>
        <button type="submit" name="today" <%= todayDisabled ? "disabled='true'" : "" %>
                class="ui-button ui-widget <%= todayDisabled ? "ui-state-disabled" : "ui-state-default" %> ui-corner-all ui-button-text-only ui-button">
            <span class="ui-button-text">Oggi</span>
        </button>
        <button class="ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only"
                type="submit" name="prevMonth" role="button" aria-disabled="false" title="Prev">
            <span class="ui-button-icon-primary ui-icon ui-icon-carat-1-w"></span>
            <span class="ui-button-text">Prev</span>
        </button><button class="ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only"
                         type="submit" name="nextMonth" role="button" aria-disabled="false" title="Next">
            <span class="ui-button-icon-primary ui-icon ui-icon-carat-1-e"></span>
            <span class="ui-button-text">Next</span>
        </button>
        <span style="margin-left: 1em;">
            <%= StringUtils.capitalize(monthFormatter.print(monthView.getReferenceDateTime())) %>
        </span>
    </div>
    <div class="yui-u" style="text-align: right">
        <div id="calendarViewType">
            <input type="radio" id="calendarViewType-month" name="calendarViewType" checked="checked" value="month"
                   /><label for="calendarViewType-month">Mese</label>
            <input type="radio" id="calendarViewType-agenda" name="calendarViewType" value="agenda"
                   /><label for="calendarViewType-agenda">Agenda</label>
        </div>
        <script>
            $(function() {
                $("#calendarViewType").buttonset();
                $("#calendarViewType-agenda").click(function() {
                    $(this).closest("form").submit();
                });
            });
        </script>
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
            MonthView.Week week = monthView.getWeek(index);
        %>
            <div class="calendar-row" style="top: <%= index * 100.0 / 6.0 %>%;">
                <table class="grid-table">
                    <tr>
                        <%
                        for(int i = 0; i < 7; i++) {
                            MonthView.Day day = week.getDay(i);
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
                            MonthView.Day day = week.getDay(i);
                            xhtmlBuffer.openElement("th");
                            if(!day.isInReferenceMonth()) {
                                xhtmlBuffer.addAttribute("class", "outOfMonth");
                            }
                            int dayOfMonth = day.getDayStart().dayOfMonth().get();
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
                            MonthView.Day day = week.getDay(dayOfWeek);
                            printEvent(day, dayOfWeek, row, xhtmlBuffer, request.getLocale());
                        }
                        xhtmlBuffer.closeElement("tr");
                    }

                    //Last row
                    xhtmlBuffer.openElement("tr");
                    boolean moreThanOneLeft = false;
                    for(int dayOfWeek = 0; dayOfWeek < 7; dayOfWeek++) {
                        MonthView.Day day = week.getDay(dayOfWeek);
                        List<EventWeek> eventsOfTheDay = day.getSlots();

                        if(eventsOfTheDay.size() > maxEventsPerCell) {
                            moreThanOneLeft = true;
                            break;
                        }
                    }
                    for(int dayOfWeek = 0; dayOfWeek < 7; dayOfWeek++) {
                        MonthView.Day day = week.getDay(dayOfWeek);
                        List<EventWeek> eventsOfTheDay = day.getSlots();

                        if(moreThanOneLeft) {
                            xhtmlBuffer.openElement("td");
                            if(!day.isInReferenceMonth()) {
                                xhtmlBuffer.addAttribute("class", "outOfMonth");
                            }
                            xhtmlBuffer.openElement("div");
                            xhtmlBuffer.addAttribute("class", "event");
                            int more = eventsOfTheDay.size() + 1 - maxEventsPerCell;
                            if(more > 0) {
                                xhtmlBuffer.write(more + " more events");
                            }
                            xhtmlBuffer.closeElement("div");
                            xhtmlBuffer.closeElement("td");
                        } else {
                            printEvent(day, dayOfWeek, maxEventsPerCell - 1, xhtmlBuffer, request.getLocale());
                        }
                    }
                    xhtmlBuffer.closeElement("tr");
                    %>
                </table>
            </div>
        <% } %>
    </div>
</div><%!
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

    private void printEvent(MonthView.Day day, int dayOfWeek, int index, XhtmlBuffer xhtmlBuffer, Locale locale) {
        List<EventWeek> eventsOfTheDay = day.getSlots();
        if(index >= eventsOfTheDay.size()) {
            xhtmlBuffer.openElement("td");
            xhtmlBuffer.closeElement("td");
            return;
        }

        EventWeek eventWeek = eventsOfTheDay.get(index);
        if(eventWeek == null) {
            xhtmlBuffer.openElement("td");
            xhtmlBuffer.closeElement("td");
            return;
        }
        if(eventWeek.getStartDay() < dayOfWeek) {
            return;
        }
        xhtmlBuffer.openElement("td");
        if(day.getDayInterval().contains(new DateTime())) {
            xhtmlBuffer.addAttribute("class", "today");
        }
        xhtmlBuffer.addAttribute("colspan", (eventWeek.getEndDay() + 1 - dayOfWeek) + "");
        if(!day.isInReferenceMonth()) {
            xhtmlBuffer.addAttribute("class", "outOfMonth");
        }

        Event event = eventWeek.getEvent();
        DateTime start = event.getInterval().getStart();
        DateTime end = event.getInterval().getEnd();

        DateTimeFormatter hhmmFormatter =
                new DateTimeFormatterBuilder()
                        .appendHourOfDay(2)
                        .appendLiteral(":")
                        .appendMinuteOfHour(2)
                        .toFormatter()
                        .withLocale(locale);

        //Dialog
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
        
        xhtmlBuffer.openElement("p");
        String timeDescription;
        DateTimeFormatter startFormatter =
                makeEventDateTimeFormatter
                        (start, day.getMonthView().getMonthInterval(), locale);
        timeDescription = startFormatter.print(start);
        if(end.minus(1).getDayOfYear() != start.getDayOfYear()) {
            DateTime formatEnd = end;
            if(formatEnd.getMillisOfDay() == 0) {
                formatEnd = formatEnd.minusDays(1);
            }
            DateTimeFormatter endFormatter =
                makeEventDateTimeFormatter
                        (formatEnd, day.getMonthView().getMonthInterval(), locale);
            timeDescription += " - " + endFormatter.print(formatEnd);
        } else if(end.getMillisOfDay() != start.getMillisOfDay()) {
            timeDescription += " - " + hhmmFormatter.print(end);
        }
        xhtmlBuffer.write(timeDescription);
        xhtmlBuffer.closeElement("p");
        if(event.getEditUrl() != null) {
            xhtmlBuffer.openElement("p");
            xhtmlBuffer.writeAnchor(event.getEditUrl(), "Edit"); //TODO i18n
            xhtmlBuffer.closeElement("p");
        }
        xhtmlBuffer.closeElement("div");

        //Cell contents
        xhtmlBuffer.openElement("div");
        xhtmlBuffer.addAttribute("class", "event");
        xhtmlBuffer.addAttribute("style", "background-color: " + event.getCalendar().getColor());
        xhtmlBuffer.openElement("a");
        xhtmlBuffer.addAttribute("href", "#");
        xhtmlBuffer.addAttribute("onclick", "$('#" + dialogId + "').dialog('open'); return false;");
        if(start.getMillisOfDay() > 0) {
            xhtmlBuffer.write(hhmmFormatter.print(start) + " ");
        }
        xhtmlBuffer.write(event.getDescription());
        xhtmlBuffer.closeElement("a");
        xhtmlBuffer.closeElement("div");
        xhtmlBuffer.closeElement("td");
    }

%>