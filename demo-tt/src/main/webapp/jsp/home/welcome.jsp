<%@ page import="com.manydesigns.portofino.shiro.ShiroUtils" %>
<%@ page import="org.apache.shiro.SecurityUtils" %>
<%@ page import="org.apache.shiro.subject.Subject" %>
<%@ page import="java.util.Map" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<%@ taglib prefix="shiro" uri="http://shiro.apache.org/tags" %>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.custom.CustomAction"/>
<stripes:layout-render name="/theme/templates/${actionBean.pageInstance.layout.template}/normal.jsp">
    <stripes:layout-component name="pageTitle">
        <shiro:authenticated>
            <%
                Subject subject = SecurityUtils.getSubject();
                Map principal = (Map) ShiroUtils.getPrimaryPrincipal(subject);
                pageContext.setAttribute("principal", principal);
            %>
            Welcome <c:out value="${principal['first_name']} ${principal['last_name']}"/>
        </shiro:authenticated>
        <shiro:notAuthenticated>
            <c:out value="${actionBean.page.title}"/>
        </shiro:notAuthenticated>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <p>
            Welcome to Portofino TT, an open-source, customizable ticket-tracker written in Java and Groovy.
        </p>
        <shiro:authenticated>
            <stripes:link href="/login" class="btn">
                <stripes:param name="logout"/>
                Log out
                <i class="icon-chevron-right"></i>
            </stripes:link>
        </shiro:authenticated>
        <shiro:notAuthenticated>
            <stripes:link href="/login" class="btn btn-success">Log in <i class="icon-chevron-right icon-white"></i></stripes:link>
        </shiro:notAuthenticated>
    </stripes:layout-component>
</stripes:layout-render>