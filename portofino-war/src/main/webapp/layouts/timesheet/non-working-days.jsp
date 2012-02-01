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
            div.tnwd-container {
                overflow-x: auto;
            }
            table.tnwd-table {
                width: 25em;
                table-layout: fixed;
            }
            table.tnwd-table th, table.tnwd-table td {
                border-color: #dddddd;
            }
            th.tndw-day-of-week {
                background-color: #94B47B;
                color: white;
            }
            th.tndw-saturday, th.tndw-sunday {
                background-color: #74936C;
            }
            table.tnwd-table tbody td {
                padding: 1px;
            }
            div.tnws-day {
                text-align: center;
                padding: 0.5em 0 ;
                cursor: pointer;
            }
            div.tnws-day.tnws-non-working {
                background-color: #F0F8E5;
                color: #993333;
                font-weight: bold;
            }
            div.tnws-day.tnws-hover {
                background-color: #FACE00;
            }
        </style>
        <div class="tnwd-container">
            <table class="tnwd-table">
                <thead>
                <tr>
                    <th class="tndw-day-of-week">lun</th>
                    <th class="tndw-day-of-week">mar</th>
                    <th class="tndw-day-of-week">mer</th>
                    <th class="tndw-day-of-week">gio</th>
                    <th class="tndw-day-of-week">ven</th>
                    <th class="tndw-day-of-week tndw-saturday">sab</th>
                    <th class="tndw-day-of-week tndw-sunday">dom</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td><div class="tnws-day">1</div></td>
                    <td><div class="tnws-day">2</div></td>
                    <td><div class="tnws-day">3</div></td>
                    <td><div class="tnws-day">4</div></td>
                    <td><div class="tnws-day">5</div></td>
                    <td><div class="tnws-day tnws-non-working">6</div></td>
                    <td><div class="tnws-day tnws-non-working">7</div></td>
                </tr>
                <tr>
                    <td><div class="tnws-day">8</div></td>
                    <td><div class="tnws-day">9</div></td>
                    <td><div class="tnws-day">10</div></td>
                    <td><div class="tnws-day">11</div></td>
                    <td><div class="tnws-day">12</div></td>
                    <td><div class="tnws-day tnws-non-working">13</div></td>
                    <td><div class="tnws-day tnws-non-working">14</div></td>
                </tr>
                </tbody>
            </table>
        </div>
        <script type="text/javascript">
            function setNotWorkingDay(cell, nonWorking) {
                var day = cell.text();
                cell.html("saving");
                var data = {
                    nonWorkingDay : day,
                    configureNonWorkingDay : ""
                };

                var postUrl = stripQueryString(location.href);
                jQuery.ajax({
                    type: "post",
                    url: postUrl,
                    data: data,
                    success: function(responseData) {
                        var options = responseData;
                        if('string' === typeof(options)) {
                            options = jQuery.parseJSON(options);
                        }
                        if (nonWorking) {
                            cell.addClass("tnws-non-working");
                        } else {
                            cell.removeClass("tnws-non-working");
                        }
                    },
                    error: function() {
                        alert("Ajax error")
                    }
                });
                cell.html(day);
            }
            $("div.tnws-day").mousemove(
                    function() {
                        $(this).addClass("tnws-hover");
                    }
            ).mouseleave(
                    function() {
                        $(this).removeClass("tnws-hover");
                    }
            ).click(
                    function() {
                        var cell = $(this);
                        if (cell.hasClass("tnws-non-working")) {
                            setNotWorkingDay(cell, false);
                        } else {
                            setNotWorkingDay(cell, true);
                        }
                        cell.removeClass("tnws-hover");
                    }
            )
        </script>
    </stripes:layout-component>
    <stripes:layout-component name="portletFooter"/>
    <stripes:layout-component name="contentFooter">
    </stripes:layout-component>
</stripes:layout-render>