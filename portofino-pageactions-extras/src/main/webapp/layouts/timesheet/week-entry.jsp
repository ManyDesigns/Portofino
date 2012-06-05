<%@ page import="com.manydesigns.elements.gfx.ColorUtils" %>
<%@ page import="com.manydesigns.elements.xml.XhtmlBuffer" %>
<%@ page
        import="com.manydesigns.portofino.pageactions.timesheet.TimesheetAction" %>
<%@ page
        import="com.manydesigns.portofino.pageactions.timesheet.model.Activity" %>
<%@ page
        import="com.manydesigns.portofino.pageactions.timesheet.model.WeekEntryModel" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.joda.time.LocalDate" %>
<%@ page import="org.joda.time.format.DateTimeFormatter" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="stripes"
           uri="http://stripes.sourceforge.net/stripes-dynattr.tld" %>
<%@ taglib prefix="mde" uri="/manydesigns-elements" %>
<<<<<<< local
<%@ taglib tagdir="/WEB-INF/tags" prefix="portofino" %>
<stripes:layout-render name="/skins/${skin}/${actionBean.pageTemplate}/modal.jsp">
<jsp:useBean id="actionBean" scope="request"
             type="com.manydesigns.portofino.pageactions.timesheet.TimesheetAction"/>
=======
<%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><jsp:useBean id="actionBean" scope="request"
             type="com.manydesigns.portofino.pageactions.timesheet.TimesheetAction"
