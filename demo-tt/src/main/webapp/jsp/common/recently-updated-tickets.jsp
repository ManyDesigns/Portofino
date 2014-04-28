<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.custom.CustomAction"/>
<stripes:layout-render name="/theme/templates/${actionBean.pageInstance.layout.template}/normal.jsp">
    <stripes:layout-component name="pageTitle">
        <c:out value="${actionBean.page.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <c:if test="${empty actionBean.tickets}">
            <p>There are no tickets.</p>
        </c:if>
        <c:if test="${not empty actionBean.tickets}">
            <ul class="ticket-list">
                <c:forEach items="${actionBean.tickets}" var="ticket">
                    <li class="ticket-list-item">
                        <div>
                            <strong><stripes:link href="/projects/${ticket.project}/tickets/${ticket.project}/${ticket.n}"><c:out value="${ticket.project}-${ticket.n}"/></stripes:link></strong>
                            <strong><c:out value="${ticket.title}"/></strong>
                        </div>
                        <div>
                            <small class="text-muted">Updated on <fmt:formatDate value="${ticket.last_updated}" pattern="yyyy-MM-dd HH:mm:ss z"/></small>
                        </div>
                    </li>
                </c:forEach>
            </ul>
        </c:if>
    </stripes:layout-component>
</stripes:layout-render>