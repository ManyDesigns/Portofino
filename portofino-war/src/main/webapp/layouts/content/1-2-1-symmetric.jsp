<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<%@taglib prefix="mde" uri="/manydesigns-elements"%>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.PortletAction"/>
<div id="contentLayoutTop" class="portletContainer">
    <c:forEach var="portletInstance" items="${ actionBean.portlets['contentLayoutTop'] }">
        <div class="portletWrapper" id="pw_<c:out value='${portletInstance.id}' />">
            <jsp:include page="${portletInstance.jsp}" />
        </div>
    </c:forEach>
</div>
<div class="yui-g">
    <div id="contentLayoutLeft" class="yui-u first portletContainer">
        <c:forEach var="portletInstance" items="${ actionBean.portlets['contentLayoutLeft'] }">
            <div class="portletWrapper" id="pw_<c:out value='${portletInstance.id}' />">
                <jsp:include page="${portletInstance.jsp}" />
            </div>
        </c:forEach>
    </div>
    <div id="contentLayoutRight" class="yui-u portletContainer">
        <c:forEach var="portletInstance" items="${ actionBean.portlets['contentLayoutRight'] }">
            <div class="portletWrapper" id="pw_<c:out value='${portletInstance.id}' />">
                <jsp:include page="${portletInstance.jsp}" />
            </div>
        </c:forEach>
    </div>
</div>
<div id="contentLayoutBottom" class="portletContainer">
    <c:forEach var="portletInstance" items="${ actionBean.portlets['contentLayoutBottom'] }">
        <div class="portletWrapper" id="pw_<c:out value='${portletInstance.id}' />">
            <jsp:include page="${portletInstance.jsp}" />
        </div>
    </c:forEach>
</div>
<script type="text/javascript">
    $(enablePortletDragAndDrop);
</script>