/><stripes:layout-render name="/skins/${skin}/${actionBean.pageTemplate}/modal.jsp">
>>>>>>> other
<stripes:layout-component name="contentHeader">
    <portofino:buttons list="timesheet-week-entry" cssClass="contentButton"/>
    <jsp:include page="/skins/${skin}/breadcrumbs.jsp"/>
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
        border-color: <%= ColorUtils.toHtmlColor(actionBean.getBorderColor()) %>;
    }

    th.twe-activity {
        background-color: <%= ColorUtils.toHtmlColor(actionBean.getHeaderBgColor()) %>;
        color: <%= ColorUtils.toHtmlColor(actionBean.getHeaderColor()) %>;
    }

    th.twe-day {
        background-color: <%= ColorUtils.toHtmlColor(actionBean.getHeaderBgColor()) %>;
        color: <%= ColorUtils.toHtmlColor(actionBean.getHeaderColor()) %>;
    }

    th.twe-day.twe-today {
        background-color: <%= ColorUtils.toHtmlColor(actionBean.getDayTodayHeaderBgColor()) %>;
        color: <%= ColorUtils.toHtmlColor(actionBean.getHeaderColor()) %>;
    }

    th.twe-day.twe-non-working {
        background-color: <%= ColorUtils.toHtmlColor(actionBean.getDayNonWorkingHeaderBgColor()) %>;
        color: <%= ColorUtils.toHtmlColor(actionBean.getHeaderColor()) %>;
    }

    table.twe-table tr.odd {
        background-color: <%= ColorUtils.toHtmlColor(actionBean.getOddRowBgColor()) %>;
    }

    table.twe-table tr.even {
        background-color: <%= ColorUtils.toHtmlColor(actionBean.getEvenRowBgColor()) %>;
    }

    tr.odd td.twe-hours.twe-today {
        background-color: <%= ColorUtils.toHtmlColor(actionBean.getHoursTodayOddBgColor()) %>;
    }

    tr.even td.twe-hours.twe-today {
        background-color: <%= ColorUtils.toHtmlColor(actionBean.getHoursTodayEvenBgColor()) %>;
    }

    tr.odd td.twe-hours.twe-non-working {
        background-color: <%= ColorUtils.toHtmlColor(actionBean.getHoursNonWorkingOddBgColor()) %>;
    }

    tr.even td.twe-hours.twe-non-working {
        background-color: <%= ColorUtils.toHtmlColor(actionBean.getHoursNonWorkingEvenBgColor()) %>;
    }

    td.twe-hours input.twe-input {
        width: 30px;
        text-align: right;
    }

    td.twe-hours img.twe-note {
        float: right;
        margin-top: 4px;
    }

    div.twe-ro-note {
        float: right;
        margin-top: 4px;
        width: 9px;
        height: 9px;
        background-image: url('<stripes:url value="/layouts/timesheet/note.png"/>');
        cursor: pointer;
    }

    div.twe-note {
        float: right;
        margin-top: 4px;
        width: 9px;
        height: 9px;
        background-image: url('<stripes:url value="/layouts/timesheet/empty-note.png"/>');
        cursor: pointer;
    }

    div.twe-note.twe-note-with-content {
        background-image: url('<stripes:url value="/layouts/timesheet/note.png"/>');
    }

    table.twe-table tfoot tr {
        background-color: <%= ColorUtils.toHtmlColor(actionBean.getFooterBgColor()) %>;
        color: <%= ColorUtils.toHtmlColor(actionBean.getFooterColor()) %>;
        font-weight: bold;
    }

    td.twe-total {
        text-align: right;
    }

    td.twe-summary.twe-today {
        background-color: <%= ColorUtils.toHtmlColor(actionBean.getTodayFooterBgColor()) %>;
    }

    td.twe-summary.twe-non-working {
        background-color: <%= ColorUtils.toHtmlColor(actionBean.getNonWorkingFooterBgColor()) %>;
    }

    span.twe-ro-hours, span.twe-day-total, span.twe-day-overtime {
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
<%
    WeekEntryModel weekEntryModel =
            actionBean.getWeekEntryModel();
    XhtmlBuffer xb = new XhtmlBuffer(out);
%>
<div style="float: right">
    <portofino:buttons list="timesheet-we-navigation" cssClass="portletButton"/>
</div>
<fmt:message key="timesheet.person"/>:
<%
    xb.write(weekEntryModel.getPersonName());
%>
<br/>
<fmt:message key="timesheet.from.day"/>:
<%
    DateTimeFormatter longDateFormatter = actionBean.getLongDateFormatter();
    xb.write(longDateFormatter.print(weekEntryModel.getDay(0).getDate()));
%>
<br/>
<fmt:message key="timesheet.to.day"/>:
<%
    xb.write(longDateFormatter.print(weekEntryModel.getDay(6).getDate()));
%>
<div class="horizontalSeparator"></div>
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
    <th class="twe-activity"><c:out
            value="${actionBean.pageInstance.configuration.level1Label}"/></th>
    <th class="twe-activity"><c:out
            value="${actionBean.pageInstance.configuration.level2Label}"/></th>
    <th class="twe-activity"><c:out
            value="${actionBean.pageInstance.configuration.level3Label}"/></th>
    <%
        for (int i = 0; i < 7; i++) {
            WeekEntryModel.Day day = weekEntryModel.getDay(i);
            LocalDate dayDate = day.getDate();
            WeekEntryModel.DayStatus dayStatus = day.getStatus();

            xb.openElement("th");
            String htmlClass = "twe-day";
            if (day.isNonWorking()) {
                htmlClass += " twe-non-working";
            }

            if (day.isToday()) {
                htmlClass += " twe-today";
            }

            xb.addAttribute("class", htmlClass);


            xb.openElement("div");
            xb.write(actionBean.getDayOfWeekFormatter().print(dayDate));
            if (dayStatus == WeekEntryModel.DayStatus.LOCKED) {
                xb.openElement("span");
                xb.addAttribute("class", "ui-icon ui-icon-locked");
                xb.addAttribute("style", "float: right");
                xb.addAttribute("title", "locked");
                xb.closeElement("span");
            }
            xb.closeElement("div");

            xb.write(actionBean.getDateFormatter().print(dayDate));

            if (dayStatus != null) {
                String id = "twe-swm-" + i;
                Integer standardWorkingMinutes =
                        day.getStandardWorkingMinutes();
                String value = (standardWorkingMinutes == null)
                        ? "-"
                        : Integer.toString(standardWorkingMinutes);
                xb.writeInputHidden(id, id, value);
            }

            xb.closeElement("th");
        }
    %>
</tr>
</thead>
<tbody>
<%
    boolean even = false;
    List<Activity> weekActivities = weekEntryModel.getActivities();
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
        xb.write(activity.getLevel1());
        xb.closeElement("td");

        xb.openElement("td");
        xb.write(activity.getLevel2());
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
            xb.write(activity.getLevel3());
        } else {
            xb.writeAnchor(readUrl, activity.getLevel3());
        }
        xb.closeElement("td");

        for (int dayIndex = 0; dayIndex < 7; dayIndex++) {
            WeekEntryModel.Day day = weekEntryModel.getDay(dayIndex);
            LocalDate dayDate = day.getDate();
            WeekEntryModel.DayStatus dayStatus = day.getStatus();

            xb.openElement("td");
            String htmlClass = "twe-hours";
            if (day.isNonWorking()) {
                htmlClass += " twe-non-working";
            }

            if (day.isToday()) {
                htmlClass += " twe-today";
            }

            xb.addAttribute("class", htmlClass);

            if (dayStatus == null) {
                //xb.write("--");
            } else {
                String hours;
                WeekEntryModel.Entry entry =
                        day.findEntryByActivity(activity);
                String note = null;

                String inputName = String.format(TimesheetAction.ENTRY_INPUT_FORMAT,
                        dayIndex, activity.getId());
                String noteInputName = String.format(TimesheetAction.NOTE_INPUT_FORMAT,
                        dayIndex, activity.getId());
                int tabIndex = activityIndex +
                        dayIndex * weekActivitiesSize + 1;


                if (entry == null) {
                    hours = null;
                } else {
                    int minutes = entry.getMinutes();
                    if (minutes == 0) {
                        hours = null;
                    } else {
                        hours = String.format("%d:%02d",
                                minutes / 60,
                                minutes % 60);
                    }
                    note = entry.getNote();
                }
                if (dayStatus == WeekEntryModel.DayStatus.LOCKED) {
                    if (hours == null) {
                        xb.openElement("span");
                        xb.addAttribute("class", "twe-ro-hours");
                        xb.write("--");
                        xb.closeElement("span");
                    } else {
                        if (StringUtils.isNotBlank(note)) {
                            xb.openElement("div");
                            xb.addAttribute("class", "twe-ro-note");
                            xb.writeInputHidden(noteInputName, note);
                            xb.closeElement("div");

                        }
                        xb.openElement("span");
                        xb.addAttribute("class", "twe-ro-hours");
                        xb.write(hours);
                        xb.closeElement("span");
                    }
                } else {
                    xb.openElement("div");
                    xb.addAttribute("class", "twe-note");
                    xb.writeInputHidden(noteInputName, note);
                    xb.closeElement("div");

                    xb.openElement("span");
                    xb.openElement("input");
                    xb.addAttribute("type", "text");
                    xb.addAttribute("name", inputName);
                    xb.addAttribute("class", "twe-input");
                    xb.addAttribute("value", hours);
                    xb.addAttribute("tabindex", Integer.toString(tabIndex));
                    xb.closeElement("input");
                    xb.closeElement("span");
                }
            }

            xb.closeElement("td");
        }

        xb.closeElement("tr");
    }
