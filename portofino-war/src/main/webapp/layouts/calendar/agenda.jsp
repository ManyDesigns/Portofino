<%@ page import="com.manydesigns.elements.xml.XhtmlBuffer" %>
<%@ page import="com.manydesigns.portofino.pageactions.calendar.Event" %>
<%@ page import="com.manydesigns.portofino.pageactions.calendar.EventDay" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.joda.time.DateTime" %>
<%@ page import="org.joda.time.format.DateTimeFormatter" %>
<%@ page import="org.joda.time.format.DateTimeFormatterBuilder" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.calendar.CalendarAction"/>
<%
    DateTime referenceDateTime = actionBean.getReferenceDateTime();
    DateTimeFormatter dateFormatter =
            new DateTimeFormatterBuilder()
                    .appendDayOfWeekText()
                    .appendLiteral(" ")
                    .appendDayOfMonth(1)
                    .appendLiteral(" ")
                    .appendMonthOfYearText()
                    .appendLiteral(" ")
                    .appendYear(4, 4)
                    .toFormatter()
                    .withLocale(request.getLocale());

    DateTimeFormatter hhmmFormatter =
            new DateTimeFormatterBuilder()
                    .appendHourOfDay(2)
                    .appendLiteral(":")
                    .appendMinuteOfHour(2)
                    .toFormatter()
                    .withLocale(request.getLocale());
    XhtmlBuffer xhtmlBuffer = new XhtmlBuffer(out);
    ResourceBundle resourceBundle = actionBean.getApplication().getBundle(request.getLocale());
%>
<style type="text/css">
    .agenda-table {
        width: 100%;
        border: none;
    }
    .agenda-table td, .agenda-table th {
        border-style: none;
        border-top: 1px solid #DDDDDD;
    }
    .event-dialog {
        display: none;
    }
    .event {
        padding: 0.25em; font-weight: bold;
    }
    .date-cell {
        width: 15%; white-space: nowrap; text-align: left;
    }
    .hour-cell {
        width: 15%; white-space: nowrap;
    }
</style>
<div class="yui-g" style="width: 100%;">
    <div class="yui-u first">
        <%
            DateTime today = new DateTime();
            boolean todayDisabled =
                    referenceDateTime.getYear() == today.getYear() && referenceDateTime.getDayOfYear() == today.getDayOfYear();
        %>
        <button type="submit" name="today" <%= todayDisabled ? "disabled='true'" : "" %>
                class="ui-button ui-widget <%= todayDisabled ? "ui-state-disabled" : "ui-state-default" %> ui-corner-all ui-button-text-only ui-button">
            <span class="ui-button-text"><fmt:message key="calendar.today" /></span>
        </button>
        <button class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-icon-primary"
                type="submit" name="prevDay" role="button" aria-disabled="false">
            <span class="ui-button-icon-primary ui-icon ui-icon-carat-1-w"></span>
            <span class="ui-button-text"><fmt:message key="calendar.previous" /></span>
        </button>
        <button class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-icon-secondary"
                         type="submit" name="nextDay" role="button" aria-disabled="false">
            <span class="ui-button-text"><fmt:message key="calendar.next" /></span>
            <span class="ui-button-icon-secondary ui-icon ui-icon-carat-1-e"></span>
        </button>
    </div>
    <div class="yui-u" style="text-align: right">
        <button class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-icon-secondary"
                         type="submit" name="monthView" role="button" aria-disabled="false">
            <span class="ui-button-text"><fmt:message key="calendar.monthView" /></span>
            <span class="ui-button-icon-secondary ui-icon ui-icon-carat-1-e"></span>
        </button>
    </div>
</div>
<div style="white-space: nowrap; clear: both; margin-bottom: 1em;">
    <h3 style="margin: 0; text-align: center;"><%= StringUtils.capitalize(dateFormatter.print(referenceDateTime)) %></h3>
</div>
<script type="text/javascript">
    $(function() {
        $( ".event-dialog" ).dialog({ autoOpen: false });
    });
</script>
<div class="calendar-container">
    <table class="agenda-table">
    <%
        for(EventDay day : actionBean.getAgendaView().getEvents()) {
            xhtmlBuffer.openElement("tr");
            DateTimeFormatter dayFormatter =
                new DateTimeFormatterBuilder()
                    .appendDayOfWeekShortText()
                    .appendLiteral(" ")
                    .appendDayOfMonth(2)
                    .appendLiteral(" ")
                    .appendMonthOfYearShortText()
                    .toFormatter()
                    .withLocale(request.getLocale());
            writeDayCell(dayFormatter, xhtmlBuffer, day);
            boolean first = true;
            for(Event event : day.getEvents()) {
                if(!first) {
                    xhtmlBuffer.openElement("tr");
                }

                DateTime start = event.getInterval().getStart();
                DateTime end = event.getInterval().getEnd();

                writeEventSpanCell(hhmmFormatter, xhtmlBuffer, day, start, end, resourceBundle);
                writeEventCell(hhmmFormatter, xhtmlBuffer, day, event, start, end, resourceBundle);
                
                if(!first) {
                    xhtmlBuffer.closeElement("tr");
                }
                first = false;
            }
            xhtmlBuffer.closeElement("tr");
        }
    %>
    </table>
