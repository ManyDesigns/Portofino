<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><stripes:layout-definition><%--
--%><!DOCTYPE html>
<html lang="<%= request.getLocale() %>">
    <jsp:useBean id="actionBean" scope="request"
                 type="com.manydesigns.portofino.pageactions.AbstractPageAction" />
    <jsp:include page="/theme/head.jsp">
        <jsp:param name="pageTitle" value="${pageTitle}" />
    </jsp:include>
    <body style="padding-top: 0;">
    <div class="container">
        <div class="row">
            <div class="col-md-12">
                <div class="contentHeader">
                    <stripes:layout-component name="contentHeader">
                        <mde:sessionMessages />
                    </stripes:layout-component>
                </div>
                <div class="pageHeader">
                    <stripes:layout-component name="pageHeader">
                        <div class="pull-right">
                            <stripes:form action="${actionBean.context.actionPath}"
                                          method="post">
                                <input type="hidden" name="returnUrl"
                                       value="<c:out value="${actionBean.returnUrl}"/>"/>
                                <portofino:buttons list="pageHeaderButtons" cssClass="btn-xs" />
                            </stripes:form>
                        </div>
                        <h1 class="pageTitle">
                            <stripes:layout-component name="pageTitle">
                                <c:out value="${actionBean.pageInstance.description}"/>
                            </stripes:layout-component>
                        </h1>
                    </stripes:layout-component>
                </div>
                <div class="pageBody">
                    <stripes:layout-component name="pageBody" />
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
    </body>
</html>
</stripes:layout-definition>