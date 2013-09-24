<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %><%@
    taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><%@
    taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"%><%@
    taglib prefix="mde" uri="/manydesigns-elements"%><%@
    taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %><%--
--%><stripes:layout-render name="/theme/templates/default/normal.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.AbstractPageAction"/>
    <stripes:layout-component name="portletTitle">
        <c:out value="${actionBean.dispatch.originalPath}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <div class="alert alert-error">
            <button data-dismiss="alert" class="close" type="button">&times;</button>
            <ul class="errorMessages">
                <li>
                    <fmt:message key="portlet.404">
                        <fmt:param value="${actionBean.dispatch.absoluteOriginalPath}" />
                    </fmt:message>
                </li>
            </ul>
        </div>
    </stripes:layout-component>
</stripes:layout-render>