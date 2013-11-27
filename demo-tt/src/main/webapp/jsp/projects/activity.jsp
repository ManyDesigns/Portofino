<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.custom.CustomAction"
/><stripes:layout-render name="/theme/templates/${actionBean.pageInstance.layout.template}/normal.jsp">
    <stripes:layout-component name="pageTitle">
        <c:out value="${actionBean.page.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <c:forEach var="activityItem" items="${actionBean.activityItems}">
            <c:set var="ticket" value="${activityItem.fk_activity_ticket}"/>
            <div class="media">
                <stripes:link class="pull-left" href="/users/${activityItem.user}">
                    <img class="media-object" alt="user" src="<stripes:url value="/images/user-placeholder-40x40.png"/>" />
                </stripes:link>
                <div class="media-body">
                    <div>
                        <strong>
                        <stripes:link href="/users/${activityItem.user}"><c:out value="${activityItem.fk_activity_user.first_name} ${activityItem.fk_activity_user.last_name}"/></stripes:link>
                        </strong>
                        on
                        <strong>
                        <stripes:link href="/projects/${ticket.project}/tickets/${ticket.project}/${ticket.n}"><c:out value="${ticket.project}-${ticket.n}"/></stripes:link>
                        <c:out value="${ticket.title}"/>
                        </strong>
                    </div>
                    <div>
                        <strong>
                        <small class="muted">
                            <fmt:message key="${activityItem.fk_activity_type.type}">
                                <fmt:param value="${activityItem.date}"/>
                            </fmt:message>
                        </small>
                        </strong>
                    </div>
                    <div><c:out value="${activityItem.message}"/></div>
                </div>
            </div>
        </c:forEach>
    </stripes:layout-component>
</stripes:layout-render>