</div><%!
    private void writeEventCell
            (DateTimeFormatter hhmmFormatter, XhtmlBuffer xhtmlBuffer, EventDay day, Event event,
             DateTime start, DateTime end, ResourceBundle resourceBundle) {
        xhtmlBuffer.openElement("td");
        xhtmlBuffer.openElement("span");
        xhtmlBuffer.addAttribute("class", "event");

        String dialogId =
                writeEventDialog(hhmmFormatter, xhtmlBuffer, day, event, start, end, resourceBundle);

        xhtmlBuffer.openElement("a");
        xhtmlBuffer.addAttribute("style", "color: " + event.getCalendar().getForegroundHtmlColor() + ";");
        xhtmlBuffer.addAttribute("href", "#");
        xhtmlBuffer.addAttribute("onclick", "$('#" + dialogId + "').dialog('open'); return false;");
        xhtmlBuffer.write(event.getDescription());
        xhtmlBuffer.closeElement("a");
        xhtmlBuffer.closeElement("span");
        xhtmlBuffer.closeElement("td");
    }

    private String writeEventDialog
            (DateTimeFormatter hhmmFormatter, XhtmlBuffer xhtmlBuffer, EventDay day, Event event,
             DateTime start, DateTime end, ResourceBundle resourceBundle) {
        String dialogId = "event-dialog-" + event.getId() + "-" + day.getDay().getMillis();

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
                makeEventDateTimeFormatter(start, hhmmFormatter.getLocale());
        timeDescription = startFormatter.print(start);
        if(end.minus(1).getDayOfYear() != start.getDayOfYear()) {
            DateTime formatEnd = end;
            if(formatEnd.getMillisOfDay() == 0) {
                formatEnd = formatEnd.minusDays(1);
            }
            DateTimeFormatter endFormatter =
                makeEventDateTimeFormatter(formatEnd, hhmmFormatter.getLocale());
            timeDescription += " - " + endFormatter.print(formatEnd);
        } else if(end.getMillisOfDay() != start.getMillisOfDay()) {
            timeDescription += " - " + hhmmFormatter.print(end);
        }
        xhtmlBuffer.write(timeDescription);
        xhtmlBuffer.closeElement("p");
        if(event.getEditUrl() != null) {
            xhtmlBuffer.openElement("p");
            String editText = resourceBundle.getString("calendar.event.edit");
            xhtmlBuffer.writeAnchor(event.getEditUrl(), editText);
            xhtmlBuffer.closeElement("p");
        }
        xhtmlBuffer.closeElement("div");
        return dialogId;
    }

    private void writeEventSpanCell
            (DateTimeFormatter hhmmFormatter, XhtmlBuffer xhtmlBuffer,
             EventDay day, DateTime start, DateTime end, ResourceBundle resourceBundle) {
        xhtmlBuffer.openElement("td");
        xhtmlBuffer.addAttribute("class", "hour-cell");
        boolean startPrinted = false;
        boolean endPrinted = false;
        if(start.isAfter(day.getDay())) {
            xhtmlBuffer.write(hhmmFormatter.print(start));
            startPrinted = true;
        }
        if(end.isBefore(day.getDay().plusDays(1))) {
            if(startPrinted) {
                xhtmlBuffer.write(" - " + hhmmFormatter.print(end));
            } else {
                String msg = MessageFormat.format
                        (resourceBundle.getString("calendar.agenda.until"), hhmmFormatter.print(end));
                xhtmlBuffer.write(msg);
            }
            endPrinted = true;
        }
        if(!startPrinted && !endPrinted) {
            xhtmlBuffer.write(resourceBundle.getString("calendar.agenda.wholeday"));
        }
        xhtmlBuffer.closeElement("td");
    }

    private void writeDayCell(DateTimeFormatter dayFormatter, XhtmlBuffer xhtmlBuffer, EventDay day) {
        xhtmlBuffer.openElement("th");
        xhtmlBuffer.addAttribute("rowspan", day.getEvents().size() + "");
        xhtmlBuffer.addAttribute("class", "date-cell");
        xhtmlBuffer.write(dayFormatter.print(day.getDay()));
        xhtmlBuffer.closeElement("th");
    }

    private DateTimeFormatter makeEventDateTimeFormatter(DateTime start, Locale locale) {
        DateTimeFormatterBuilder builder =
            new DateTimeFormatterBuilder()
                    .appendDayOfWeekShortText()
                    .appendLiteral(", ")
                    .appendDayOfMonth(1)
                    .appendLiteral(" ")
                    .appendMonthOfYearText();
        if(start.getSecondOfDay() > 0) {
            builder
                .appendLiteral(", ")
                .appendHourOfDay(2)
                .appendLiteral(":")
                .appendMinuteOfHour(2);
        }
        return builder.toFormatter().withLocale(locale);
    }
%>