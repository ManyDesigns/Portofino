<%@ page import="com.manydesigns.elements.xml.XhtmlBuffer" %>
<%@ page import="com.manydesigns.portofino.pageactions.calendar.Event" %>
<%@ page import="com.manydesigns.portofino.pageactions.calendar.EventWeek" %>
<%@ page import="com.manydesigns.portofino.pageactions.calendar.MonthView" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.joda.time.format.DateTimeFormatter" %>
<%@ page import="org.joda.time.format.DateTimeFormatterBuilder" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Locale" %>
<%@ page import="com.manydesigns.portofino.pageactions.calendar.EventDay" %>
<%@ page import="org.joda.time.*" %>
<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"%>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.calendar.CalendarAction"/>
<%
    DateTime referenceDateTime = actionBean.getReferenceDateTime();
    DateTimeFormatter monthFormatter =
            new DateTimeFormatterBuilder()
                    .appendMonthOfYearText()
                    .appendLiteral(" ")
                    .appendYear(4, 4)
                    .toFormatter()
                    .withLocale(request.getLocale());

    DateTimeFormatter dayFormatter =
            new DateTimeFormatterBuilder()
                    .appendDayOfWeekShortText()
                    .appendLiteral(" ")
                    .appendDayOfMonth(2)
                    .appendLiteral(" ")
                    .appendMonthOfYearText()
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
<div class="yui-gc" style="width: 100%;">
    <div class="yui-u first">
        <%
            DateTime today = new DateTime();
            boolean todayDisabled =
                    referenceDateTime.getYear() == today.getYear() && referenceDateTime.getDayOfYear() == today.getDayOfYear();
        %>
        <button type="submit" name="today" <%= todayDisabled ? "disabled='true'" : "" %>
                class="ui-button ui-widget <%= todayDisabled ? "ui-state-disabled" : "ui-state-default" %> ui-corner-all ui-button-text-only ui-button">
            <span class="ui-button-text">Oggi</span>
        </button>
        <button class="ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only"
                type="submit" name="prevDay" role="button" aria-disabled="false" title="Prev">
            <span class="ui-button-icon-primary ui-icon ui-icon-carat-1-w"></span>
            <span class="ui-button-text">Prev</span>
        </button><button class="ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only"
                         type="submit" name="nextDay" role="button" aria-disabled="false" title="Next">
            <span class="ui-button-icon-primary ui-icon ui-icon-carat-1-e"></span>
            <span class="ui-button-text">Next</span>
        </button>
        <span style="margin-left: 1em;">
            <%= StringUtils.capitalize(monthFormatter.print(referenceDateTime)) %>
        </span>
    </div>
    <div class="yui-u" style="text-align: right">
        <div id="calendarViewType">
            <input type="radio" id="calendarViewType-month" name="calendarViewType" value="month"
                   /><label for="calendarViewType-month">Mese</label>
            <input type="radio" id="calendarViewType-agenda" name="calendarViewType" checked="checked" value="agenda"
                   /><label for="calendarViewType-agenda">Agenda</label>
        </div>
        <script>
            $(function() {
                $("#calendarViewType").buttonset();
                $("#calendarViewType-month").click(function() {
                    $(this).closest("form").submit();
                });
            });
        </script>
    </div>
</div>
<div class="horizontalSeparator"></div>
<div class="calendar-container">
    <table style="width: 100%; border: none;">
    <%
        for(EventDay day : actionBean.getAgendaView().getEvents()) {
            xhtmlBuffer.openElement("tr");
            xhtmlBuffer.openElement("td");
            xhtmlBuffer.addAttribute("rowspan", day.getEvents().size() + "");
            xhtmlBuffer.write(dayFormatter.print(day.getDay()));
            xhtmlBuffer.closeElement("td");
            boolean first = true;
            for(Event event : day.getEvents()) {
                if(!first) {
                    xhtmlBuffer.openElement("tr");
                }
                xhtmlBuffer.openElement("td");
                DateTime start = event.getInterval().getStart();
                DateTime end = event.getInterval().getEnd();
                if(start.isAfter(day.getDay())) {
                    xhtmlBuffer.write(hhmmFormatter.print(start));
                }
                if(end.isBefore(day.getDay().plusDays(1))) { //TODO
                    xhtmlBuffer.write(" - " + hhmmFormatter.print(end));
                }
                xhtmlBuffer.closeElement("td");
                xhtmlBuffer.openElement("td");
                xhtmlBuffer.write(event.getDescription());
                xhtmlBuffer.closeElement("td");
                if(!first) {
                    xhtmlBuffer.closeElement("tr");
                }
                first = false;
            }
            xhtmlBuffer.closeElement("tr");
        }
    %>
    </table>
</div>