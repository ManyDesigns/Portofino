<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<%@ taglib prefix="shiro" uri="http://shiro.apache.org/tags" %>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.resourceactions.AbstractResourceAction"/>
<stripes:layout-render name="/theme/templates/${actionBean.pageTemplate}/normal.jsp">
    <stripes:layout-component name="pageTitle">
        <c:out value="${actionBean.actionInstance.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <p>
            This information is available only to project members.
        </p>
    </stripes:layout-component>
</stripes:layout-render>
