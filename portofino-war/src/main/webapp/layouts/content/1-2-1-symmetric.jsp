<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<%@taglib prefix="mde" uri="/manydesigns-elements"%>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.PortletAction"/>
<div>
    <c:set var="firstTop" value="${true}" />
    <c:forEach var="pageToInclude" items="${ actionBean.portlets['contentLayoutTop'] }">
        <div class="portletWrapper ${firstTop ? 'first' : ''}">
            <jsp:include page="${pageToInclude}" />
        </div>
        <c:set var="firstTop" value="${false}" />
    </c:forEach>
</div>
<div class="yui-g">
    <div class="yui-u first">
        <c:set var="firstLeft" value="${firstTop}" />
        <c:forEach var="pageToInclude" items="${ actionBean.portlets['contentLayoutLeft'] }">
            <div class="portletWrapper ${firstLeft ? 'first' : ''}">
                <jsp:include page="${pageToInclude}" />
            </div>
            <c:set var="firstLeft" value="${false}" />
        </c:forEach>
    </div>
    <div class="yui-u">
        <c:set var="firstRight" value="${firstTop}" />
        <c:forEach var="pageToInclude" items="${ actionBean.portlets['contentLayoutRight'] }">
            <div class="portletWrapper ${firstRight ? 'first' : ''}">
                <jsp:include page="${pageToInclude}" />
            </div>
            <c:set var="firstRight" value="${false}" />
        </c:forEach>
    </div>
</div>
<div>
    <c:set var="firstBottom" value="${firstLeft && firstRight}" />
    <c:forEach var="pageToInclude" items="${ actionBean.portlets['contentLayoutBottom'] }">
        <div class="portletWrapper ${firstBottom ? 'first' : ''}">
            <jsp:include page="${pageToInclude}" />
        </div>
        <c:set var="firstBottom" value="${false}" />
    </c:forEach>
</div>