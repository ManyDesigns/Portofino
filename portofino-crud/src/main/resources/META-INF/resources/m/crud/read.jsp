<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><stripes:layout-render name="/m/theme${actionBean.pageTemplate}/normal.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.crud.AbstractCrudAction"/>
    <stripes:layout-component name="beforeBreadcrumbs">
        <div class="pull-right">
            <jsp:include page="result-set-navigation.jsp" />
            <jsp:include page="return-to-parent.jsp" />
        </div>
    </stripes:layout-component>
    <stripes:layout-component name="portletTitle">
        <c:out value="${actionBean.readTitle}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <stripes:form action="${actionBean.dispatch.originalPath}" method="post" class="form-horizontal">
            <%-- Hidden submit so that ENTER on a form executes the default action --%>
            <div class="hidden-submit"><portofino:buttons list="portlet-default-button" /></div>
            <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
            <mde:write name="actionBean" property="form"/>
            <c:if test="${not empty actionBean.searchString}">
                <input type="hidden" name="searchString" value="<c:out value="${actionBean.searchString}"/>"/>
            </c:if>
            <div class="crudReadButtons">
                <portofino:buttons list="crud-read" />
            </div>
            <script type="text/javascript">
                $(".crudReadButtons button[name=delete]").click(function() {
                    return confirm ('<fmt:message key="commons.confirm" />');
                });
            </script>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>