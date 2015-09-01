<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.calendar.CalendarAction"
/><stripes:layout-render name="/theme/templates/${actionBean.pageTemplate}/normal.jsp">
    <stripes:layout-component name="pageTitle">
        <c:out value="${actionBean.page.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
    <style type="text/css">
        ul.calendars {
            padding:  0;
        }

        ul.calendars li {
            margin: 0.5em 0; list-style: none;
        }

        ul.calendars div {
            height: 100%; width: 1em; display: inline-block;
        }

        .event{ border-radius: 3px; font-size: smaller; }
        .event a {color:white}

    </style>
    <div class="row">
        <div class="col-md-2 calendar-calendars">
            <strong><fmt:message key="calendars" /></strong>
            <ul class="calendars">
                <c:forEach var="calendar" items="${actionBean.calendars}">
                    <li>
                        <div style="background-color: ${calendar.backgroundHtmlColor};">&nbsp;</div>
                        ${calendar.name}
                    </li>
                </c:forEach>
            </ul>
        </div>
        <div class="col-md-10 calendar-view">
            <stripes:form action="${actionBean.context.actionPath}" method="post">
                <input type="hidden" name="referenceDateTimeLong" value="${actionBean.referenceDateTimeLong}" />
                <input type="hidden" name="returnUrl" value="<c:out value="${actionBean.returnUrl}"/>"/>
                <input type="hidden" name="calendarViewType" value="<c:out value="${actionBean.calendarViewType}"/>"/>
                <a class="calendar-legend-hide-link" data-hide="false" href="#"><span id="glyphicon" class="glyphicon glyphicon-menu-left"></span><span id="hide_link_text"><fmt:message key="hide.calendars" /></span></a>
                <jsp:include page="${actionBean.calendarViewType}.jsp" />
            </stripes:form>
        </div>
    </div>
    <script type="text/javascript">
        $(function() {
            $(".calendar-legend-hide-link").click(function() {
                var link = $(this);
                var hide = link.data("hide");
                var calendarsDiv = $(".calendar-calendars");
                var viewDiv = $(".calendar-view");
                var gliph = $("#glyphicon");
                var txt = $("#hide_link_text");

                if (hide) {
                    calendarsDiv.insertBefore(viewDiv);
                    calendarsDiv.show();
                    viewDiv.removeClass("col-md-12");
                    viewDiv.addClass("col-md-10");
                    gliph.removeClass("glyphicon-menu-right");
                    gliph.addClass("glyphicon-menu-left");
                    txt.text('<fmt:message key="hide.calendars" />');
                    link.data("hide", false);
                } else {
                    calendarsDiv.hide();
                    calendarsDiv.insertAfter(viewDiv);
                    viewDiv.removeClass("col-md-10");
                    viewDiv.addClass("col-md-12");
                    gliph.removeClass("glyphicon-menu-left");
                    gliph.addClass("glyphicon-menu-right");
                    txt.text('<fmt:message key="show.calendars" />');
                    link.data("hide", true);
                }
                return false;
            });
        });
    </script>
    </stripes:layout-component>
</stripes:layout-render>