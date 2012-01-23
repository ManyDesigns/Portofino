<%@ page import="com.manydesigns.portofino.pageactions.AbstractPageAction" %>
<%@ page import="org.apache.commons.lang.exception.ExceptionUtils" %>
<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"%>
<%@taglib prefix="mde" uri="/manydesigns-elements"%>

<stripes:layout-render name="/skins/${skin}/portlet.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.AbstractPageAction"/>
    <stripes:layout-component name="portletTitle">
        <c:out value="${actionBean.pageInstance.page.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <div class=".ui-state-error">
            This portlet has thrown an exception<%
                Object exception = request.getAttribute(AbstractPageAction.PORTOFINO_PORTLET_EXCEPTION);
                if(exception instanceof Throwable) {
                    Throwable rootCause = ExceptionUtils.getRootCause((Throwable) exception);
                    if(rootCause == null) {
                        rootCause = (Throwable) exception;
                    }
                    out.write(" (" + rootCause.toString() + ")");
                }
            %>. Consult the log files for details.
        </div>
    </stripes:layout-component>
    <stripes:layout-component name="portletFooter" />
</stripes:layout-render>