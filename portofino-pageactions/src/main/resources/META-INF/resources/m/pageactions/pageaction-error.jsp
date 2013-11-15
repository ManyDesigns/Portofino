<%@ page import="com.manydesigns.portofino.pageactions.AbstractPageAction" %><%@
    page import="org.apache.commons.lang.exception.ExceptionUtils" %><%@
    page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %><%@
    taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><%@
    taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"%><%@
    taglib prefix="mde" uri="/manydesigns-elements"%><%@
    taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %><%--
--%><stripes:layout-render name="/theme/templates/default/normal.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.AbstractPageAction"/>
    <stripes:layout-component name="pageTitle">
        <c:out value="${actionBean.pageInstance.page.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <%
            Object exception = request.getAttribute(AbstractPageAction.PORTOFINO_PAGEACTION_EXCEPTION);
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
        <div class="alert alert-error">
            <button data-dismiss="alert" class="close" type="button">&times;</button>
            <ul class="errorMessages">
                <li>
                    <fmt:message key="this.page.has.thrown.an.exception.during.execution">
                        <fmt:param value="${exceptionString}" />
                    </fmt:message>
                </li>
            </ul>
        </div>
    </stripes:layout-component>
</stripes:layout-render>