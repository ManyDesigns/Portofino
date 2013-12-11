<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="mde" uri="/manydesigns-elements" %>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.activitystream.ActivityStreamAction"/>
<stripes:layout-render name="/theme/templates/${actionBean.pageInstance.layout.template}/normal.jsp">
    <stripes:layout-component name="pageTitle">
        <c:out value="${actionBean.page.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <div>
            <strong><i class="icon-tag"></i> Ticket description:</strong>
        </div>
        <div><c:out value="${mde:formattedText(actionBean.ticket.description, false)}" escapeXml="false"/></div>
        <c:forEach var="activityItem" items="${actionBean.activityItems}">
            <mde:write name="activityItem"/>
        </c:forEach>
        <c:if test="${actionBean.contributor}">
            <hr/>
            <stripes:form action="${actionBean.context.actionPath}" method="post">
                <label>Add a comment:
                <textarea name="comment" class="input-block-level"></textarea>
                </label>
                <portofino:buttons list="activity"/>
            </stripes:form>
        </c:if>
    </stripes:layout-component>
</stripes:layout-render>