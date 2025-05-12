<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="mde" uri="/manydesigns-elements" %>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.activitystream.ActivityStreamAction"/>
<stripes:layout-render name="/theme/templates/${actionBean.pageTemplate}/normal.jsp">
    <stripes:layout-component name="pageTitle">
        <c:out value="${actionBean.pageInstance.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <c:forEach var="activityItem" items="${actionBean.activityItems}">
            <mde:write name="activityItem"/>
        </c:forEach>
    </stripes:layout-component>
</stripes:layout-render>
