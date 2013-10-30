<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"
%><%@ page import="com.manydesigns.portofino.pageactions.PageActionLogic"
%><%@ page import="org.slf4j.LoggerFactory"
%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><jsp:useBean id="actionBean" scope="request"
               type="com.manydesigns.portofino.pageactions.AbstractPageAction" /><%--
--%><stripes:layout-definition><%--
--%><c:set var="embedded" value="<%= PageActionLogic.isEmbedded(actionBean) %>" scope="page" /><%--
--%><c:if test="${embedded}"><%--
--%><div class="pageHeader">
        <stripes:layout-component name="pageHeader">
            <div class="pull-right">
                <stripes:form action="${actionBean.context.actualServletPath}" method="post">
                    <input type="hidden" name="cancelReturnUrl" 
                           value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
                    <portofino:buttons list="pageHeaderButtons" cssClass="btn-mini" />
                </stripes:form>
            </div>
            <h4 class="pageTitle">
                <stripes:layout-component name="pageTitle">
                    <c:out value="${actionBean.pageInstance.description}"/>
                </stripes:layout-component>
            </h4>
        </stripes:layout-component>
    </div>
    <div class="pageBody">
        <stripes:layout-component name="pageBody" />
    </div>
    <div class="pageFooter">
        <stripes:layout-component name="pageFooter" />
    </div><%--
--%></c:if><%--
--%><c:if test="${not embedded}"><%--
--%><!DOCTYPE html>
<html lang="en">
    <jsp:include page="/theme/head.jsp">
        <jsp:param name="pageTitle" value="${pageTitle}" />
    </jsp:include>
    <body>
    <jsp:include page="/theme/header.jsp"/>
    <div class="container">
        <div class="row">
            <div class="span2">
                <portofino:embedded-page-actions list="aboveNavigation" />
                <jsp:include page="/theme/navigation.jsp" />
                <portofino:embedded-page-actions list="belowNavigation" />
            </div>
            <div class="content span10">
                <div class="contentHeader">
                    <stripes:layout-component name="contentHeader">
                        <mde:sessionMessages />
                        <jsp:include page="/theme/breadcrumbs.jsp" />
                    </stripes:layout-component>
                </div>
                <div class="pageHeader">
                    <stripes:layout-component name="pageHeader">
                        <div class="pull-right">
                            <stripes:form action="${actionBean.context.actualServletPath}"
                                          method="post">
                                <input type="hidden" name="cancelReturnUrl" 
                                       value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
                                <portofino:buttons list="pageHeaderButtons" cssClass="btn-mini" />
                            </stripes:form>
                        </div>
                        <h3 class="pageTitle">
                            <stripes:layout-component name="pageTitle">
                                <c:out value="${actionBean.pageInstance.description}"/>
                            </stripes:layout-component>
                        </h3>
                    </stripes:layout-component>
                </div>
                <div class="row-fluid">
                    <div class="span8">
                        <ul class="nav nav-tabs tabs-header">
                            <c:forEach var="embeddedPageAction" items="${ actionBean.embeddedPageActions['default'] }">
                                <li>
                                    <a href="#embeddedPageActionWrapper_<c:out value='${embeddedPageAction.id}' />"
                                       data-toggle="tab"><c:out value='${embeddedPageAction.page.title}' /></a>
                                </li>
                            </c:forEach>
                        </ul>
                        <div class="tab-content">
                            <c:forEach var="embeddedPageAction" items="${ actionBean.embeddedPageActions['default'] }">
                                <div class="tab-pane" id="embeddedPageActionWrapper_<c:out value='${embeddedPageAction.id}' />">
                                    <% try {%>
                                    <jsp:include page="${embeddedPageAction.path}" flush="false" />
                                    <%} catch (Throwable t) {
                                        LoggerFactory.getLogger(actionBean.getClass()).error("Error in included page", t);
                                    %>
                                    <div class="alert alert-error">
                                        <button data-dismiss="alert" class="close" type="button">&times;</button>
                                        <ul class="errorMessages">
                                            <li>
                                                <fmt:message key="pageaction.view.error">
                                                    <fmt:param value="${embeddedPageAction.path}" />
                                                </fmt:message>
                                            </li>
                                        </ul>
                                    </div>
                                    <%}%>
                                </div>
                            </c:forEach>
                        </div>
                        <script>
                            $('.tabs-header a:first').tab('show');
                        </script>
                    </div>
                    <div class="span4">
                        <div class="well">
                            <div>
                                <div class="pageBody">
                                    <stripes:layout-component name="pageBody" />
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="pageFooter">
                    <stripes:layout-component name="pageFooter" />
                </div>
                <div class="contentFooter">
                    <stripes:layout-component name="contentFooter" />
                </div>
            </div>
        </div>
    </div>
    <jsp:include page="/theme/footer.jsp"/>
    </body>
</html>
</c:if>
</stripes:layout-definition>