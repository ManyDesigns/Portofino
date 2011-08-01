<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><stripes:layout-definition><%--
--%><stripes:layout-render name="/skins/${skin}/common-with-navigation.jsp">
        <stripes:layout-component name="content">
            <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.AbstractActionBean"/>
            <stripes:form action="${actionBean.dispatch.absoluteOriginalPath}" method="post"
                          enctype="${actionBean.multipartRequest ? 'multipart/form-data' : 'application/x-www-form-urlencoded'}">
                    <div class="portletPageHeader">
                        <stripes:layout-component name="portletPageHeader">
                            <c:if test="${empty actionBean.returnToParentTarget}">
                                <stripes:submit name="returnToParent" value="<< Return to search" class="portletPageButton"/>
                            </c:if><c:if test="${not empty actionBean.returnToParentTarget}">
                                <stripes:submit name="returnToParent" value="<< Return to ${actionBean.returnToParentTarget}" class="portletPageButton"/>
                            </c:if>
                            <div class="breadcrumbs">
                                <div class="inner">
                                    <mde:write name="breadcrumbs"/>
                                </div>
                            </div>
                        </stripes:layout-component>
                    </div>
                    <div id="portletPageBody">
                        <stripes:layout-component name="portletPageBody">
                            <c:forEach var="portlet" items="${actionBean.portlets}" varStatus="status">
                                <div class="portletWrapper ${status.first ? 'first' : ''}">
                                    <jsp:include page="${portlet}" />
                                </div>
                            </c:forEach>
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