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
    <input type="hidden" name="referenceDateTimeLong" value="${actionBean.referenceDateTimeLong}" />
    <div style="width: 100%; position: relative;">
        <div style="width: 10em; position: absolute; overflow: hidden;">
            <h3 style="margin: 0;">Calendars</h3>
            <ul style="margin-left: 0;">
                <c:forEach var="calendar" items="${actionBean.calendars}">
                    <li style="margin: 0.5em 0; list-style: none;">
                        <div style="background-color: ${calendar.color}; height: 1em;
                             width: 1em; display: inline-block;">&nbsp;</div>
                        ${calendar.name}
                    </li>
                </c:forEach>
            </ul>
        </div>
        <div style="position: relative; margin-left: 10em;">
            <jsp:include page="${actionBean.calendarViewType}.jsp" />
        </div>
    </div>
    </stripes:layout-component>
    <stripes:layout-component name="portletFooter">
    </stripes:layout-component>
</stripes:layout-render>