%>
</tbody>
<tfoot>
<tr>
    <td class="twe-total" colspan="3"><fmt:message
            key="timesheet.total.week.hours"/>: <span id="twe-week-total"
                                                      class="twe-week-total"></span>
    </td>
    <%
        for (int i = 0; i < 7; i++) {
            WeekEntryModel.Day day = weekEntryModel.getDay(i);

            xb.openElement("td");
            String htmlClass = "twe-summary";
            if (day.isNonWorking()) {
                htmlClass += " twe-non-working";
            }

            if (day.isToday()) {
                htmlClass += " twe-today";
            }

            xb.addAttribute("class", htmlClass);

            xb.openElement("span");
            xb.addAttribute("class", "twe-day-total");
            xb.closeElement("span");

            xb.closeElement("td");
        }
    %>
</tr>
<tr>
    <td class="twe-total" colspan="3"><fmt:message
            key="timesheet.total.overtime"/>: <span id="twe-week-overtime"
                                                      class="twe-week-total"></span>
    </td>
    <%
        for (int i = 0; i < 7; i++) {
            WeekEntryModel.Day day = weekEntryModel.getDay(i);

            xb.openElement("td");
            String htmlClass = "twe-summary";
            if (day.isNonWorking()) {
                htmlClass += " twe-non-working";
            }

            if (day.isToday()) {
                htmlClass += " twe-today";
            }

            xb.addAttribute("class", htmlClass);

            xb.openElement("span");
            xb.addAttribute("class", "twe-day-overtime");
            xb.closeElement("span");

            xb.closeElement("td");
        }
    %>
</tr>
</tfoot>
</table>
</div>
<%
    String value = actionBean.getReferenceDateFormatter()
            .print(weekEntryModel.getReferenceDate());
    xb.writeInputHidden("referenceDate", value);
%>
<input type="hidden" name="personId"
       value="<c:out value="${actionBean.personId}"/>"/>

<div id="twe-ro-note-dialog">
    Read-only note dialog
