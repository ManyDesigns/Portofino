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
        <shiro:user>
            <%
                Subject subject = SecurityUtils.getSubject();
                Map principal = (Map) ShiroUtils.getPrimaryPrincipal(subject);
                pageContext.setAttribute("principal", principal);
            %>
            Welcome <c:out value="${principal['first_name']} ${principal['last_name']}"/>
        </shiro:user>
        <shiro:guest>
            <c:out value="${actionBean.page.title}"/>
        </shiro:guest>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <shiro:user>
            <p>
                <stripes:link href="/profile" class="btn btn-success">
                    Edit your profile
                    <i class="glyphicon glyphicon-chevron-right white"></i>
                </stripes:link>
            </p>
            <shiro:hasRole name="admin">
                <p>
                    <stripes:link href="/admin" class="btn btn-success">
                        Manage users and settings
                        <i class="glyphicon glyphicon-chevron-right white"></i>
                    </stripes:link>
                </p>
            </shiro:hasRole>
            <shiro:hasRole name="project-manager">
                <p>
                    <stripes:link href="/projects" class="btn btn-success">
                        <stripes:param name="create"/>
                        Create a new project
                        <i class="glyphicon glyphicon-chevron-right white"></i>
                    </stripes:link>
                </p>
            </shiro:hasRole>
            <hr/>
            To finish your session:
            <stripes:link href="/login" class="btn">
                <stripes:param name="logout"/>
                Log out
                <i class="glyphicon glyphicon-chevron-right"></i>
            </stripes:link>
        </shiro:user>
        <shiro:guest>
            <p>
                <strong>Portofino TT</strong> is a modern, customizable ticket-tracker written in Java and Groovy,
                distributed under the LGPL open source license.
            </p>
            <p>
                Among its features:
            </p>
            <ul>
                <li>A clean, web-responsive user interface</li>
                <li>Public and private projects</li>
                <li>Tickets, versions and components</li>
                <li>Intuitive dashboards</li>
                <li>Activity streams</li>
                <li>Ticket and version workflows</li>
                <li>Ticket attachments</li>
                <li>Two system-level roles</li>
                <li>Four project-level roles</li>
                <li>Email notifications</li>
                <li>User management and self-registration</li>
            </ul>
            <p>
                To start using the system, please log in:
            </p>
            <p>
                <stripes:link href="/login" class="btn btn-success">Log in <i class="glyphicon glyphicon-chevron-right white"></i></stripes:link>
            </p>
        </shiro:guest>
    </stripes:layout-component>
</stripes:layout-render>