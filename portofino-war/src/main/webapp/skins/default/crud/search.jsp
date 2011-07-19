<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><jsp:include page="/skins/default/header.jsp"/>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.UseCaseAction"/>
<stripes:form action="${actionBean.dispatch.absolutePath}" method="post">
    <jsp:include page="/skins/default/crud/searchButtonsBar.jsp"/>
    <div id="inner-content">
        <h1><c:out value="${actionBean.useCase.searchTitle}"/></h1>
        <div class="yui-ge">
            <div class="yui-u first">
                <div class="search_results">
                    <mde:write name="actionBean" property="tableForm"/>
                </div>
            </div>
            <div class="yui-u">
                <c:if test="${not empty actionBean.searchForm}">
                    <div class="search_form">
                        <mde:write name="actionBean" property="searchForm"/>
                        <stripes:submit name="search" value="Search"/>
                        <stripes:submit name="resetSearch" value="Reset form"/>
                    </div>
                </c:if>
            </div>
        </div>
        <stripes:hidden name="cancelReturnUrl" value="${actionBean.cancelReturnUrl}"/>
    </div>
    <jsp:include page="/skins/default/crud/searchButtonsBar.jsp"/>
</stripes:form>
<jsp:include page="/skins/default/footer.jsp"/>