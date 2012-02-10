<%@ page import="com.manydesigns.portofino.pageactions.AbstractPageAction" %><%@
    page import="org.apache.commons.lang.exception.ExceptionUtils" %><%@
    page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %><%@
    taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><%@
    taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"%><%@
    taglib prefix="mde" uri="/manydesigns-elements"%><%@
    taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %><%--
--%><stripes:layout-render name="/skins/${skin}/portlet.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.AbstractPageAction"/>
    <stripes:layout-component name="portletTitle">
        <c:out value="${actionBean.pageInstance.page.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <%
            Object exception = request.getAttribute(AbstractPageAction.PORTOFINO_PORTLET_EXCEPTION);
            if(exception instanceof Throwable) {
                Throwable rootCause = ExceptionUtils.getRootCause((Throwable) exception);
                if(rootCause == null) {
                    rootCause = (Throwable) exception;
                }
                pageContext.setAttribute("exceptionString", rootCause.toString());
            } else {
                pageContext.setAttribute("exceptionString", "unknown");
            }
        %>
        <ul class="errorMessages">
            <li>
                <fmt:message key="portlet.exception">
                    <fmt:param value="${exceptionString}" />
                </fmt:message>
            </li>
        </ul>
    </stripes:layout-component>
    <stripes:layout-component name="portletFooter" />
</stripes:layout-render>