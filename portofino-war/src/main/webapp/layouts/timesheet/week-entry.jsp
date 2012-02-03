<%@ page import="com.manydesigns.elements.xml.XhtmlBuffer" %>
<%@ page import="com.manydesigns.portofino.pageactions.timesheet.model.Activity" %>
<%@ page import="com.manydesigns.portofino.pageactions.timesheet.model.Entry" %>
<%@ page import="com.manydesigns.portofino.pageactions.timesheet.model.PersonDay" %>
<%@ page
        import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.joda.time.DateMidnight" %>
<%@ page
        import="org.joda.time.Period" %>
<%@ page import="org.joda.time.format.DateTimeFormat" %>
<%@ page import="org.joda.time.format.DateTimeFormatter" %>
<%@ page import="org.joda.time.format.PeriodFormatter" %>
<%@ page import="org.joda.time.format.PeriodFormatterBuilder" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino" %>
<stripes:layout-render name="/skins/${skin}/modal-page.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.timesheet.TimesheetAction"/>
    <stripes:layout-component name="contentHeader">
        <portofino:buttons list="timesheet-week-entry" cssClass="contentButton" />
        <jsp:include page="/skins/${skin}/breadcrumbs.jsp" />
    </stripes:layout-component>
    <stripes:layout-component name="portletTitle">
        <c:out value="${actionBean.page.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <style type="text/css">
            div.twe-container {
                overflow-x: auto;
            }
            table.twe-table {
                width: 100%;
            }
            table.twe-table col.day-column {
                width: 60px;
            }
            table.twe-table th, table.twe-table td {
                border-color: #dddddd;
            }
            th.twe-activity {
                background-color: #567050;
                color: white;
            }
            th.twe-day {
                background-color: #94B47B;
                color: white;
            }
            th.twe-day.twe-today {
                background-color: #B4C97E;
            }
            th.twe-day.twe-non-working {
                background-color: #74936C;
            }
            table.twe-table tr.even {
                background-color: #FCFAF3
            }
            td.twe-hours.twe-today {
                background-color: #FFF2B5;
            }
            td.twe-hours.twe-non-working {
                background-color: #F0F8E5;
            }
            td.twe-hours input.twe-input {
                display: block;
                width: 30px;
                text-align: right;
            }
            table.twe-table tfoot tr {
                background-color: #F0EAD7;
                color: #625644;
                font-weight: bold;
            }
            td.twe-total {
                text-align: right;
            }
            td.twe-summary.twe-today {
                background-color: #F9EFBE;
            }
            span.twe-ro-hours, span.twe-day-total {
                display: block;
                width: 32px;
                text-align: right;
            }
            span.twe-week-total {
                color: black;
            }
            .display-none {
                display: none;
            }
        </style>
        <div class="twe-container">
        <table class="twe-table">
            <col/>
            <col/>
            <col/>
            <col class="day-column"/>
            <col class="day-column"/>
            <col class="day-column"/>
            <col class="day-column"/>
            <col class="day-column"/>
            <col class="day-column"/>
            <col class="day-column"/>
            <thead>
            <tr>
                <th class="twe-activity">Commessa</th>
                <th class="twe-activity">Progetto</th>
                <th class="twe-activity">Attività</th>
                <%
                    XhtmlBuffer xb = new XhtmlBuffer(out);
                    Set<DateMidnight> nonWorkingDays =
                            actionBean.getNonWorkingDays();

                    DateMidnight day = actionBean.getMonday();
                    DateTimeFormatter dayOfWeekFormatter =
                    DateTimeFormat.forPattern("E").withLocale(Locale.ITALY);
                    DateTimeFormatter dateFormatter =
                    DateTimeFormat.shortDate().withLocale(Locale.ITALY);
                    Map<DateMidnight,PersonDay> personDays =
                            actionBean.getPersonDays();
                    for (int i = 0; i < 7; i++) {
                        PersonDay personDay = personDays.get(day);
                        xb.openElement("th");
                        String htmlClass = "twe-day";
                        if (nonWorkingDays.contains(day)) {
                            htmlClass += " twe-non-working";
                        }

                        if (day.equals(actionBean.getToday())) {
                            htmlClass += " twe-today";
                        }

                        xb.addAttribute("class", htmlClass);


                        xb.openElement("div");
                        xb.write(dayOfWeekFormatter.print(day));
                        if (personDay != null && personDay.isLocked()) {
                            xb.openElement("span");
                            xb.addAttribute("class", "ui-icon ui-icon-locked");
                            xb.addAttribute("style", "float: right");
                            xb.addAttribute("title", "locked");
                            xb.closeElement("span");
                        }
                        xb.closeElement("div");

                        xb.write(dateFormatter.print(day));

                        if (personDay != null) {
                            String id = "swm-" + i;
                            Integer standardWorkingMinutes =
                                    personDay.getStandardWorkingMinutes();
                            String value = (standardWorkingMinutes == null)
                                    ? "-"
                                    : Integer.toString(standardWorkingMinutes);
                            xb.writeInputHidden(id, id, value);
                        }

                        xb.closeElement("th");
                        day = day.plusDays(1);
                    }
                %>
            </tr>
            </thead>
            <tbody>
            <%
                PeriodFormatter periodFormatter = new PeriodFormatterBuilder()
                        .printZeroAlways()
                        .appendHours()
                        .appendLiteral(":")
                        .minimumPrintedDigits(2)
                        .appendMinutes()
                        .toFormatter();
                boolean even = false;
                List<Activity> weekActivities = actionBean.getWeekActivities();
                int weekActivitiesSize = weekActivities.size();
                for (int activityIndex = 0; activityIndex < weekActivitiesSize; activityIndex++) {
                    Activity activity = weekActivities.get(activityIndex);
                    xb.openElement("tr");
                    if (even) {
                        xb.addAttribute("class", "even");
                        even = false;
                    } else {
                        xb.addAttribute("class", "odd");
                        even = true;
                    }

                    xb.openElement("td");
                    xb.write(activity.getGroup1());
                    xb.closeElement("td");

                    xb.openElement("td");
                    xb.write(activity.getGroup2());
                    xb.closeElement("td");

                    xb.openElement("td");
                    String description = activity.getDescription();
                    if (StringUtils.isNotBlank(description)) {
                        xb.openElement("a");
                        String acInfoId = "ac-info-" + activity.getId();
                        xb.addAttribute("onclick", "$( '#" + acInfoId + "' ).dialog(); return false;");
                        xb.addAttribute("href", "#");

                        xb.openElement("div");
                        xb.addAttribute("class", "ui-icon ui-icon-info");
                        xb.addAttribute("style", "float: right");
                        xb.closeElement("div");

                        xb.closeElement("a");

                        xb.openElement("div");
                        xb.addAttribute("id", acInfoId);
                        xb.addAttribute("class", "display-none");
                        xb.write(description);
                        xb.closeElement("div");
                    }
                    String readUrl = activity.getReadUrl();
                    if (readUrl == null) {
                        xb.write(activity.getTitle());
                    } else {
                        xb.writeAnchor(readUrl, activity.getTitle());
                    }
                    xb.closeElement("td");

                    day = actionBean.getMonday();
                    for (int dayIndex = 0; dayIndex < 7; dayIndex++) {
                        xb.openElement("td");
                        String htmlClass = "twe-hours";
                        if (nonWorkingDays.contains(day)) {
                            htmlClass += " twe-non-working";
                        }

                        if (day.equals(actionBean.getToday())) {
                            htmlClass += " twe-today";
                        }

                        xb.addAttribute("class", htmlClass);

                        PersonDay personDay = personDays.get(day);
                        if (personDay == null) {
                            //xb.write("--");
                        } else {
                            String hours = null;
                            Entry entry = null;
                            for (Entry current : personDay.getEntries()) {
                                if (activity == current.getActivity()) {
                                    entry = current;
                                    break;
                                }
                            }
                            String note = null;
                            if (entry != null) {
                                Period period = entry.getPeriod();
                                hours = periodFormatter.print(period);
                                note = entry.getNote();
                            }
                            if (personDay.isLocked()) {
                                if (hours == null) {
                                    xb.openElement("span");
                                    xb.addAttribute("class", "twe-ro-hours");
                                    xb.write("--");
                                    xb.closeElement("span");
                                } else {
                                    if (StringUtils.isNotBlank(note)) {
                                        xb.openElement("div");
                                        xb.addAttribute("class", "ui-icon ui-icon-comment");
                                        xb.addAttribute("style", "float: right");
                                        xb.addAttribute("title", note);
                                        xb.closeElement("div");
                                    }
                                    xb.openElement("span");
                                    xb.addAttribute("class", "twe-ro-hours");
                                    xb.write(hours);
                                    xb.closeElement("span");
                                }
                            } else {
                                if (StringUtils.isBlank(note)) {
                                    xb.openElement("div");
                                    xb.addAttribute("class", "ui-icon ui-icon-document");
                                    xb.addAttribute("style", "float: right");
                                    xb.closeElement("div");
                                } else {
                                    xb.openElement("div");
                                    xb.addAttribute("class", "ui-icon ui-icon-comment");
                                    xb.addAttribute("style", "float: right");
                                    xb.addAttribute("title", note);
                                    xb.closeElement("div");
                                }
                                String name = String.format("cell-%d-%s",
                                        dayIndex, activity.getId());
                                int tabIndex = activityIndex +
                                        dayIndex * weekActivitiesSize;
                                xb.openElement("span");
                                xb.openElement("input");
                                xb.addAttribute("type", "text");
                                xb.addAttribute("name", name);
                                xb.addAttribute("class", "twe-input");
                                xb.addAttribute("value", hours);
                                xb.addAttribute("tabindex", Integer.toString(tabIndex));
                                xb.closeElement("input");
                                xb.closeElement("span");
                            }
                        }

                        xb.closeElement("td");
                        day = day.plusDays(1);
                    }

                    xb.closeElement("tr");
                }
            %>
            </tbody>
            <tfoot>
            <tr>
                <td class="twe-total" colspan="3">Totale ore settimanali: <span id="twe-week-total" class="twe-week-total"></span></td>
                <%
                    day = actionBean.getMonday();
                    for (int i = 0; i < 7; i++) {
                        xb.openElement("td");
                        String htmlClass = "twe-summary";
                        if (nonWorkingDays.contains(day)) {
                            htmlClass += " twe-non-working";
                        }

                        if (day.equals(actionBean.getToday())) {
                            htmlClass += " twe-today";
                        }

                        xb.addAttribute("class", htmlClass);

                        xb.openElement("span");
                        xb.addAttribute("class", "twe-day-total");
                        xb.closeElement("span");

                        xb.closeElement("td");
                        day = day.plusDays(1);
                    }
                %>
            </tr>
            </tfoot>
        </table>
    </div>
    <input type="hidden" name="weeksAgo" value="<c:out value="${actionBean.weeksAgo}"/>"/>
    <script type="text/javascript">
        var hoursRe = /^(\d+):(\d+)$/;
        var hoursRe2 = /^(\d+):?$/;

        function parseHours(text) {
            var match = hoursRe.exec(text);
            if (match) {
                return parseInt(match[1]) * 60 + parseInt(match[2]);
            } else {
                return 0;
            }
        }
        function formatHoursMinutes(hours, minutes) {
            var totalTime = "" + hours + ":";
            if (minutes < 10) {
                totalTime += "0";
            }
            totalTime += minutes;
            return totalTime;
        }
        function updateDayTotal(index) {
            var totalMinutes = 0;
            $("table.twe-table tbody td:nth-child(" + (4 + index) + ")").each( function(tdIndex, currentTd) {
                $(currentTd).find("span.twe-ro-hours").each(
                        function (spanIndex, currentSpan) {
                            var text = $(currentSpan).html();
                            totalMinutes += parseHours(text);
                        }
                );
                $(currentTd).find("input.twe-input").each(
                        function (inputIndex, currentInput) {
                            var text = $(currentInput).val();
                            totalMinutes += parseHours(text);
                        }
                );
            });

            var hours = Math.floor(totalMinutes / 60);
            var minutes = totalMinutes % 60;
            var totalTime = formatHoursMinutes(hours, minutes);
            $("table.twe-table tfoot tr:first-child td:nth-child(" +
                    (2 + index) +
                    ") span").html(totalTime);
        }

        function updateWeekTotal() {
            var totalMinutes = 0;
            $("span.twe-day-total").each(function(spanIndex, currentSpan) {
                    var text = $(currentSpan).html();
                    totalMinutes += parseHours(text);
            });
            var hours = Math.floor(totalMinutes / 60);
            var minutes = totalMinutes % 60;
            var totalTime = formatHoursMinutes(hours, minutes);
            $("span.twe-week-total").html(totalTime);
        }

        function cellUpdated(wrappedInput, columnIndex) {
            // normalize the input
            var text = wrappedInput.val();
            var match = hoursRe.exec(text);
            var hours = 0;
            var minutes = 0;
            var valid = false;
            if (match) {
                hours = parseInt(match[1]);
                minutes = parseInt(match[2]);
                valid = true;
            } else {
                match = hoursRe2.exec(text);
                if (match) {
                    hours = parseInt(match[1]);
                    valid = true;
                }
            }
            if (valid) {
                // reformat hours/minutes
                var totalTime = formatHoursMinutes(hours, minutes);
                wrappedInput.val(totalTime);
            }
            updateDayTotal(columnIndex);
            updateWeekTotal();
        }

        for (var i = 0; i < 7; i++) {
            updateDayTotal(i);
            $("table.twe-table tbody td:nth-child(" + (4 + i) + ")").each( function(tdIndex, currentTd) {
                $(currentTd).find("input.twe-input").each(
                    function(inputIndex, currentInput) {
                        var j = i;
                        var wrappedInput = $(currentInput);
                        wrappedInput.blur(function() {
                            cellUpdated(wrappedInput, j);
                        });
                    }
                );
            });
        }
        updateWeekTotal();
    </script>
    </stripes:layout-component>
    <stripes:layout-component name="portletFooter"/>
    <stripes:layout-component name="contentFooter">
    </stripes:layout-component>
</stripes:layout-render>