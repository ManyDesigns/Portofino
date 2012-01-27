<%@ page import="com.manydesigns.elements.xml.XhtmlBuffer" %>
<%@ page import="com.manydesigns.portofino.pageactions.calendar.MonthView" %>
<%@ page import="org.joda.time.DateMidnight" %>
<%@ page import="org.joda.time.DateTime" %>
<%@ page import="org.joda.time.format.DateTimeFormatter" %>
<%@ page import="org.joda.time.format.DateTimeFormatterBuilder" %>
<%@ page import="com.manydesigns.portofino.pageactions.calendar.EventWeek" %>
<%@ page import="com.manydesigns.portofino.pageactions.calendar.Event" %>
<%@ page import="com.manydesigns.portofino.pageactions.calendar.Calendar" %>
<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"%>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.calendar.CalendarAction"/>
<stripes:layout-render name="/skins/${skin}/portlet.jsp">
    <stripes:layout-component name="portletTitle">
        <c:out value="${actionBean.page.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <%
            int maxEventsPerCell = 1;
            MonthView monthView = new MonthView(new DateTime());
            DateMidnight monthStart = monthView.getMonthStart();
            DateMidnight monthEnd = monthView.getMonthEnd();
            DateTimeFormatter dayOfWeekFormatter =
                    new DateTimeFormatterBuilder()
                            .appendDayOfWeekShortText()
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
            }
            .events-table {
                position: relative; border: none;
            }
            .events-table td {
                padding: 1px; border: none;
            }
            .events-table th {
                padding: 0 0 3px 10px; border: none; text-align: left;
            }
            .event {
                padding: 0 0 0 8px;
            }
            .outOfMonth {
                color: #BBBBBB;
            }
        </style>
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
                                <td></td>
                                <td></td>
                                <td></td>
                                <td></td>
                                <td></td>
                                <td></td>
                                <td></td>
                            </tr>
                        </table>
                        <table class="events-table">
                            <tr>
                                <%
                                for(int i = 0; i < 7; i++) {
                                    MonthView.Day day = week.getDay(i);
                                    boolean inMonth =
                                            day.getDayStart().compareTo(monthStart) >= 0 &&
                                            day.getDayEnd().compareTo(monthEnd) <= 0;
                                    xhtmlBuffer.openElement("th");
                                    if(!inMonth) {
                                        xhtmlBuffer.addAttribute("class", "outOfMonth");
                                    }
                                    xhtmlBuffer.write(day.getDayStart().dayOfMonth().get() + "");
                                    xhtmlBuffer.closeElement("th");
                                } %>
                            </tr>
                            <tr>
                                <%
                                for(int row = 0; row < maxEventsPerCell - 1; row++) {
                                    for(int dayOfWeek = 0; dayOfWeek < 7; dayOfWeek++) {
                                        MonthView.Day day = week.getDay(dayOfWeek);

                                        EventWeek[] eventsOfTheDay = getEventsOfTheDay();

                                        if(row < eventsOfTheDay.length) {
                                            printEvent(day, dayOfWeek, row, xhtmlBuffer);
                                        }
                                    }
                                }
                                //Last row
                                boolean moreThanOneLeft = false;
                                for(int dayOfWeek = 0; dayOfWeek < 7; dayOfWeek++) {
                                    MonthView.Day day = week.getDay(dayOfWeek);

                                    EventWeek[] eventsOfTheDay = getEventsOfTheDay();
                                    if(eventsOfTheDay.length > maxEventsPerCell) {
                                        moreThanOneLeft = true;
                                        break;
                                    }
                                }
                                for(int dayOfWeek = 0; dayOfWeek < 7; dayOfWeek++) {
                                    MonthView.Day day = week.getDay(dayOfWeek);

                                    EventWeek[] eventsOfTheDay = getEventsOfTheDay();

                                    if(moreThanOneLeft) {
                                        boolean inMonth =
                                                day.getDayStart().compareTo(monthStart) >= 0 &&
                                                day.getDayEnd().compareTo(monthEnd) <= 0;

                                        xhtmlBuffer.openElement("td");
                                        if(!inMonth) {
                                            xhtmlBuffer.addAttribute("class", "outOfMonth");
                                        }
                                        xhtmlBuffer.openElement("div");
                                        xhtmlBuffer.addAttribute("class", "event");
                                        xhtmlBuffer.write(eventsOfTheDay.length + 1 - maxEventsPerCell + " more events");
                                        xhtmlBuffer.closeElement("div");
                                        xhtmlBuffer.closeElement("td");
                                    } else {
                                        printEvent(day, dayOfWeek, maxEventsPerCell - 1, xhtmlBuffer);
                                    }
                                }
                                %>
                            </tr>
                        </table>
                    </div>
                <% } %>
            </div>
        </div>
    </stripes:layout-component>
    <stripes:layout-component name="portletFooter">
    </stripes:layout-component>
</stripes:layout-render><%!
    private void printEvent(MonthView.Day day, int dayOfWeek, int index, XhtmlBuffer xhtmlBuffer) {
        boolean inMonth = true; //TODO
        EventWeek[] eventsOfTheDay = getEventsOfTheDay();
        EventWeek event = eventsOfTheDay[index];

        if(event == null) {
            xhtmlBuffer.write("<td></td>");
            return;
        }
        if(event.getStartDay() < dayOfWeek) {
            return;
        }
        xhtmlBuffer.openElement("td");
        xhtmlBuffer.addAttribute("colspan", (event.getEndDay() + 1 - dayOfWeek) + "");
        if(!inMonth) {
            xhtmlBuffer.addAttribute("class", "outOfMonth");
        }
        xhtmlBuffer.openElement("div");
        xhtmlBuffer.addAttribute("class", "event");
        xhtmlBuffer.write(event.getEvent().getDescription());
        xhtmlBuffer.closeElement("div");
        xhtmlBuffer.closeElement("td");
    }

    private EventWeek[] getEventsOfTheDay() {
        Calendar testCal1 = new Calendar("testCal1", "testCal1", "FF0000");
        Event testEvt1 = new Event(testCal1);
        testEvt1.setDescription("Test evt 1");
        EventWeek testEw1 = new EventWeek(testEvt1);
        testEw1.setStartDay(5);
        testEw1.setEndDay(6);
        testEw1.setContinues(true);
        return new EventWeek[] { testEw1 };
    }
%>