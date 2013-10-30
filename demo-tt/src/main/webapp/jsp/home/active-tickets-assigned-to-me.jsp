<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.custom.CustomAction"/>
<stripes:layout-render name="/theme/templates/${actionBean.pageInstance.layout.template}/normal.jsp">
    <stripes:layout-component name="pageTitle">
        <c:out value="${actionBean.page.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <c:if test="${empty actionBean.tickets}">
            <p>There are no tickets assigned to you.</p>
        </c:if>
        <c:if test="${not empty actionBean.tickets}">
            <ul>
                <c:forEach items="${actionBean.tickets}" var="ticket">
                    <li>
                        <stripes:link href="/projects/${ticket.project_id}/tickets/${ticket.project_id}/${ticket.n}">
                            <c:out value="${ticket.project_id}-${ticket.n}"/>
                        </stripes:link>: <c:out value="${ticket.title}"/>
                    </li>
                </c:forEach>
            </ul>
        </c:if>
    </stripes:layout-component>
</stripes:layout-render>