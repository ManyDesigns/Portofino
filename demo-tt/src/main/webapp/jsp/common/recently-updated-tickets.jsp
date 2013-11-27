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
            <dl>
                <c:forEach items="${actionBean.tickets}" var="ticket">
                    <dt>
                        <stripes:link href="/projects/${ticket.project}/tickets/${ticket.project}/${ticket.n}">
                            <c:out value="${ticket.project}-${ticket.n}"/>
                        </stripes:link>
                    </dt>
                    <dd>
                        <small class="muted">
                            updated on <fmt:formatDate value="${ticket.date_updated}" pattern="yyyy-MM-dd HH:mm:ss z"/>
                        </small>
                        <div>
                            <c:out value="${ticket.title}"/>
                        </div>
                    </dd>
                </c:forEach>
            </dl>
        </c:if>
    </stripes:layout-component>
</stripes:layout-render>