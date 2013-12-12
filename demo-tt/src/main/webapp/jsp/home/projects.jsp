<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.custom.CustomAction"/>
<stripes:layout-render name="/theme/templates/${actionBean.pageInstance.layout.template}/normal.jsp">
    <stripes:layout-component name="pageTitle">
        <c:out value="${actionBean.page.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <div class="padding-bottom-20">
            <c:forEach items="${actionBean.projects}" var="project">
                <div class="media">
                    <stripes:link class="pull-left" href="/projects/${project[0]}">
                        <img class="media-object" alt="project" src="<stripes:url value="/images/placeholder-64x64.png"/>" />
                    </stripes:link>
                    <div class="media-body">
                        <h4 class="media-heading">
                            <stripes:link href="/projects/${project[0]}">
                                <c:out value="${project[0]} - ${project[1]}"/>
                            </stripes:link>
                            <c:if test="${project[4]}">
                                <span style="vertical-align: middle" class="label label-success">Public project</span>
                            </c:if>
                            <c:if test="${not project[4]}">
                                <span style="vertical-align: middle" class="label label-warning">Private project</span>
                            </c:if>
                        </h4>
                        <div><c:out value="${project[2]}"/></div>
                        <div>Active tickets: <c:out value="${project[3]}"/></div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </stripes:layout-component>
</stripes:layout-render>