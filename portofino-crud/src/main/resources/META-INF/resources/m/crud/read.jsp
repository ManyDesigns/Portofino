<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.crud.AbstractCrudAction"
/><stripes:layout-render name="/theme/templates/${actionBean.pageTemplate}/normal.jsp">
    <stripes:layout-component name="contentHeader">
        <mde:sessionMessages />
        <jsp:include page="/theme/breadcrumbs.jsp" />
        <div class="pull-right">
            <c:if test="${actionBean.crudConfiguration.largeResultSet}">
                <jsp:include page="/m/crud/return-to-parent.jsp" />
            </c:if>
            <c:if test="${not actionBean.crudConfiguration.largeResultSet}">
                <jsp:include page="/m/crud/result-set-navigation.jsp" />
            </c:if>
        </div>
    </stripes:layout-component>
    <stripes:layout-component name="pageTitle">
        <c:out value="${actionBean.readTitle}"/>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <stripes:form action="${actionBean.context.actionPath}" method="post" class="form-horizontal read"
                      id="${(not empty actionBean.crudConfiguration.name) ? actionBean.crudConfiguration.name : null}">
            <%-- Hidden submit so that ENTER on a form executes the default action --%>
            <div class="hidden-submit"><portofino:buttons list="crud-read-default-button" /></div>
            <input type="hidden" name="returnUrl" value="<c:out value="${actionBean.returnUrl}"/>"/>
            <mde:write name="actionBean" property="form"/>
            <input type="hidden" name="searchString" value="<c:out value="${actionBean.searchString}"/>"/>
            <div class="form-group crudReadButtons">
                <portofino:buttons list="crud-read" />
            </div>
            <script type="text/javascript">
                $(".crudReadButtons button[name=delete]").click(function() {
                    return confirm ('<fmt:message key="are.you.sure" />');
                });
            </script>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>