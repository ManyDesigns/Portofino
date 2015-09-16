<%@ page import="java.math.BigInteger" %>
<%@ page import="java.util.Map" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"%>
<%@ taglib prefix="mde" uri="/manydesigns-elements"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.crud.CrudAction"/>
<c:set var="version" value="${actionBean.object}"/>
<stripes:layout-render name="/theme/templates/${actionBean.pageTemplate}/normal.jsp">
    <stripes:layout-component name="contentHeader">
        <mde:sessionMessages />
        <div class="pull-right">
            <jsp:include page="/m/crud/result-set-navigation.jsp" />
        </div>
        <jsp:include page="/theme/breadcrumbs.jsp" />
    </stripes:layout-component>
    <stripes:layout-component name="pageHeader">
        <div class="pull-right">
            <stripes:form action="${actionBean.context.actionPath}" method="post">
                <input type="hidden" name="returnUrl"
                       value="<c:out value="${actionBean.returnUrl}"/>"/>
                <portofino:buttons list="pageHeaderButtons" cssClass="btn-xs" />
            </stripes:form>
        </div>
        <h3 class="pageTitle">
            <c:out value="${version.id} - ${version.title}"/>
            <span style="vertical-align: middle" class="<c:out value="${version.fk_version_state.css_class}"/>"><c:out value="${version.fk_version_state.state}"/></span>
        </h3>
        <div><c:out value="${version.description}"/></div>
        <div>
            <c:if test="${not empty version.planned_date}">
                <fmt:message key="planned.date._">
                    <fmt:param value="${version.planned_date}"/>
                </fmt:message>
            </c:if>
            <c:if test="${empty version.planned_date}">
                <fmt:message key="no.planned.date"/>
            </c:if>
        </div>
        <div>
            <div class="pull-right">
                <%
                    Map version = (Map)pageContext.getAttribute("version");
                    Long versionId = (Long) version.get("id");
                    int nTickets = ((BigInteger)actionBean.session
                            .createSQLQuery("select count(*) from tickets where fix_version = :versionId")
                            .setLong("versionId", versionId)
                            .uniqueResult()).intValue();
                    int nClosedTickets = ((BigInteger)actionBean.session
                            .createSQLQuery("select count(*) from tickets where state = 4 and fix_version = :versionId")
                            .setLong("versionId", versionId)
                            .uniqueResult()).intValue();
                    int percentage = 0;
                    if (nTickets > 0) {
                        percentage = nClosedTickets * 100 / nTickets;
                    }
                    pageContext.setAttribute("percentage", percentage);
                    pageContext.setAttribute("nClosedTickets", nClosedTickets);
                    pageContext.setAttribute("nTickets", nTickets);
                %>
                <c:if test="${nTickets > 0}">
                    <fmt:message key="progress._._.closed.of._">
                        <fmt:param value="${percentage}"/>
                        <fmt:param value="${nClosedTickets}"/>
                        <fmt:param value="${nTickets}"/>
                    </fmt:message>
                </c:if>
                <c:if test="${nTickets == 0}">
                <fmt:message key="progress.no.tickets.in.this.version"/>
                </c:if>
            </div>
            <c:if test="${not empty version.release_date}">
                <fmt:message key="release.date._">
                    <fmt:param value="${version.release_date}"/>
                </fmt:message>
            </c:if>
            <c:if test="${empty version.release_date}">
                <fmt:message key="no.release.date"/>
            </c:if>
        </div>

    </stripes:layout-component>
    <stripes:layout-component name="pageTitle">
        <c:out value="${actionBean.readTitle}"/>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
    </stripes:layout-component>
</stripes:layout-render>