<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<%@taglib prefix="mde" uri="/manydesigns-elements"%>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.PortletAction"/>
<div class="portletContainer">
    <c:forEach var="pageToInclude" items="${ actionBean.portlets['contentLayoutTop'] }">
        <div class="portletWrapper">
            <jsp:include page="${pageToInclude}" />
        </div>
    </c:forEach>
</div>
<div class="yui-g">
    <div class="yui-u first portletContainer">
        <c:forEach var="pageToInclude" items="${ actionBean.portlets['contentLayoutLeft'] }">
            <div class="portletWrapper">
                <jsp:include page="${pageToInclude}" />
            </div>
        </c:forEach>
    </div>
    <div class="yui-u portletContainer">
        <c:forEach var="pageToInclude" items="${ actionBean.portlets['contentLayoutRight'] }">
            <div class="portletWrapper">
                <jsp:include page="${pageToInclude}" />
            </div>
        </c:forEach>
    </div>
</div>
<div class="portletContainer">
    <c:forEach var="pageToInclude" items="${ actionBean.portlets['contentLayoutBottom'] }">
        <div class="portletWrapper">
            <jsp:include page="${pageToInclude}" />
        </div>
    </c:forEach>
</div>
<script type="text/javascript">
    $(enablePortletDragAndDrop);
</script>