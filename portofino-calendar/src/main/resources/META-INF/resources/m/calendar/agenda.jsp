<%@ page import="com.manydesigns.elements.ElementsThreadLocals" %>
<%@ page import="com.manydesigns.elements.xml.XhtmlBuffer" %>
<%@ page import="com.manydesigns.portofino.pageactions.calendar.Event" %>
<%@ page import="com.manydesigns.portofino.pageactions.calendar.EventDay" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.joda.time.DateTime" %>
<%@ page import="org.joda.time.format.DateTimeFormatter" %>
<%@ page import="org.joda.time.format.DateTimeFormatterBuilder" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="java.util.Locale" %>
<%@ page import="com.manydesigns.portofino.calendar.PresentationHelper" %>
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
<h1><%= StringUtils.capitalize(dateFormatter.print(referenceDateTime)) %></h1>
<div>
    <div class="pull-right" >
        <button type="submit" name="monthView" class="btn btn-default btn-sm">
            <span class="glyphicon glyphicon-calendar"></span>
            <fmt:message key="month" />
        </button>
    </div>
    <div>
        <%
            DateTime today = new DateTime();
            boolean todayDisabled =
                    referenceDateTime.getYear() == today.getYear() && referenceDateTime.getDayOfYear() == today.getDayOfYear();
        %>
        <button type="submit" name="today" class="btn btn-default btn-sm"<%= todayDisabled ? " disabled='true'" : "" %>>
            <fmt:message key="today" />
        </button>
        <button type="submit" name="prevDay" class="btn btn-default btn-sm">
            <em class="glyphicon glyphicon-chevron-left"></em>
            <fmt:message key="previous" />
        </button>
        <button type="submit" name="nextDay" class="btn btn-default btn-sm">
            <fmt:message key="next" />
            <em class="glyphicon glyphicon-chevron-right"></em>
        </button>
    </div>
</div>
<div class="horizontalSeparator"></div>
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

                writeEventSpanCell(hhmmFormatter, xhtmlBuffer, day, start, end);
                writeEventCell(hhmmFormatter, xhtmlBuffer, day, event, start, end);
                
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
             DateTime start, DateTime end) {
        xhtmlBuffer.openElement("td");
        xhtmlBuffer.openElement("span");
        xhtmlBuffer.addAttribute("class", "event");

        String dialogId =
                writeEventDialog(hhmmFormatter, xhtmlBuffer, day, event, start, end);

        xhtmlBuffer.openElement("a");
        xhtmlBuffer.addAttribute("style", "color: " + event.getCalendar().getForegroundHtmlColor() + ";");
        xhtmlBuffer.addAttribute("href", "#");
        xhtmlBuffer.addAttribute("data-target", "#" + dialogId);
        xhtmlBuffer.addAttribute("data-toggle", "modal");
        xhtmlBuffer.write(event.getDescription());
        xhtmlBuffer.closeElement("a");
        xhtmlBuffer.closeElement("span");
        xhtmlBuffer.closeElement("td");
    }

    private String writeEventDialog
            (DateTimeFormatter hhmmFormatter, XhtmlBuffer xhtmlBuffer, EventDay day, Event event,
             DateTime start, DateTime end) {
        String dialogId = "event-dialog-" + event.getId() + "-" + day.getDay().getMillis();

        PresentationHelper.openDialog(xhtmlBuffer, dialogId, null);

        // modal-header
        xhtmlBuffer.openElement("div");
        xhtmlBuffer.addAttribute("class", "modal-header");

        PresentationHelper.writeDialogCloseButtonInHeader(xhtmlBuffer);

        xhtmlBuffer.openElement("h1");
        xhtmlBuffer.addAttribute("class", "modal-title");
        if(event.getReadUrl() != null) {
            xhtmlBuffer.writeAnchor(event.getReadUrl(), event.getDescription());
        } else {
            xhtmlBuffer.write(event.getDescription());
        }
        xhtmlBuffer.closeElement("h1");
        xhtmlBuffer.closeElement("div"); // modal-header

        // modal-body
        xhtmlBuffer.openElement("div");
        xhtmlBuffer.addAttribute("class", "modal-body");

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
            String editText = ElementsThreadLocals.getText("edit");
            xhtmlBuffer.writeAnchor(event.getEditUrl(), editText);
            xhtmlBuffer.closeElement("p");
        }
        xhtmlBuffer.closeElement("div"); // modal-body

        // modal-footer
        xhtmlBuffer.openElement("div");
        xhtmlBuffer.addAttribute("class", "modal-footer");
        PresentationHelper.writeDialogCloseButtonInFooter(xhtmlBuffer);
        xhtmlBuffer.closeElement("div"); // modal-footer

        PresentationHelper.closeDialog(xhtmlBuffer);
        return dialogId;
    }

    private void writeEventSpanCell
            (DateTimeFormatter hhmmFormatter, XhtmlBuffer xhtmlBuffer,
             EventDay day, DateTime start, DateTime end) {
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
                        (ElementsThreadLocals.getText("until._"), hhmmFormatter.print(end));
                xhtmlBuffer.write(msg);
            }
            endPrinted = true;
        }
        if(!startPrinted && !endPrinted) {
            xhtmlBuffer.write(ElementsThreadLocals.getText("whole.day"));
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