</div>
<div id="twe-note-dialog">
    <textarea style="width: 99%; height: 95%">Note dialog</textarea>
</div>
<script type="text/javascript">
    var hoursRe = /^(\d+):(\d+)$/;
    var hoursRe2 = /^(\d+):?$/;
    var unsavedChanges = false;

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
        $("table.twe-table tbody td:nth-child(" + (4 + index) + ")").each(function(tdIndex, currentTd) {
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
        $("table.twe-table tfoot tr:nth-child(1) td:nth-child(" +
                (2 + index) +
                ") span").html(totalTime);

        //overtime
        var swmId = "twe-swm-" + index;
        var standardWorkingMinutes = $("input#" + swmId).val();
        var overtimeMinutes = totalMinutes - standardWorkingMinutes;
        if (isNaN(overtimeMinutes)) {
            overtimeMinutes = 0;
        }
        if (overtimeMinutes < 0) {
            overtimeMinutes = 0;
        }
        hours = Math.floor(overtimeMinutes / 60);
        minutes = overtimeMinutes % 60;
        totalTime = formatHoursMinutes(hours, minutes);
        $("table.twe-table tfoot tr:nth-child(2) td:nth-child(" +
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
        $("span#twe-week-total").html(totalTime);

        // overtime
        totalMinutes = 0;
        $("span.twe-day-overtime").each(function(spanIndex, currentSpan) {
            var text = $(currentSpan).html();
            totalMinutes += parseHours(text);
        });
        hours = Math.floor(totalMinutes / 60);
        minutes = totalMinutes % 60;
        totalTime = formatHoursMinutes(hours, minutes);
        $("span#twe-week-overtime").html(totalTime);
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
        unsavedChanges = true;
    }

    for (var i = 0; i < 7; i++) {
        updateDayTotal(i);
        $("table.twe-table tbody td:nth-child(" + (4 + i) + ")").each(function(tdIndex, currentTd) {
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

    // note dialogs initialization
    $("#twe-ro-note-dialog").dialog({
        autoOpen: false,
        height: 200,
        width: 350,
        modal: true,
        buttons: {
            "<fmt:message key="commons.close"/>": function() {
                $(this).dialog("close");
            }
        },
        close: function() {
        }
    });

    $("#twe-note-dialog").dialog({
        autoOpen: false,
        height: 200,
        width: 350,
        modal: true,
        buttons: {
            '<fmt:message key="commons.save"/>': function() {
                var dialogWrapper = $(this);
                var dialogTextarea = dialogWrapper.find("textarea");
                var text = $.trim(dialogTextarea.val());

                var divWrapper = dialogWrapper.data("divWrapper");
                var inputWrapper = divWrapper.find("input");
                inputWrapper.val(text);
                unsavedChanges = true;
                setNoteWithContent(text, divWrapper);

                dialogWrapper.dialog("close");
            },
            '<fmt:message key="commons.cancel"/>': function() {
                $(this).dialog("close");
            }
        },
        close: function() {
        }
    });

    // note events and display
    $("div.twe-ro-note").click(function() {
        var text = $(this).find("input").val();
        var dialogDiv = $("#twe-ro-note-dialog");
        dialogDiv.text(text);
        dialogDiv.dialog("open");
    });

    function setNoteWithContent(text, divWrapper) {
        if (text.length > 0) {
            divWrapper.addClass("twe-note-with-content");
        } else {
            divWrapper.removeClass("twe-note-with-content");
        }
    }

    $("div.twe-note").each(function(index, div) {
        var divWrapper = $(div);
        var inputWrapper = divWrapper.find("input");
        var text = $.trim(inputWrapper.val());
        setNoteWithContent(text, divWrapper);
        divWrapper.click(function() {
            var text = $.trim(inputWrapper.val());
            var dialogDiv = $("#twe-note-dialog");
            dialogDiv.data("divWrapper", divWrapper);
            var dialogTextarea = dialogDiv.find("textarea");
            dialogTextarea.val(text);
            dialogDiv.dialog("open");
        });
    });

    // page abandon
    $("button[type='submit'][name!='saveWeekEntryModel']").click(function() {
        if (unsavedChanges) {
            return confirm("There are unsaved changes. Abandon page anyway?");
        } else {
            return true;
        }
    });
</script>
</stripes:layout-component>
<stripes:layout-component name="portletFooter"/>
<stripes:layout-component name="contentFooter">
</stripes:layout-component>
</stripes:layout-render>