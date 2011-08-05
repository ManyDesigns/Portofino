<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><stripes:layout-definition><%--
--%><stripes:layout-render name="/skins/${skin}/common-with-navigation.jsp">
        <stripes:layout-component name="content">
            <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.PortletAction"/>
            <stripes:form action="${actionBean.dispatch.absoluteOriginalPath}" method="post"
                          enctype="${actionBean.multipartRequest ? 'multipart/form-data' : 'application/x-www-form-urlencoded'}">
                    <div class="portletPageHeader">
                        <stripes:layout-component name="portletPageHeader">
                            <c:if test="${not empty actionBean.returnToParentTarget}">
                                <stripes:submit name="returnToParent" value="<< Return to ${actionBean.returnToParentTarget}" class="portletPageButton"/>
                            </c:if>
                            <div class="breadcrumbs">
                                <div class="inner">
                                    <mde:write name="breadcrumbs"/>
                                </div>
                            </div>
                            <!-- Admin buttons -->
                            <div class="portletHeaderButtons" style="float: right;">
                                <button onclick="enablePortletDragAndDrop(this); return false;" class="arrow-4">Edit page layout</button>
                                <button name="reloadModel" class="refresh">Reload model</button>
                            </div>
                            <!-- End admin buttons -->
                            <c:set var="resultSetNavigation" scope="request" value="${actionBean.resultSetNavigation}"/>
                            <c:if test="${not empty resultSetNavigation}">
                                <div class="resultSetNavigation">
                                    <c:if test="${not empty resultSetNavigation.firstUrl}">
                                        <stripes:link id="first" href="${resultSetNavigation.firstUrl}">first</stripes:link>
                                        <stripes:link id="previous" href="${resultSetNavigation.previousUrl}">previous</stripes:link>
                                    </c:if>
                                    <c:if test="${empty resultSetNavigation.firstUrl}">
                                        <span class="disabled">first</span> <span class="disabled">previous</span>
                                    </c:if>
                                    <c:out value="${resultSetNavigation.position + 1}"/> of <c:out value="${resultSetNavigation.size}"/>
                                    <c:if test="${not empty resultSetNavigation.lastUrl}">
                                        <stripes:link id="next" href="${resultSetNavigation.nextUrl}">next</stripes:link>
                                        <stripes:link id="last" href="${resultSetNavigation.lastUrl}">last</stripes:link>
                                    </c:if>
                                    <c:if test="${empty resultSetNavigation.lastUrl}">
                                        <span class="disabled">next</span> <span class="disabled">last</span>
                                    </c:if>
                                </div>
                            </c:if>
                        </stripes:layout-component>
                    </div>
                    <div id="portletPageBody">
                        <stripes:layout-component name="portletPageBody">
                            <jsp:include page="/layouts/content/1-2-1-symmetric.jsp" />
                        </stripes:layout-component>
                    </div>
                    <div class="portletPageFooter">
                        <stripes:layout-component name="portletPageFooter">
                            <!-- TODO -->
                        </stripes:layout-component>
                    </div>
            </stripes:form>
        </stripes:layout-component>
    </stripes:layout-render>
</stripes:layout-definition>