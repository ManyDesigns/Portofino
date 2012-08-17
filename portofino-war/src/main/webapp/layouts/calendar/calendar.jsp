<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.calendar.CalendarAction"/>
<stripes:layout-render name="/skins/${skin}/portlet.jsp">
    <stripes:layout-component name="portletTitle">
        <c:out value="${actionBean.page.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
    <input type="hidden" name="referenceDateTimeLong" value="${actionBean.referenceDateTimeLong}" />
    <style type="text/css">
        .calendar-view {
            position: relative; margin-left: 10em;
        }
        .calendar-legend-area {
            position: absolute; overflow-x: hidden; overflow-y: auto; top: 3.5em; left: 0; height: 96%;
        }
        a.calendar-legend-hide-link, .calendar-legend-show-link a {
            border: none;
        }
    </style>
    <div style="width: 100%; position: relative;">
        <div class="calendar-legend-area calendar-legend" style="width: 9.5em; margin-right: 0.5em;">
            <h3 style="margin: 0;">
                <fmt:message key="calendar.calendars.legend" />
                <a class="calendar-legend-hide-link" href="#">&lt;&lt;</a>
            </h3>
            <ul style="margin-left: 0;">
                <c:forEach var="calendar" items="${actionBean.calendars}">
                    <li style="margin: 0.5em 0; list-style: none;">
                        <div style="background-color: ${calendar.backgroundHtmlColor}; height: 1em;
                             width: 1em; display: inline-block;">&nbsp;</div>
                        ${calendar.name}
                    </li>
                </c:forEach>
            </ul>
        </div>
        <div class="calendar-legend-area calendar-legend-show-link" style="display: none;">
            <h3 style="margin: 0;"><a href="#">&gt;&gt;</a></h3>
        </div>
        <div class="calendar-view">
            <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
            <input type="hidden" name="calendarViewType" value="<c:out value="${actionBean.calendarViewType}"/>"/>
            <jsp:include page="${actionBean.calendarViewType}.jsp" />
        </div>
    </div>
    <script type="text/javascript">
        $(function() {
            $(".calendar-legend-hide-link").click(function() {
                var legendDiv = $(this).parent().parent();
                legendDiv.hide();
                var container = legendDiv.parent();
                container.find(".calendar-view").attr("style", "margin-left: 2em;");
                container.find(".calendar-legend-show-link").show();
                return false;
            });
            $(".calendar-legend-show-link a").click(function() {
                var showLinkDiv = $(this).parent().parent();
                showLinkDiv.hide();
                var container = showLinkDiv.parent();
                container.find(".calendar-view").attr("style", "margin-left: 10em;");
                container.find(".calendar-legend").show();
                return false;
            });
        });
    </script>
    </stripes:layout-component>
    <stripes:layout-component name="portletFooter">
    </stripes:layout-component>
</stripes:layout-render>