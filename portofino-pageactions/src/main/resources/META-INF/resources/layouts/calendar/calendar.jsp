<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.calendar.CalendarAction"/>
<stripes:layout-render name="/skins/${skin}${actionBean.pageTemplate}/normal.jsp">
    <stripes:layout-component name="portletTitle">
        <c:out value="${actionBean.page.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
    <input type="hidden" name="referenceDateTimeLong" value="${actionBean.referenceDateTimeLong}" />
    <style type="text/css">
        ul.calendars {
            margin:  0;
        }
    </style>
        <div class="row-fluid">
            <div class="span2 calendar-calendars">
                <strong><fmt:message key="calendar.calendars.legend" /></strong>
                <ul class="calendars">
                    <c:forEach var="calendar" items="${actionBean.calendars}">
                        <li style="margin: 0.5em 0; list-style: none;">
                            <div style="background-color: ${calendar.backgroundHtmlColor}; height: 1em;
                                 width: 1em; display: inline-block;">&nbsp;</div>
                            ${calendar.name}
                        </li>
                    </c:forEach>
                </ul>
            </div>
            <div class="span10 calendar-view">
                <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
                <input type="hidden" name="calendarViewType" value="<c:out value="${actionBean.calendarViewType}"/>"/>
                <a class="calendar-legend-hide-link" data-hide="false" href="#"><fmt:message key="calendar.hide.calendars" /></a>
                <jsp:include page="${actionBean.calendarViewType}.jsp" />
            </div>
        </div>
    <script type="text/javascript">
        $(function() {
            $(".calendar-legend-hide-link").click(function() {
                var link = $(this);
                var hide = link.data("hide");
                var containerDiv = link.parent().parent();
                var calendarsDiv = containerDiv.find(".calendar-calendars")
                var viewDiv = containerDiv.find(".calendar-view")
                if (hide) {
                    calendarsDiv.insertBefore(viewDiv);
                    calendarsDiv.show();
                    viewDiv.removeClass("span12");
                    viewDiv.addClass("span10");
                    link.text('<fmt:message key="calendar.hide.calendars" />');
                    link.data("hide", false);
                } else {
                    calendarsDiv.hide();
                    calendarsDiv.insertAfter(viewDiv);
                    viewDiv.removeClass("span10");
                    viewDiv.addClass("span12");
                    link.text('<fmt:message key="calendar.show.calendars" />');
                    link.data("hide", true);
                }
                return false;
            });
        });
    </script>
    </stripes:layout-component>
</stripes:layout-render>