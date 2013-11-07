<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.custom.CustomAction"/>
<stripes:layout-render name="/theme/templates/${actionBean.pageInstance.layout.template}/normal.jsp">
    <stripes:layout-component name="pageHeader">
        <div class="pull-right">
            <stripes:link class="btn btn-small" href="/projects/${actionBean.project.id}">
                <stripes:param name="edit"/>
                Edit project details
            </stripes:link>
        </div>
        <h3 class="pageTitle">
            <stripes:layout-component name="pageTitle">
                <c:out value="${actionBean.project.id} - ${actionBean.project.title}"/>
            </stripes:layout-component>
            <c:if test="${actionBean.project['public']}">
                <span style="vertical-align: middle" class="label label-success">Public project</span>
            </c:if>
            <c:if test="${not actionBean.project['public']}">
                <span style="vertical-align: middle" class="label label-warning">Private project</span>
            </c:if>
        </h3>
        <div><c:out value="${actionBean.project.description}"/></div>
        <div>
            <c:if test="${not empty actionBean.project.url}">
                <stripes:link href="${actionBean.project.url}"><c:out value="${actionBean.project.url}"/></stripes:link>
            </c:if>
        </div>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
    </stripes:layout-component>
</stripes:layout-render>