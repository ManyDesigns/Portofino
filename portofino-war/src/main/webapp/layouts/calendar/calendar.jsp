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
        <% int maxEventsPerCell = 4; %>
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
        </style>
        <div class="calendar-container">
            <table class="days-table">
                <tr>
                    <th>Lun</th>
                    <th>Mar</th>
                    <th>Mer</th>
                    <th>Gio</th>
                    <th>Ven</th>
                    <th>Sab</th>
                    <th>Dom</th>
                </tr>
            </table>
            <div class="calendar-table">
                <c:forEach items="<%= new int[] { 0, 1, 2, 3, 4, 5 } %>" var="index">
                    <% int index = (Integer) pageContext.getAttribute("index"); %>
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
                                <th>1</th>
                                <th>2</th>
                                <th>3</th>
                                <th>4</th>
                                <th>5</th>
                                <th>6</th>
                                <th>7</th>
                            </tr>
                            <% for(int i = 0; i < maxEventsPerCell; i++) { %>
                                <tr>
                                    <td></td>
                                    <td colspan="2">
                                        <div class="event" style="background-color: #F0D0CE; border: 1px solid #DB7972;">Uno</div>
                                    </td>
                                    <td></td>
                                    <td><div class="event">Due</div></td>
                                    <td></td>
                                    <td></td>
                                </tr>
                            <% } %>
                        </table>
                    </div>
                </c:forEach>
            </div>
        </div>
    </stripes:layout-component>
    <stripes:layout-component name="portletFooter">
    </stripes:layout-component>
</stripes:layout-render>