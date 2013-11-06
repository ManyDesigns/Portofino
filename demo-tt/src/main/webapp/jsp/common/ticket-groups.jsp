<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<%@ taglib prefix="shiro" uri="http://shiro.apache.org/tags"%>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.custom.CustomAction"/>
<stripes:layout-render name="/theme/templates/${actionBean.pageInstance.layout.template}/normal.jsp">
    <stripes:layout-component name="pageTitle">
        <c:out value="${actionBean.page.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <c:if test="${empty actionBean.groups}">
            <p>There are no groups.</p>
        </c:if>
        <c:if test="${not empty actionBean.groups}">
            <table class="table table-striped table-condensed">
                <c:forEach items="${actionBean.groups}" var="group">
                    <tr>
                        <td>
                            <stripes:link href="${group.url}">
                                <c:out value="${group.groupName}"/>
                            </stripes:link>
                        </td>
                        <td>
                            <div class="text-right"><c:out value="${group.groupCount}"/></div>
                        </td>
                    </tr>
                </c:forEach>
            </table>
        </c:if>
    </stripes:layout-component>
</stripes:layout-render>