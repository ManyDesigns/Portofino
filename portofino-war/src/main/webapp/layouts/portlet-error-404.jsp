<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %><%@
    taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><%@
    taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"%><%@
    taglib prefix="mde" uri="/manydesigns-elements"%><%@
    taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %><%--
--%><stripes:layout-render name="/skins/${skin}/portlet.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.AbstractPageAction"/>
    <stripes:layout-component name="portletTitle">
        <c:out value="${actionBean.dispatch.originalPath}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <ul class="errorMessages">
            <li>
                <fmt:message key="portlet.404">
                    <fmt:param value="${actionBean.dispatch.absoluteOriginalPath}" />
                </fmt:message>
            </li>
        </ul>
    </stripes:layout-component>
    <stripes:layout-component name="portletFooter" />
</stripes:layout-render>