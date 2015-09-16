<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"%>
<%@ taglib prefix="mde" uri="/manydesigns-elements"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.crud.AbstractCrudAction"/>
<stripes:layout-render name="/theme/templates/${actionBean.pageTemplate}/normal.jsp">
    <stripes:layout-component name="contentHeader">
        <mde:sessionMessages />
        <div class="pull-right">
            <jsp:include page="/m/crud/return-to-parent.jsp" />
        </div>
        <jsp:include page="/theme/breadcrumbs.jsp" />
    </stripes:layout-component>
    <stripes:layout-component name="pageHeader">
        <div class="pull-right">
            <stripes:form action="${actionBean.context.actionPath}"
                          method="post">
                <input type="hidden" name="returnUrl"
                       value="<c:out value="${actionBean.returnUrl}"/>"/>
                <portofino:buttons list="pageHeaderButtons" cssClass="btn-xs" />
            </stripes:form>
        </div>
        <div>
            <strong><c:out value="${actionBean.object.project}-${actionBean.object.n}"/></strong>
            <span class="<c:out value="${actionBean.object.fk_ticket_type.css_class}"/>"><c:out value="${actionBean.object.fk_ticket_type.type}"/></span>
            <span class="<c:out value="${actionBean.object.fk_ticket_state.css_class}"/>"><c:out value="${actionBean.object.fk_ticket_state.state}"/></span>
            <span class="<c:out value="${actionBean.object.fk_ticket_priority.css_class}"/>">Priority: <c:out value="${actionBean.object.fk_ticket_priority.priority}"/></span>
        </div>
        <h3 class="pageTitle">
            <c:out value="${actionBean.object.title}"/>
        </h3>
    </stripes:layout-component>
    <stripes:layout-component name="pageTitle">
        <c:out value="${actionBean.object.project}-${actionBean.object.n} ${actionBean.object.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <stripes:form action="${actionBean.context.actionPath}" method="post" class="ticket-read">
            <%-- Hidden submit so that ENTER on a form executes the default action --%>
            <div class="hidden-submit"><portofino:buttons list="crud-read-default-button" /></div>
            <div>
                <portofino:buttons list="crud-read" />
            </div>
            <div style="margin-top:10px">
                <portofino:buttons list="assign" />
            </div>
            <div style="margin-top:10px">
                <portofino:buttons list="ticket-workflow1" />
            </div>
            <div style="margin-top:10px">
                <portofino:buttons list="ticket-workflow2" />
            </div>
            <hr/>
            <input type="hidden" name="returnUrl" value="<c:out value="${actionBean.returnUrl}"/>"/>
            <mde:write name="actionBean" property="form"/>
            <c:if test="${not empty actionBean.searchString}">
                <input type="hidden" name="searchString" value="<c:out value="${actionBean.searchString}"/>"/>
            </c:if>
            <script type="text/javascript">
                $(".crudReadButtons button[name=delete]").click(function() {
                    return confirm ('<fmt:message key="are.you.sure" />');
                });
            </script>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>