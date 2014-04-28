<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<%@ taglib prefix="mde" uri="/manydesigns-elements"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"%>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.custom.CustomAction"/>
<stripes:layout-render name="/theme/templates/${actionBean.pageInstance.layout.template}/normal.jsp">
    <stripes:layout-component name="pageTitle">
        <c:out value="${actionBean.page.title}"/>
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
        <h3 class="pageTitle">
            <stripes:url var="avatarUrl" value="${actionBean.context.actionPath}" event="photo" prependContext="true">
                <stripes:param name="avatar" value="${actionBean.avatar}" />
            </stripes:url>
            <img src="${avatarUrl}"
                 alt="User photo" />
            <c:out value="${actionBean.user.first_name} ${actionBean.user.last_name}"/>
        </h3>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <div class="row">
            <div class="col-md-8">
                <form class="form-horizontal">
                    <mde:write name="actionBean" property="form" />
                </form>
            </div>
            <div class="col-md-4 well">
                <h3>Actions</h3>
                <stripes:form action="${actionBean.context.actionPath}">
                    <portofino:buttons list="view" cssClass="btn-block" />
                </stripes:form>
                <hr/>
                To finish your session:
                <stripes:link href="/login" class="btn">
                    <stripes:param name="logout"/>
                    Log out
                    <i class="glyphicon glyphicon-chevron-right"></i>
                </stripes:link>
            </div>
        </div>
    </stripes:layout-component>
</stripes:layout-render>